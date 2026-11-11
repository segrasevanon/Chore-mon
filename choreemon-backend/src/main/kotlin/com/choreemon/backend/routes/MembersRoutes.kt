package com.choreemon.backend.routes

import com.choreemon.backend.config.ServiceRegistry
import com.choreemon.backend.domain.Element
import com.choreemon.backend.domain.GameRules
import com.choreemon.backend.domain.GameState
import com.choreemon.backend.domain.Rarity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDate

data class AddMemberRequest(
    val name: String,
    val avatar: Int = 0,
    val isParent: Boolean = false
)

fun Route.membersRoutes(registry: ServiceRegistry) {
    route("/members") {
        get {
            val householdId = call.principal<JWTPrincipal>()?.payload?.getClaim("householdId")?.asString()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val members = registry.members.findByHousehold(householdId).map { it.member }
            call.respond(members)
        }

        post {
            val householdId = call.principal<JWTPrincipal>()?.payload?.getClaim("householdId")?.asString()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val isParent = call.principal<JWTPrincipal>()?.payload?.getClaim("isParent")?.asBoolean() ?: false
            if (!isParent) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Only parents can add members"))
                return@post
            }

            val req = call.receive<AddMemberRequest>()
            val created = registry.members.createMember(
                householdId = householdId,
                name = req.name,
                avatar = req.avatar,
                isParent = req.isParent
            )

            // Auto-assign starter creature
            val starterElement = Element.entries.random()
            val starter = registry.creatures.createCreature(
                ownerId = created.member.id,
                element = starterElement,
                rarity = Rarity.COMMON,
                adoptedDate = LocalDate.now().toString()
            )
            registry.creatures.setActive(starter.id, created.member.id)

            call.respond(HttpStatusCode.Created, created.member)
        }

        get("/{id}/streak") {
            val memberId = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val householdId = call.principal<JWTPrincipal>()?.payload?.getClaim("householdId")?.asString()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val completions = registry.completions.findByHousehold(householdId)
            val members = registry.members.findByHousehold(householdId).map { it.member }
            val mockState = GameState(
                activeMember = memberId,
                members = members,
                completions = completions
            )

            val streak = GameRules.streak(mockState, memberId, LocalDate.now())
            call.respond(mapOf("streak" to streak))
        }

        post("/milestone") {
            val memberId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val householdId = call.principal<JWTPrincipal>()?.payload?.getClaim("householdId")?.asString()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val completions = registry.completions.findByHousehold(householdId)
            val members = registry.members.findByHousehold(householdId).map { it.member }
            val claimed = registry.members.getClaimedMilestones(memberId)

            val state = GameState(
                activeMember = memberId,
                members = members,
                completions = completions,
                claimedMilestones = claimed
            )

            val updated = GameRules.claimMilestone(state)
            val newlyClaimed = updated.claimedMilestones - claimed
            for (key in newlyClaimed) {
                registry.members.claimMilestone(memberId, key)
            }
            registry.members.updatePoints(memberId, pointsDelta = 25, weeklyPointsDelta = 0)

            call.respond(mapOf("status" to "claimed", "pointsAwarded" to 25))
        }
    }
}
