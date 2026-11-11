package com.choreemon.backend.routes

import com.choreemon.backend.config.ServiceRegistry
import com.choreemon.backend.domain.GameState
import com.choreemon.backend.domain.weekOf
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDate

data class PinRequest(val pin: String)

fun Route.householdsRoutes(registry: ServiceRegistry) {
    route("/household") {
        get {
            val principal = call.principal<JWTPrincipal>()?.payload
            val householdId = principal?.getClaim("householdId")?.asString() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val memberId = principal.getClaim("userId")?.asString() ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val household = registry.households.findById(householdId) ?: return@get call.respond(HttpStatusCode.NotFound)
            val members = registry.members.findByHousehold(householdId).map { it.member }
            val creatures = registry.creatures.findByHousehold(householdId)
            val chores = registry.chores.findByHousehold(householdId)
            val completions = registry.completions.findByHousehold(householdId)
            val milestones = registry.members.getClaimedMilestones(memberId)
            val activeCreature = registry.creatures.getActive(memberId)?.id ?: ""

            val state = GameState(
                household = household.name,
                inviteCode = household.inviteCode,
                onboarded = true,
                activeMember = memberId,
                activeCreature = activeCreature,
                members = members,
                creatures = creatures,
                chores = chores,
                completions = completions,
                week = household.weekStart.ifBlank { weekOf(LocalDate.now()).toString() },
                pinSalt = household.pinSalt,
                pinHash = household.pinHash,
                claimedMilestones = milestones
            )

            call.respond(state)
        }

        post("/verify-pin") {
            val householdId = call.principal<JWTPrincipal>()?.payload?.getClaim("householdId")?.asString()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val req = call.receive<PinRequest>()
            val household = registry.households.findById(householdId) ?: return@post call.respond(HttpStatusCode.NotFound)

            val isValid = registry.auth.verifyPassword(req.pin + household.pinSalt, household.pinHash)
            if (isValid) {
                call.respond(mapOf("valid" to true))
            } else {
                call.respond(HttpStatusCode.Forbidden, mapOf("valid" to false, "error" to "Incorrect PIN"))
            }
        }

        put("/pin") {
            val householdId = call.principal<JWTPrincipal>()?.payload?.getClaim("householdId")?.asString()
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val isParent = call.principal<JWTPrincipal>()?.payload?.getClaim("isParent")?.asBoolean() ?: false
            if (!isParent) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Only parents can change PIN"))
                return@put
            }

            val req = call.receive<PinRequest>()
            if (req.pin.length !in 4..6) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "PIN must be between 4 and 6 digits"))
                return@put
            }

            val newSalt = java.util.UUID.randomUUID().toString().substring(0, 8)
            val newHash = registry.auth.hashPassword(req.pin + newSalt)
            registry.households.updatePin(householdId, newSalt, newHash)
            call.respond(HttpStatusCode.OK, mapOf("status" to "updated"))
        }
    }
}
