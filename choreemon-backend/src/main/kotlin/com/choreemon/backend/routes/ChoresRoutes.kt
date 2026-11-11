package com.choreemon.backend.routes

import com.choreemon.backend.config.ServiceRegistry
import com.choreemon.backend.domain.Element
import com.choreemon.backend.domain.Schedule
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

data class CreateChoreRequest(
    val title: String,
    val description: String = "",
    val points: Int,
    val element: String = "WATER",
    val schedule: String = "DAILY",
    val approval: Boolean = false,
    val assigned: List<String> = emptyList(),
    val steps: List<String> = emptyList()
)

data class UpdateChoreRequest(
    val title: String?,
    val description: String?,
    val points: Int?,
    val element: String?,
    val schedule: String?,
    val approval: Boolean?,
    val assigned: List<String>?,
    val steps: List<String>?
)

fun Route.choresRoutes(registry: ServiceRegistry) {
    route("/chores") {
        get {
            val householdId = call.principal<JWTPrincipal>()?.payload?.getClaim("householdId")?.asString()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val chores = registry.chores.findByHousehold(householdId)
            call.respond(chores)
        }

        post {
            val householdId = call.principal<JWTPrincipal>()?.payload?.getClaim("householdId")?.asString()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val isParent = call.principal<JWTPrincipal>()?.payload?.getClaim("isParent")?.asBoolean() ?: false
            if (!isParent) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Only parents can create quests"))
                return@post
            }

            val req = call.receive<CreateChoreRequest>()
            val chore = registry.chores.createChore(
                householdId = householdId,
                title = req.title,
                description = req.description,
                points = req.points,
                element = Element.valueOf(req.element.uppercase()),
                assignedMemberIds = req.assigned,
                steps = req.steps,
                schedule = Schedule.valueOf(req.schedule.uppercase()),
                requiresApproval = req.approval
            )

            // Notify assigned members via FCM
            for (memberId in req.assigned) {
                val tokens = registry.members.getDeviceTokensForMember(memberId)
                registry.notifications.sendNotification(
                    tokens = tokens,
                    title = "New Quest Available!",
                    body = "${req.title} (+${req.points} pts)",
                    data = mapOf("type" to "new_chore", "choreId" to chore.id)
                )
            }

            call.respond(HttpStatusCode.Created, chore)
        }

        put("/{id}") {
            val isParent = call.principal<JWTPrincipal>()?.payload?.getClaim("isParent")?.asBoolean() ?: false
            if (!isParent) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Only parents can edit quests"))
                return@put
            }

            val choreId = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            val req = call.receive<UpdateChoreRequest>()

            registry.chores.updateChore(
                choreId = choreId,
                title = req.title,
                description = req.description,
                points = req.points,
                element = req.element?.let { Element.valueOf(it.uppercase()) },
                schedule = req.schedule?.let { Schedule.valueOf(it.uppercase()) },
                requiresApproval = req.approval,
                steps = req.steps,
                assignedMemberIds = req.assigned
            )

            val updated = registry.chores.findById(choreId) ?: return@put call.respond(HttpStatusCode.NotFound)
            call.respond(updated)
        }

        delete("/{id}") {
            val isParent = call.principal<JWTPrincipal>()?.payload?.getClaim("isParent")?.asBoolean() ?: false
            if (!isParent) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Only parents can archive quests"))
                return@delete
            }

            val choreId = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            registry.chores.setArchived(choreId, true)
            call.respond(HttpStatusCode.OK, mapOf("status" to "archived"))
        }
    }
}
