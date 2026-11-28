package com.choreemon.backend.repositories

import com.choreemon.backend.db.dbQuery
import com.choreemon.backend.db.tables.ClaimedMilestones
import com.choreemon.backend.db.tables.DeviceTokens
import com.choreemon.backend.db.tables.Members
import com.choreemon.backend.domain.Member
import org.jetbrains.exposed.sql.*
import java.time.Instant
import java.util.UUID

data class MemberWithAuth(
    val member: Member,
    val householdId: String,
    val authEmail: String?,
    val authPasswordHash: String?,
    val isParent: Boolean
)

class MemberRepository {
    private fun toMember(row: ResultRow) = Member(
        id = row[Members.id],
        name = row[Members.name],
        avatar = row[Members.avatar],
        points = row[Members.points],
        weeklyPoints = row[Members.weeklyPoints]
    )

    private fun toMemberWithAuth(row: ResultRow) = MemberWithAuth(
        member = toMember(row),
        householdId = row[Members.householdId],
        authEmail = row[Members.authEmail],
        authPasswordHash = row[Members.authPasswordHash],
        isParent = row[Members.isParent]
    )

    suspend fun createMember(
        householdId: String,
        name: String,
        avatar: Int = 0,
        isParent: Boolean = false,
        email: String? = null,
        passwordHash: String? = null
    ): MemberWithAuth = dbQuery {
        val id = UUID.randomUUID().toString()
        Members.insert {
            it[Members.id] = id
            it[Members.householdId] = householdId
            it[Members.authEmail] = email?.lowercase()
            it[Members.authPasswordHash] = passwordHash
            it[Members.name] = name
            it[Members.avatar] = avatar
            it[Members.points] = 0
            it[Members.weeklyPoints] = 0
            it[Members.isParent] = isParent
        }

        MemberWithAuth(
            member = Member(id, name, avatar, 0, 0),
            householdId = householdId,
            authEmail = email,
            authPasswordHash = passwordHash,
            isParent = isParent
        )
    }

    suspend fun findById(id: String): MemberWithAuth? = dbQuery {
        Members.selectAll()
            .where { Members.id eq id }
            .map(::toMemberWithAuth)
            .singleOrNull()
    }

    suspend fun findByEmail(email: String): MemberWithAuth? = dbQuery {
        Members.selectAll()
            .where { Members.authEmail eq email.lowercase() }
            .map(::toMemberWithAuth)
            .singleOrNull()
    }

    suspend fun findByHousehold(householdId: String): List<MemberWithAuth> = dbQuery {
        Members.selectAll()
            .where { Members.householdId eq householdId }
            .map(::toMemberWithAuth)
    }

    suspend fun updatePoints(memberId: String, pointsDelta: Int, weeklyPointsDelta: Int) = dbQuery {
        val current = Members.selectAll().where { Members.id eq memberId }.singleOrNull() ?: return@dbQuery
        Members.update({ Members.id eq memberId }) {
            it[points] = (current[points] + pointsDelta).coerceAtLeast(0)
            it[weeklyPoints] = (current[weeklyPoints] + weeklyPointsDelta).coerceAtLeast(0)
        }
    }

    suspend fun resetWeeklyPoints(householdId: String) = dbQuery {
        Members.update({ Members.householdId eq householdId }) {
            it[weeklyPoints] = 0
        }
    }

    suspend fun claimMilestone(memberId: String, milestoneKey: String) = dbQuery {
        val now = Instant.now().toString()
        ClaimedMilestones.insert {
            it[ClaimedMilestones.memberId] = memberId
            it[ClaimedMilestones.milestoneKey] = milestoneKey
            it[ClaimedMilestones.claimedAt] = now
        }
    }

    suspend fun getClaimedMilestones(memberId: String): Set<String> = dbQuery {
        ClaimedMilestones.selectAll()
            .where { ClaimedMilestones.memberId eq memberId }
            .map { it[ClaimedMilestones.milestoneKey] }
            .toSet()
    }

    suspend fun saveDeviceToken(memberId: String, fcmToken: String, platform: String = "ANDROID") = dbQuery {
        val now = Instant.now().toString()
        val existing = DeviceTokens.selectAll()
            .where { (DeviceTokens.memberId eq memberId) and (DeviceTokens.fcmToken eq fcmToken) }
            .singleOrNull()

        if (existing != null) {
            DeviceTokens.update({ DeviceTokens.id eq existing[DeviceTokens.id] }) {
                it[DeviceTokens.updatedAt] = now
            }
        } else {
            DeviceTokens.insert {
                it[id] = UUID.randomUUID().toString()
                it[DeviceTokens.memberId] = memberId
                it[DeviceTokens.fcmToken] = fcmToken
                it[DeviceTokens.platform] = platform
                it[updatedAt] = now
            }
        }
    }

    suspend fun getDeviceTokensForHousehold(householdId: String, excludeMemberId: String? = null): List<String> = dbQuery {
        val memberIds = if (excludeMemberId != null) {
            Members.selectAll().where { (Members.householdId eq householdId) and (Members.id neq excludeMemberId) }
        } else {
            Members.selectAll().where { Members.householdId eq householdId }
        }.map { it[Members.id] }

        if (memberIds.isEmpty()) return@dbQuery emptyList()

        DeviceTokens.selectAll()
            .where { DeviceTokens.memberId inList memberIds }
            .map { it[DeviceTokens.fcmToken] }
    }

    suspend fun getDeviceTokensForMember(memberId: String): List<String> = dbQuery {
        DeviceTokens.selectAll()
            .where { DeviceTokens.memberId eq memberId }
            .map { it[DeviceTokens.fcmToken] }
    }
}
