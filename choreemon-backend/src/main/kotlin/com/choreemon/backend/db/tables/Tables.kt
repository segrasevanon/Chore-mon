package com.choreemon.backend.db.tables

import org.jetbrains.exposed.sql.Table

object Households : Table("Households") {
    val id         = varchar("id", 36)
    val name       = varchar("name", 100)
    val inviteCode = varchar("invite_code", 12).uniqueIndex()
    val pinSalt    = varchar("pin_salt", 32)
    val pinHash    = varchar("pin_hash", 64)
    val weekStart  = varchar("week_start", 10)
    val createdAt  = varchar("created_at", 50)
    override val primaryKey = PrimaryKey(id)
}

object Members : Table("Members") {
    val id               = varchar("id", 36)
    val householdId      = varchar("household_id", 36) references Households.id
    val authEmail        = varchar("auth_email", 256).nullable()
    val authPasswordHash = varchar("auth_password_hash", 256).nullable()
    val name             = varchar("name", 64)
    val avatar           = integer("avatar").default(0)
    val points           = integer("points").default(0)
    val weeklyPoints     = integer("weekly_points").default(0)
    val isParent         = bool("is_parent").default(false)
    override val primaryKey = PrimaryKey(id)
}

object Creatures : Table("Creatures") {
    val id          = varchar("id", 36)
    val ownerId     = varchar("owner_id", 36) references Members.id
    val element     = varchar("element", 10)
    val rarity      = varchar("rarity", 12)
    val xp          = integer("xp").default(0)
    val stage       = integer("stage").default(0)
    val nickname    = varchar("nickname", 64).default("")
    val adoptedDate = varchar("adopted_date", 10)
    val lovedOn     = varchar("loved_on", 10).default("")
    val isFavorite  = bool("is_favorite").default(false)
    val isActive    = bool("is_active").default(false)
    override val primaryKey = PrimaryKey(id)
}

object Chores : Table("Chores") {
    val id               = varchar("id", 36)
    val householdId      = varchar("household_id", 36) references Households.id
    val title            = varchar("title", 100)
    val description      = varchar("description", 500).default("")
    val points           = integer("points")
    val element          = varchar("element", 10)
    val schedule         = varchar("schedule", 12).default("DAILY")
    val requiresApproval = bool("requires_approval").default(false)
    val archived         = bool("archived").default(false)
    val steps            = text("steps").default("[]")
    override val primaryKey = PrimaryKey(id)
}

object ChoreAssignments : Table("ChoreAssignments") {
    val choreId  = varchar("chore_id", 36)  references Chores.id
    val memberId = varchar("member_id", 36) references Members.id
    override val primaryKey = PrimaryKey(choreId, memberId)
}

object Completions : Table("Completions") {
    val id         = varchar("id", 36)
    val choreId    = varchar("chore_id", 36)    references Chores.id
    val memberId   = varchar("member_id", 36)   references Members.id
    val creatureId = varchar("creature_id", 36) references Creatures.id
    val date       = varchar("date", 10)
    val title      = varchar("title", 100)
    val points     = integer("points")
    val xp         = integer("xp")
    val status     = varchar("status", 10).default("PENDING")
    val photoUrl   = varchar("photo_url", 500).default("")
    override val primaryKey = PrimaryKey(id)
}

object DeviceTokens : Table("DeviceTokens") {
    val id        = varchar("id", 36)
    val memberId  = varchar("member_id", 36) references Members.id
    val fcmToken  = varchar("fcm_token", 256)
    val platform  = varchar("platform", 10).default("ANDROID")
    val updatedAt = varchar("updated_at", 50)
    override val primaryKey = PrimaryKey(id)
}

object ClaimedMilestones : Table("ClaimedMilestones") {
    val memberId     = varchar("member_id", 36) references Members.id
    val milestoneKey = varchar("milestone_key", 64)
    val claimedAt    = varchar("claimed_at", 50)
    override val primaryKey = PrimaryKey(memberId, milestoneKey)
}
