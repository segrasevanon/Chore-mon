package com.choreemon.backend.repositories

import com.choreemon.backend.db.dbQuery
import com.choreemon.backend.db.tables.ChoreAssignments
import com.choreemon.backend.db.tables.Chores
import com.choreemon.backend.domain.Chore
import com.choreemon.backend.domain.Element
import com.choreemon.backend.domain.Schedule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class ChoreRepository {
    private val mapper = jacksonObjectMapper()

    suspend fun createChore(
        householdId: String,
        title: String,
        description: String,
        points: Int,
        element: Element,
        assignedMemberIds: List<String>,
        steps: List<String>,
        schedule: Schedule = Schedule.DAILY,
        requiresApproval: Boolean = false
    ): Chore = dbQuery {
        val choreId = UUID.randomUUID().toString()
        val stepsJson = mapper.writeValueAsString(steps)

        Chores.insert {
            it[id] = choreId
            it[Chores.householdId] = householdId
            it[Chores.title] = title
            it[Chores.description] = description
            it[Chores.points] = points
            it[Chores.element] = element.name
            it[Chores.schedule] = schedule.name
            it[Chores.requiresApproval] = requiresApproval
            it[Chores.archived] = false
            it[Chores.steps] = stepsJson
        }

        for (memberId in assignedMemberIds) {
            ChoreAssignments.insert {
                it[ChoreAssignments.choreId] = choreId
                it[ChoreAssignments.memberId] = memberId
            }
        }

        Chore(
            id = choreId,
            title = title,
            description = description,
            points = points,
            element = element,
            assigned = assignedMemberIds,
            steps = steps,
            schedule = schedule,
            approval = requiresApproval,
            archived = false
        )
    }

    suspend fun findById(choreId: String): Chore? = dbQuery {
        val row = Chores.selectAll().where { Chores.id eq choreId }.singleOrNull() ?: return@dbQuery null
        val assigned = ChoreAssignments.selectAll()
            .where { ChoreAssignments.choreId eq choreId }
            .map { it[ChoreAssignments.memberId] }
        val steps: List<String> = try {
            mapper.readValue(row[Chores.steps])
        } catch (_: Exception) {
            emptyList()
        }

        Chore(
            id = row[Chores.id],
            title = row[Chores.title],
            description = row[Chores.description],
            points = row[Chores.points],
            element = Element.valueOf(row[Chores.element].uppercase()),
            assigned = assigned,
            steps = steps,
            schedule = Schedule.valueOf(row[Chores.schedule].uppercase()),
            approval = row[Chores.requiresApproval],
            archived = row[Chores.archived]
        )
    }

    suspend fun findByHousehold(householdId: String, includeArchived: Boolean = false): List<Chore> = dbQuery {
        val query = Chores.selectAll().where { Chores.householdId eq householdId }
        val filtered = if (!includeArchived) query.where { Chores.archived eq false } else query
        val choreRows = filtered.toList()
        if (choreRows.isEmpty()) return@dbQuery emptyList()

        val choreIds = choreRows.map { it[Chores.id] }
        val assignments = ChoreAssignments.selectAll()
            .where { ChoreAssignments.choreId inList choreIds }
            .groupBy({ it[ChoreAssignments.choreId] }, { it[ChoreAssignments.memberId] })

        choreRows.map { row ->
            val id = row[Chores.id]
            val steps: List<String> = try {
                mapper.readValue(row[Chores.steps])
            } catch (_: Exception) {
                emptyList()
            }
            Chore(
                id = id,
                title = row[Chores.title],
                description = row[Chores.description],
                points = row[Chores.points],
                element = Element.valueOf(row[Chores.element].uppercase()),
                assigned = assignments[id] ?: emptyList(),
                steps = steps,
                schedule = Schedule.valueOf(row[Chores.schedule].uppercase()),
                approval = row[Chores.requiresApproval],
                archived = row[Chores.archived]
            )
        }
    }

    suspend fun updateChore(
        choreId: String,
        title: String? = null,
        description: String? = null,
        points: Int? = null,
        element: Element? = null,
        schedule: Schedule? = null,
        requiresApproval: Boolean? = null,
        steps: List<String>? = null,
        assignedMemberIds: List<String>? = null
    ) = dbQuery {
        Chores.update({ Chores.id eq choreId }) {
            if (title != null) it[Chores.title] = title
            if (description != null) it[Chores.description] = description
            if (points != null) it[Chores.points] = points
            if (element != null) it[Chores.element] = element.name
            if (schedule != null) it[Chores.schedule] = schedule.name
            if (requiresApproval != null) it[Chores.requiresApproval] = requiresApproval
            if (steps != null) it[Chores.steps] = mapper.writeValueAsString(steps)
        }

        if (assignedMemberIds != null) {
            ChoreAssignments.deleteWhere { ChoreAssignments.choreId eq choreId }
            for (memberId in assignedMemberIds) {
                ChoreAssignments.insert {
                    it[ChoreAssignments.choreId] = choreId
                    it[ChoreAssignments.memberId] = memberId
                }
            }
        }
    }

    suspend fun setArchived(choreId: String, archived: Boolean) = dbQuery {
        Chores.update({ Chores.id eq choreId }) {
            it[Chores.archived] = archived
        }
    }
}
