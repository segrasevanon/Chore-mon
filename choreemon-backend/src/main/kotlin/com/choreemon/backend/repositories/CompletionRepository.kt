package com.choreemon.backend.repositories

import com.choreemon.backend.db.dbQuery
import com.choreemon.backend.db.tables.Completions
import com.choreemon.backend.db.tables.Members
import com.choreemon.backend.domain.Completion
import com.choreemon.backend.domain.CompletionStatus
import org.jetbrains.exposed.sql.*

class CompletionRepository {
    private fun toCompletion(row: ResultRow) = Completion(
        id = row[Completions.id],
        choreId = row[Completions.choreId],
        memberId = row[Completions.memberId],
        date = row[Completions.date],
        title = row[Completions.title],
        points = row[Completions.points],
        xp = row[Completions.xp],
        creatureId = row[Completions.creatureId],
        status = CompletionStatus.valueOf(row[Completions.status].uppercase()),
        photo = row[Completions.photoUrl]
    )

    suspend fun createCompletion(completion: Completion): Completion = dbQuery {
        Completions.insert {
            it[id] = completion.id
            it[choreId] = completion.choreId
            it[memberId] = completion.memberId
            it[creatureId] = completion.creatureId
            it[date] = completion.date
            it[title] = completion.title
            it[points] = completion.points
            it[xp] = completion.xp
            it[status] = completion.status.name
            it[photoUrl] = completion.photo
        }
        completion
    }

    suspend fun findById(id: String): Completion? = dbQuery {
        Completions.selectAll()
            .where { Completions.id eq id }
            .map(::toCompletion)
            .singleOrNull()
    }

    suspend fun findByHousehold(householdId: String): List<Completion> = dbQuery {
        (Completions innerJoin Members)
            .selectAll()
            .where { Members.householdId eq householdId }
            .map(::toCompletion)
    }

    suspend fun findByMember(memberId: String): List<Completion> = dbQuery {
        Completions.selectAll()
            .where { Completions.memberId eq memberId }
            .map(::toCompletion)
    }

    suspend fun updateStatus(id: String, status: CompletionStatus) = dbQuery {
        Completions.update({ Completions.id eq id }) {
            it[Completions.status] = status.name
        }
    }
}
