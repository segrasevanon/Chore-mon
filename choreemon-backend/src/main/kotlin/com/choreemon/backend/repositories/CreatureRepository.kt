package com.choreemon.backend.repositories

import com.choreemon.backend.db.dbQuery
import com.choreemon.backend.db.tables.Creatures
import com.choreemon.backend.db.tables.Members
import com.choreemon.backend.domain.Creature
import com.choreemon.backend.domain.Element
import com.choreemon.backend.domain.Rarity
import org.jetbrains.exposed.sql.*
import java.util.UUID

class CreatureRepository {
    private fun toCreature(row: ResultRow) = Creature(
        id = row[Creatures.id],
        owner = row[Creatures.ownerId],
        element = Element.valueOf(row[Creatures.element].uppercase()),
        rarity = Rarity.valueOf(row[Creatures.rarity].uppercase()),
        xp = row[Creatures.xp],
        stage = row[Creatures.stage],
        nickname = row[Creatures.nickname],
        adopted = row[Creatures.adoptedDate],
        lovedOn = row[Creatures.lovedOn],
        favorite = row[Creatures.isFavorite]
    )

    suspend fun createCreature(
        ownerId: String,
        element: Element,
        rarity: Rarity,
        adoptedDate: String,
        nickname: String = "",
        id: String = UUID.randomUUID().toString()
    ): Creature = dbQuery {
        Creatures.insert {
            it[Creatures.id] = id
            it[Creatures.ownerId] = ownerId
            it[Creatures.element] = element.name
            it[Creatures.rarity] = rarity.name
            it[Creatures.xp] = 0
            it[Creatures.stage] = 0
            it[Creatures.nickname] = nickname
            it[Creatures.adoptedDate] = adoptedDate
            it[Creatures.lovedOn] = ""
            it[Creatures.isFavorite] = false
            it[Creatures.isActive] = false
        }
        Creature(id, ownerId, element, rarity, 0, 0, nickname, adoptedDate, "", false)
    }

    suspend fun findById(id: String): Creature? = dbQuery {
        Creatures.selectAll()
            .where { Creatures.id eq id }
            .map(::toCreature)
            .singleOrNull()
    }

    suspend fun findByOwner(ownerId: String): List<Creature> = dbQuery {
        Creatures.selectAll()
            .where { Creatures.ownerId eq ownerId }
            .map(::toCreature)
    }

    suspend fun findByHousehold(householdId: String): List<Creature> = dbQuery {
        (Creatures innerJoin Members)
            .selectAll()
            .where { Members.householdId eq householdId }
            .map(::toCreature)
    }

    suspend fun addXp(id: String, xpDelta: Int) = dbQuery {
        val current = Creatures.selectAll().where { Creatures.id eq id }.singleOrNull() ?: return@dbQuery
        Creatures.update({ Creatures.id eq id }) {
            it[xp] = current[xp] + xpDelta
        }
    }

    suspend fun evolve(id: String): Creature? = dbQuery {
        val current = Creatures.selectAll().where { Creatures.id eq id }.singleOrNull() ?: return@dbQuery null
        val nextStage = current[Creatures.stage] + 1
        Creatures.update({ Creatures.id eq id }) {
            it[stage] = nextStage
        }
        Creatures.selectAll().where { Creatures.id eq id }.map(::toCreature).singleOrNull()
    }

    suspend fun love(id: String, date: String) = dbQuery {
        Creatures.update({ Creatures.id eq id }) {
            it[lovedOn] = date
        }
    }

    suspend fun update(id: String, nickname: String? = null, favorite: Boolean? = null) = dbQuery {
        Creatures.update({ Creatures.id eq id }) {
            if (nickname != null) it[Creatures.nickname] = nickname
            if (favorite != null) it[isFavorite] = favorite
        }
    }

    suspend fun setActive(id: String, ownerId: String) = dbQuery {
        Creatures.update({ Creatures.ownerId eq ownerId }) {
            it[isActive] = false
        }
        Creatures.update({ (Creatures.id eq id) and (Creatures.ownerId eq ownerId) }) {
            it[isActive] = true
        }
    }

    suspend fun getActive(ownerId: String): Creature? = dbQuery {
        Creatures.selectAll()
            .where { (Creatures.ownerId eq ownerId) and (Creatures.isActive eq true) }
            .map(::toCreature)
            .singleOrNull() ?: findByOwner(ownerId).firstOrNull()
    }
}
