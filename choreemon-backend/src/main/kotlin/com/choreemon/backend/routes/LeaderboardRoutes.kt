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
        // Household-internal rankings (backwards-compatible)
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

        get("/household") {
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

        // Society-level rankings (Households & Adventurers in Society)
        get("/society") {
            val householdId = call.principal<JWTPrincipal>()?.payload?.getClaim("householdId")?.asString()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val memberId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()

            val period = call.request.queryParameters["period"] ?: "weekly"
            val requestedSocietyId = call.request.queryParameters["societyId"]

            val targetSocietyId = if (!requestedSocietyId.isNullOrBlank()) {
                requestedSocietyId
            } else {
                val household = registry.households.findById(householdId)
                household?.societyId ?: run {
                    val defaultSoc = registry.societies.ensureDefaultSociety()
                    registry.households.updateSociety(householdId, defaultSoc.id)
                    defaultSoc.id
                }
            }

            try {
                val leaderboard = registry.societies.getSocietyLeaderboard(
                    societyId = targetSocietyId,
                    currentHouseholdId = householdId,
                    currentMemberId = memberId,
                    period = period
                )
                call.respond(leaderboard)
            } catch (e: NoSuchElementException) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to (e.message ?: "Society not found")))
            }
        }

        // Global / cross-society rankings
        get("/societies") {
            val period = call.request.queryParameters["period"] ?: "weekly"
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
            val globalLeaderboard = registry.societies.getGlobalSocietiesLeaderboard(period, limit)
            call.respond(globalLeaderboard)
        }
    }
}
