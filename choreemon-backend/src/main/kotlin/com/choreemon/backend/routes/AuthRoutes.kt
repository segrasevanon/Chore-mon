package com.choreemon.backend.routes

import com.choreemon.backend.config.ServiceRegistry
import com.choreemon.backend.domain.Element
import com.choreemon.backend.domain.Rarity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDate

data class RegisterRequest(
    val householdName: String,
    val parentName: String,
    val email: String,
    val password: String,
    val pin: String = "1234"
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class JoinRequest(
    val inviteCode: String,
    val memberName: String,
    val avatar: Int = 0
)

data class DeviceTokenRequest(
    val token: String,
    val platform: String = "ANDROID"
)

fun Route.authRoutes(registry: ServiceRegistry) {
    route("/auth") {
        post("/register") {
            val req = call.receive<RegisterRequest>()
            if (req.email.isBlank() || req.password.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Email and password are required"))
                return@post
            }

            val existing = registry.members.findByEmail(req.email)
            if (existing != null) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to "An account with this email already exists"))
                return@post
            }

            val pinSalt = java.util.UUID.randomUUID().toString().substring(0, 8)
            val pinHash = registry.auth.hashPassword(req.pin + pinSalt)
            val passwordHash = registry.auth.hashPassword(req.password)

            val household = registry.households.createHousehold(
                name = req.householdName,
                pinSalt = pinSalt,
                pinHash = pinHash
            )

            val memberWithAuth = registry.members.createMember(
                householdId = household.id,
                name = req.parentName,
                avatar = 0,
                isParent = true,
                email = req.email,
                passwordHash = passwordHash
            )

            // Starter companion creature for parent
            val starter = registry.creatures.createCreature(
                ownerId = memberWithAuth.member.id,
                element = Element.WATER,
                rarity = Rarity.COMMON,
                adoptedDate = LocalDate.now().toString(),
                nickname = "Sudsy"
            )
            registry.creatures.setActive(starter.id, memberWithAuth.member.id)

            val token = registry.auth.generateToken(
                userId = memberWithAuth.member.id,
                householdId = household.id,
                isParent = true
            )

            call.respond(
                HttpStatusCode.Created,
                mapOf(
                    "token" to token,
                    "member" to memberWithAuth.member,
                    "household" to household
                )
            )
        }

        post("/login") {
            val req = call.receive<LoginRequest>()
            val memberWithAuth = registry.members.findByEmail(req.email)
            if (memberWithAuth == null || memberWithAuth.authPasswordHash == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                return@post
            }

            if (!registry.auth.verifyPassword(req.password, memberWithAuth.authPasswordHash)) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                return@post
            }

            val household = registry.households.findById(memberWithAuth.householdId)
            val token = registry.auth.generateToken(
                userId = memberWithAuth.member.id,
                householdId = memberWithAuth.householdId,
                isParent = memberWithAuth.isParent
            )

            call.respond(
                mapOf(
                    "token" to token,
                    "member" to memberWithAuth.member,
                    "household" to household
                )
            )
        }

        post("/join") {
            val req = call.receive<JoinRequest>()
            val household = registry.households.findByInviteCode(req.inviteCode)
            if (household == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Invalid household invite code"))
                return@post
            }

            val memberWithAuth = registry.members.createMember(
                householdId = household.id,
                name = req.memberName,
                avatar = req.avatar,
                isParent = false
            )

            // Random starter creature for kid
            val starterElement = Element.entries.random()
            val starter = registry.creatures.createCreature(
                ownerId = memberWithAuth.member.id,
                element = starterElement,
                rarity = Rarity.COMMON,
                adoptedDate = LocalDate.now().toString()
            )
            registry.creatures.setActive(starter.id, memberWithAuth.member.id)

            val token = registry.auth.generateToken(
                userId = memberWithAuth.member.id,
                householdId = household.id,
                isParent = false
            )

            call.respond(
                HttpStatusCode.Created,
                mapOf(
                    "token" to token,
                    "member" to memberWithAuth.member,
                    "household" to household
                )
            )
        }

        authenticate("jwt") {
            post("/device-token") {
                val memberId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val req = call.receive<DeviceTokenRequest>()
                registry.members.saveDeviceToken(memberId, req.token, req.platform)
                call.respond(HttpStatusCode.OK, mapOf("status" to "registered"))
            }
        }
    }
}
