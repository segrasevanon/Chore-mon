package com.choreemon.backend.repositories

import com.choreemon.backend.db.dbQuery
import com.choreemon.backend.db.tables.Households
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID

data class HouseholdRecord(
    val id: String,
    val name: String,
    val inviteCode: String,
    val pinSalt: String,
    val pinHash: String,
    val weekStart: String,
    val createdAt: String
)

class HouseholdRepository {
    private fun toHousehold(row: ResultRow) = HouseholdRecord(
        id = row[Households.id],
        name = row[Households.name],
        inviteCode = row[Households.inviteCode],
        pinSalt = row[Households.pinSalt],
        pinHash = row[Households.pinHash],
        weekStart = row[Households.weekStart],
        createdAt = row[Households.createdAt]
    )

    suspend fun createHousehold(
        name: String,
        pinSalt: String = "",
        pinHash: String = "",
        weekStart: String = ""
    ): HouseholdRecord = dbQuery {
        val id = UUID.randomUUID().toString()
        val inviteCode = generateInviteCode()
        val now = Instant.now().toString()

        Households.insert {
            it[Households.id] = id
            it[Households.name] = name
            it[Households.inviteCode] = inviteCode
            it[Households.pinSalt] = pinSalt
            it[Households.pinHash] = pinHash
            it[Households.weekStart] = weekStart
            it[Households.createdAt] = now
        }

        HouseholdRecord(id, name, inviteCode, pinSalt, pinHash, weekStart, now)
    }

    suspend fun findById(id: String): HouseholdRecord? = dbQuery {
        Households.selectAll()
            .where { Households.id eq id }
            .map(::toHousehold)
            .singleOrNull()
    }

    suspend fun findByInviteCode(code: String): HouseholdRecord? = dbQuery {
        Households.selectAll()
            .where { Households.inviteCode eq code.uppercase() }
            .map(::toHousehold)
            .singleOrNull()
    }

    suspend fun updateWeekStart(id: String, weekStart: String) = dbQuery {
        Households.update({ Households.id eq id }) {
            it[Households.weekStart] = weekStart
        }
    }

    suspend fun updatePin(id: String, pinSalt: String, pinHash: String) = dbQuery {
        Households.update({ Households.id eq id }) {
            it[Households.pinSalt] = pinSalt
            it[Households.pinHash] = pinHash
        }
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}
