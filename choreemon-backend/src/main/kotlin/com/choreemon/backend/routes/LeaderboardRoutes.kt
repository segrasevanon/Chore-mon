package com.choreemon.backend.routes

import com.choreemon.backend.config.ServiceRegistry
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

data class LeaderboardEntry(
    val memberId: String,
    val name: String,
    val avatar: Int,
    val weeklyPoints: Int,
    val totalPoints: Int,
    val rank: Int
)

fun Route.leaderboardRoutes(registry: ServiceRegistry) {
    route("/leaderboard") {
        get {
            val householdId = call.principal<JWTPrincipal>()?.payload?.getClaim("householdId")?.asString()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val members = registry.members.findByHousehold(householdId).map { it.member }
            val sorted = members.sortedByDescending { it.weeklyPoints }

            val leaderboard = sorted.mapIndexed { index, m ->
                LeaderboardEntry(
                    memberId = m.id,
                    name = m.name,
                    avatar = m.avatar,
                    weeklyPoints = m.weeklyPoints,
                    totalPoints = m.points,
                    rank = index + 1
                )
            }

            call.respond(leaderboard)
        }
    }
}
