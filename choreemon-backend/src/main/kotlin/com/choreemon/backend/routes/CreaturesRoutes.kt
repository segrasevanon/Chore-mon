package com.choreemon.backend.routes

import com.choreemon.backend.config.ServiceRegistry
import com.choreemon.backend.domain.EggKind
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

data class HatchEggRequest(val kind: String)
data class UpdateCreatureRequest(
    val nickname: String? = null,
    val favorite: Boolean? = null,
    val active: Boolean? = null
)

fun Route.creaturesRoutes(registry: ServiceRegistry) {
    route("/creatures") {
        get {
            val memberId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val creatures = registry.creatures.findByOwner(memberId)
            call.respond(creatures)
        }

        post("/hatch") {
            val memberId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val householdId = call.principal<JWTPrincipal>()?.payload?.getClaim("householdId")?.asString()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val req = call.receive<HatchEggRequest>()
            val eggKind = EggKind.valueOf(req.kind.uppercase())
            val today = LocalDate.now()

            val members = registry.members.findByHousehold(householdId).map { it.member }
            val member = members.firstOrNull { it.id == memberId } ?: return@post call.respond(HttpStatusCode.NotFound)

            val state = GameState(
                activeMember = memberId,
                members = members
            )

            val (_, hatchResult) = GameRules.hatch(state, eggKind, today)
            val created = registry.creatures.createCreature(
                ownerId = memberId,
                element = hatchResult.creature.element,
                rarity = hatchResult.creature.rarity,
                adoptedDate = today.toString(),
                id = hatchResult.creature.id
            )

            // Deduct member points for egg purchase
            registry.members.updatePoints(memberId, -eggKind.price, 0)

            call.respond(HttpStatusCode.Created, hatchResult)
        }

        post("/{id}/evolve") {
            val memberId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val creatureId = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)

            val creature = registry.creatures.findById(creatureId) ?: return@post call.respond(HttpStatusCode.NotFound)
            if (creature.owner != memberId) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Not your buddy"))
                return@post
            }

            if (!creature.canEvolve) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Creature cannot evolve yet"))
                return@post
            }

            val updated = registry.creatures.evolve(creatureId)
            call.respond(updated ?: creature)
        }

        post("/{id}/love") {
            val memberId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val creatureId = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)

            val creature = registry.creatures.findById(creatureId) ?: return@post call.respond(HttpStatusCode.NotFound)
            if (creature.owner != memberId) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Not your buddy"))
                return@post
            }

            val today = LocalDate.now().toString()
            registry.creatures.love(creatureId, today)
            call.respond(mapOf("lovedOn" to today))
        }

        patch("/{id}") {
            val memberId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()
                ?: return@patch call.respond(HttpStatusCode.Unauthorized)
            val creatureId = call.parameters["id"] ?: return@patch call.respond(HttpStatusCode.BadRequest)

            val req = call.receive<UpdateCreatureRequest>()
            val creature = registry.creatures.findById(creatureId) ?: return@patch call.respond(HttpStatusCode.NotFound)
            if (creature.owner != memberId) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Not your buddy"))
                return@patch
            }

            if (req.nickname != null || req.favorite != null) {
                registry.creatures.update(creatureId, req.nickname, req.favorite)
            }

            if (req.active == true) {
                registry.creatures.setActive(creatureId, memberId)
            }

            val updated = registry.creatures.findById(creatureId)
            call.respond(updated ?: creature)
        }
    }
}
