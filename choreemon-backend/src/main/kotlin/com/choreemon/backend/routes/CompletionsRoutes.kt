package com.choreemon.backend.routes

import com.choreemon.backend.config.ServiceRegistry
import com.choreemon.backend.domain.CompletionStatus
import com.choreemon.backend.domain.GameRules
import com.choreemon.backend.domain.GameState
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDate

data class CompleteChoreRequest(
    val choreId: String,
    val photoUrl: String = ""
)

data class ReviewCompletionRequest(
    val approve: Boolean
)

fun Route.completionsRoutes(registry: ServiceRegistry) {
    route("/completions") {
        get {
            val householdId = call.principal<JWTPrincipal>()?.payload?.getClaim("householdId")?.asString()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val completions = registry.completions.findByHousehold(householdId)
            call.respond(completions)
        }

        post {
            val memberId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val householdId = call.principal<JWTPrincipal>()?.payload?.getClaim("householdId")?.asString()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val req = call.receive<CompleteChoreRequest>()
            val today = LocalDate.now()

            val household = registry.households.findById(householdId) ?: return@post call.respond(HttpStatusCode.NotFound)
            val members = registry.members.findByHousehold(householdId).map { it.member }
            val creatures = registry.creatures.findByHousehold(householdId)
            val chores = registry.chores.findByHousehold(householdId)
            val completions = registry.completions.findByHousehold(householdId)
            val activeCreature = registry.creatures.getActive(memberId)?.id ?: ""

            val state = GameState(
                household = household.name,
                activeMember = memberId,
                activeCreature = activeCreature,
                members = members,
                creatures = creatures,
                chores = chores,
                completions = completions
            )

            val (_, reward) = GameRules.complete(state, req.choreId, today, req.photoUrl)
            val chore = chores.first { it.id == req.choreId }
            val pet = state.companion

            val newCompletion = com.choreemon.backend.domain.Completion(
                id = java.util.UUID.randomUUID().toString(),
                choreId = chore.id,
                memberId = memberId,
                date = today.toString(),
                title = chore.title,
                points = chore.points,
                xp = reward.xp,
                creatureId = pet.id,
                status = if (chore.approval) CompletionStatus.PENDING else CompletionStatus.APPROVED,
                photo = req.photoUrl
            )

            registry.completions.createCompletion(newCompletion)

            if (!chore.approval) {
                registry.members.updatePoints(memberId, reward.points, reward.points)
                registry.creatures.addXp(pet.id, reward.xp)
            } else {
                // Notify parent that quest needs review
                val parentTokens = registry.members.getDeviceTokensForHousehold(householdId, excludeMemberId = memberId)
                val memberName = members.firstOrNull { it.id == memberId }?.name ?: "Someone"
                registry.notifications.sendNotification(
                    tokens = parentTokens,
                    title = "Quest Completed!",
                    body = "$memberName finished ${chore.title} and needs approval.",
                    data = mapOf("type" to "review_needed", "completionId" to newCompletion.id)
                )
            }

            call.respond(HttpStatusCode.Created, mapOf("completion" to newCompletion, "reward" to reward))
        }

        post("/{id}/review") {
            val isParent = call.principal<JWTPrincipal>()?.payload?.getClaim("isParent")?.asBoolean() ?: false
            if (!isParent) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Only parents can approve quests"))
                return@post
            }

            val completionId = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val req = call.receive<ReviewCompletionRequest>()
            val completion = registry.completions.findById(completionId) ?: return@post call.respond(HttpStatusCode.NotFound)

            if (completion.status != CompletionStatus.PENDING) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Quest has already been reviewed"))
                return@post
            }

            val newStatus = if (req.approve) CompletionStatus.APPROVED else CompletionStatus.REJECTED
            registry.completions.updateStatus(completionId, newStatus)

            if (req.approve) {
                registry.members.updatePoints(completion.memberId, completion.points, completion.points)
                registry.creatures.addXp(completion.creatureId, completion.xp)
            }

            // Notify child that quest was reviewed
            val childTokens = registry.members.getDeviceTokensForMember(completion.memberId)
            val title = if (req.approve) "Quest Approved!" else "Quest Needs Revision"
            val body = if (req.approve) "${completion.title} was approved! +${completion.points} pts, +${completion.xp} XP." else "${completion.title} was not approved this time."
            registry.notifications.sendNotification(
                tokens = childTokens,
                title = title,
                body = body,
                data = mapOf("type" to "review_result", "completionId" to completionId, "approved" to req.approve.toString())
            )

            call.respond(mapOf("status" to newStatus.name))
        }
    }
}
