package com.choreemon.backend.repositories

import com.choreemon.backend.db.dbQuery
import com.choreemon.backend.db.tables.Creatures
import com.choreemon.backend.db.tables.Households
import com.choreemon.backend.db.tables.Members
import com.choreemon.backend.db.tables.Societies
import com.choreemon.backend.domain.*
import org.jetbrains.exposed.sql.*
import java.time.Instant
import java.util.UUID

class SocietyRepository {

    private fun toSociety(row: ResultRow) = Society(
        id = row[Societies.id],
        name = row[Societies.name],
        code = row[Societies.code],
        city = row[Societies.city],
        createdAt = row[Societies.createdAt]
    )

    suspend fun createSociety(
        name: String,
        code: String,
        city: String = "",
        id: String = UUID.randomUUID().toString()
    ): Society = dbQuery {
        val normalizedCode = code.trim().uppercase()
        require(normalizedCode.length in 3..12) { "Society code must be between 3 and 12 characters" }
        require(name.trim().isNotBlank()) { "Society name cannot be blank" }

        val now = Instant.now().toString()
        Societies.insert {
            it[Societies.id] = id
            it[Societies.name] = name.trim()
            it[Societies.code] = normalizedCode
            it[Societies.city] = city.trim()
            it[Societies.createdAt] = now
        }
        Society(id, name.trim(), normalizedCode, city.trim(), now)
    }

    suspend fun findById(id: String): Society? = dbQuery {
        Societies.selectAll()
            .where { Societies.id eq id }
            .map(::toSociety)
            .singleOrNull()
    }

    suspend fun findByCode(code: String): Society? = dbQuery {
        Societies.selectAll()
            .where { Societies.code eq code.trim().uppercase() }
            .map(::toSociety)
            .singleOrNull()
    }

    suspend fun findAll(): List<Society> = dbQuery {
        Societies.selectAll()
            .orderBy(Societies.name to SortOrder.ASC)
            .map(::toSociety)
    }

    suspend fun assignHousehold(householdId: String, societyId: String) = dbQuery {
        val societyExists = Societies.selectAll().where { Societies.id eq societyId }.count() > 0
        require(societyExists) { "Society not found" }
        Households.update({ Households.id eq householdId }) {
            it[Households.societyId] = societyId
        }
    }

    suspend fun getSocietyForHousehold(householdId: String): Society? = dbQuery {
        val household = Households.selectAll()
            .where { Households.id eq householdId }
            .singleOrNull() ?: return@dbQuery null

        val societyId = household[Households.societyId] ?: return@dbQuery null
        Societies.selectAll()
            .where { Societies.id eq societyId }
            .map(::toSociety)
            .singleOrNull()
    }

    suspend fun ensureDefaultSociety(): Society = dbQuery {
        val existing = Societies.selectAll()
            .where { (Societies.id eq "soc-default-001") or (Societies.code eq "SUNVALLEY") }
            .map(::toSociety)
            .firstOrNull()

        if (existing != null) return@dbQuery existing

        val now = Instant.now().toString()
        Societies.insert {
            it[Societies.id] = "soc-default-001"
            it[Societies.name] = "Sunshine Valley Community"
            it[Societies.code] = "SUNVALLEY"
            it[Societies.city] = "San Francisco, CA"
            it[Societies.createdAt] = now
        }
        Society("soc-default-001", "Sunshine Valley Community", "SUNVALLEY", "San Francisco, CA", now)
    }

    suspend fun getSocietyLeaderboard(
        societyId: String,
        currentHouseholdId: String? = null,
        currentMemberId: String? = null,
        period: String = "weekly"
    ): SocietyLeaderboardResponse = dbQuery {
        val society = Societies.selectAll()
            .where { Societies.id eq societyId }
            .map(::toSociety)
            .singleOrNull() ?: throw NoSuchElementException("Society with ID '$societyId' not found")

        // Query all members belonging to households in this society
        val memberRows = (Households innerJoin Members)
            .selectAll()
            .where { Households.societyId eq societyId }
            .toList()

        val allHouseholdIdsInSociety = Households.selectAll()
            .where { Households.societyId eq societyId }
            .map { it[Households.id] }
            .toSet()

        // Group rows by Household
        val householdGroups = memberRows.groupBy { it[Households.id] }

        // Household Rankings
        val householdRankingsList = allHouseholdIdsInSociety.map { hId ->
            val members = householdGroups[hId] ?: emptyList()
            val householdName = if (members.isNotEmpty()) {
                members.first()[Households.name]
            } else {
                Households.selectAll().where { Households.id eq hId }.single()[Households.name]
            }
            val weeklyPoints = members.sumOf { it[Members.weeklyPoints] }
            val totalPoints = members.sumOf { it[Members.points] }
            SocietyHouseholdLeaderboardEntry(
                rank = 0, // Assigned after sorting
                householdId = hId,
                householdName = householdName,
                memberCount = members.size,
                weeklyPoints = weeklyPoints,
                totalPoints = totalPoints,
                isCurrentHousehold = (hId == currentHouseholdId)
            )
        }

        val sortedHouseholds = if (period.equals("allTime", ignoreCase = true)) {
            householdRankingsList.sortedWith(
                compareByDescending<SocietyHouseholdLeaderboardEntry> { it.totalPoints }
                    .thenByDescending { it.weeklyPoints }
                    .thenBy { it.householdName }
            )
        } else {
            householdRankingsList.sortedWith(
                compareByDescending<SocietyHouseholdLeaderboardEntry> { it.weeklyPoints }
                    .thenByDescending { it.totalPoints }
                    .thenBy { it.householdName }
            )
        }.mapIndexed { index, entry -> entry.copy(rank = index + 1) }

        // Adventurer (Member) Rankings in Society
        val memberIds = memberRows.map { it[Members.id] }
        val activeCreatures = if (memberIds.isNotEmpty()) {
            Creatures.selectAll()
                .where { (Creatures.ownerId inList memberIds) and (Creatures.isActive eq true) }
                .associateBy { it[Creatures.ownerId] }
        } else emptyMap()

        val adventurerEntries = memberRows.map { row ->
            val mId = row[Members.id]
            val pet = activeCreatures[mId]
            val companionElement = pet?.get(Creatures.element)
            val companionStage = pet?.get(Creatures.stage) ?: 0
            val companionSpecies = companionElement?.let { elemName ->
                runCatching {
                    Element.valueOf(elemName.uppercase()).names[companionStage.coerceIn(0, 2)]
                }.getOrNull()
            }
            val companionName = pet?.let { p ->
                val nick = p[Creatures.nickname]
                if (nick.isNotBlank()) nick else companionSpecies
            }

            SocietyMemberLeaderboardEntry(
                rank = 0, // Assigned after sorting
                memberId = mId,
                memberName = row[Members.name],
                avatar = row[Members.avatar],
                householdId = row[Members.householdId],
                householdName = row[Households.name],
                weeklyPoints = row[Members.weeklyPoints],
                totalPoints = row[Members.points],
                isCurrentMember = (mId == currentMemberId),
                companionName = companionName,
                companionElement = companionElement,
                companionStage = companionStage
            )
        }

        val sortedAdventurers = if (period.equals("allTime", ignoreCase = true)) {
            adventurerEntries.sortedWith(
                compareByDescending<SocietyMemberLeaderboardEntry> { it.totalPoints }
                    .thenByDescending { it.weeklyPoints }
                    .thenBy { it.memberName }
            )
        } else {
            adventurerEntries.sortedWith(
                compareByDescending<SocietyMemberLeaderboardEntry> { it.weeklyPoints }
                    .thenByDescending { it.totalPoints }
                    .thenBy { it.memberName }
            )
        }.mapIndexed { index, entry -> entry.copy(rank = index + 1) }

        val myHouseholdRank = sortedHouseholds.firstOrNull { it.isCurrentHousehold }?.rank
        val myMemberRank = sortedAdventurers.firstOrNull { it.isCurrentMember }?.rank

        val totalWeeklyPoints = memberRows.sumOf { it[Members.weeklyPoints] }
        val totalPoints = memberRows.sumOf { it[Members.points] }

        val summary = SocietySummary(
            societyId = society.id,
            societyName = society.name,
            code = society.code,
            city = society.city,
            totalHouseholds = allHouseholdIdsInSociety.size,
            totalAdventurers = memberRows.size,
            totalWeeklyPoints = totalWeeklyPoints,
            totalPoints = totalPoints
        )

        SocietyLeaderboardResponse(
            society = summary,
            householdRankings = sortedHouseholds,
            adventurerRankings = sortedAdventurers,
            myHouseholdRank = myHouseholdRank,
            myMemberRank = myMemberRank
        )
    }

    suspend fun getGlobalSocietiesLeaderboard(
        period: String = "weekly",
        limit: Int = 50
    ): List<GlobalSocietyLeaderboardEntry> = dbQuery {
        val societies = Societies.selectAll().map(::toSociety)
        if (societies.isEmpty()) return@dbQuery emptyList()

        // Fetch all households and members
        val allHouseholdRows = Households.selectAll().toList()
        val allMemberRows = Members.selectAll().toList()

        val membersByHousehold = allMemberRows.groupBy { it[Members.householdId] }
        val householdsBySociety = allHouseholdRows.groupBy { it[Households.societyId] }

        val entries = societies.map { soc ->
            val households = householdsBySociety[soc.id] ?: emptyList()
            val members = households.flatMap { h -> membersByHousehold[h[Households.id]] ?: emptyList() }

            val totalWeekly = members.sumOf { it[Members.weeklyPoints] }
            val totalAllTime = members.sumOf { it[Members.points] }
            val avgWeekly = if (households.isNotEmpty()) {
                (totalWeekly.toDouble() / households.size * 10.0).let { Math.round(it) / 10.0 }
            } else 0.0

            GlobalSocietyLeaderboardEntry(
                rank = 0,
                societyId = soc.id,
                societyName = soc.name,
                code = soc.code,
                city = soc.city,
                householdCount = households.size,
                memberCount = members.size,
                totalWeeklyPoints = totalWeekly,
                averageWeeklyPointsPerHousehold = avgWeekly,
                totalPoints = totalAllTime
            )
        }

        val sorted = if (period.equals("allTime", ignoreCase = true)) {
            entries.sortedWith(
                compareByDescending<GlobalSocietyLeaderboardEntry> { it.totalPoints }
                    .thenByDescending { it.totalWeeklyPoints }
                    .thenBy { it.societyName }
            )
        } else {
            entries.sortedWith(
                compareByDescending<GlobalSocietyLeaderboardEntry> { it.totalWeeklyPoints }
                    .thenByDescending { it.averageWeeklyPointsPerHousehold }
                    .thenBy { it.societyName }
            )
        }

        sorted.take(limit).mapIndexed { index, item -> item.copy(rank = index + 1) }
    }
}
