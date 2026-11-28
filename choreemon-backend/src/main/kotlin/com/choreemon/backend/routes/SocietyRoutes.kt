package com.choreemon.backend.routes

import com.choreemon.backend.config.ServiceRegistry
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

data class CreateSocietyRequest(
    val name: String,
    val code: String,
    val city: String = ""
)

data class JoinSocietyRequest(
    val code: String
)

fun Route.societiesRoutes(registry: ServiceRegistry) {
    route("/societies") {
        get {
            val societies = registry.societies.findAll()
            call.respond(societies)
        }

        get("/my") {
            val householdId = call.principal<JWTPrincipal>()?.payload?.getClaim("householdId")?.asString()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val society = registry.societies.getSocietyForHousehold(householdId)
            if (society != null) {
                call.respond(society)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Household is not linked to any society"))
            }
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val society = registry.societies.findById(id)
            if (society != null) {
                call.respond(society)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Society not found"))
            }
        }

        post {
            val isParent = call.principal<JWTPrincipal>()?.payload?.getClaim("isParent")?.asBoolean() ?: false
            if (!isParent) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Only parents can create a society"))
                return@post
            }

            val req = call.receive<CreateSocietyRequest>()
            val existing = registry.societies.findByCode(req.code)
            if (existing != null) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to "A society with code '${req.code}' already exists"))
                return@post
            }

            val created = registry.societies.createSociety(
                name = req.name,
                code = req.code,
                city = req.city
            )

            // Auto-assign the creator's household to this new society
            val householdId = call.principal<JWTPrincipal>()?.payload?.getClaim("householdId")?.asString()
            if (householdId != null) {
                registry.households.updateSociety(householdId, created.id)
            }

            call.respond(HttpStatusCode.Created, created)
        }

        post("/join") {
            val householdId = call.principal<JWTPrincipal>()?.payload?.getClaim("householdId")?.asString()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val isParent = call.principal<JWTPrincipal>()?.payload?.getClaim("isParent")?.asBoolean() ?: false
            if (!isParent) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Only parents can change or join a society"))
                return@post
            }

            val req = call.receive<JoinSocietyRequest>()
            val society = registry.societies.findByCode(req.code)
            if (society == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Society with code '${req.code}' not found"))
                return@post
            }

            registry.households.updateSociety(householdId, society.id)
            call.respond(HttpStatusCode.OK, mapOf("status" to "joined", "society" to society))
        }
    }
}
