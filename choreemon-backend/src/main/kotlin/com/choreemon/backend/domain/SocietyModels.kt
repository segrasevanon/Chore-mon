package com.choreemon.backend.domain

data class Society(
    val id: String,
    val name: String,
    val code: String,
    val city: String = "",
    val createdAt: String
)

data class SocietySummary(
    val societyId: String,
    val societyName: String,
    val code: String,
    val city: String,
    val totalHouseholds: Int,
    val totalAdventurers: Int,
    val totalWeeklyPoints: Int,
    val totalPoints: Int
)

data class SocietyHouseholdLeaderboardEntry(
    val rank: Int,
    val householdId: String,
    val householdName: String,
    val memberCount: Int,
    val weeklyPoints: Int,
    val totalPoints: Int,
    val isCurrentHousehold: Boolean = false
)

data class SocietyMemberLeaderboardEntry(
    val rank: Int,
    val memberId: String,
    val memberName: String,
    val avatar: Int,
    val householdId: String,
    val householdName: String,
    val weeklyPoints: Int,
    val totalPoints: Int,
    val isCurrentMember: Boolean = false,
    val companionName: String? = null,
    val companionElement: String? = null,
    val companionStage: Int? = null
)

data class SocietyLeaderboardResponse(
    val society: SocietySummary,
    val householdRankings: List<SocietyHouseholdLeaderboardEntry>,
    val adventurerRankings: List<SocietyMemberLeaderboardEntry>,
    val myHouseholdRank: Int? = null,
    val myMemberRank: Int? = null
)

data class GlobalSocietyLeaderboardEntry(
    val rank: Int,
    val societyId: String,
    val societyName: String,
    val code: String,
    val city: String,
    val householdCount: Int,
    val memberCount: Int,
    val totalWeeklyPoints: Int,
    val averageWeeklyPointsPerHousehold: Double,
    val totalPoints: Int
)
