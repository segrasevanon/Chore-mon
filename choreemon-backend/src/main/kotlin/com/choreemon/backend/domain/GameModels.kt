package com.choreemon.backend.domain

// Copied verbatim from the Android app — only the package declaration changed.
// All imports are standard Java/Kotlin (no android.* dependencies).
// GameRules runs on both the device and the server — zero duplication.

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.random.Random

enum class Element(val label: String, val names: List<String>, val description: String) {
    WATER("Water",  listOf("Sudsy",     "Bubblescale", "Dishdrake"),   "A bubbly little buddy who turns a pile of dishes into a reason to smile."),
    EARTH("Earth",  listOf("Sockmite",  "Sockgolem",   "Socktitan"),   "A soft, sock-loving explorer. Every laundry day is a new adventure."),
    AIR(  "Air",    listOf("Dustbunny", "Whirlpup",    "Vacuumphant"), "A tiny whirlwind of fluff with a very big heart. Loves a freshly vacuumed room."),
    POISON("Poison",listOf("Stinkle",   "Grubsong",    "Trashorok"),   "A cheerful recycling champion who finds treasure in tidying up.")
}
enum class Rarity(val label: String)  { COMMON("Common"), RARE("Rare"), LEGENDARY("Legendary") }
enum class Mood(val label: String, val multiplier: Double) { HAPPY("Happy", 1.2), CONTENT("Content", 1.0), SAD("Sad", 0.8) }
enum class Schedule(val label: String) { DAILY("Daily"), WEEKDAYS("Weekdays"), WEEKENDS("Weekends"), ONCE("One time") }
enum class CompletionStatus { PENDING, APPROVED, REJECTED }
enum class EggKind(val label: String, val price: Int, val common: Int, val rare: Int, val legendary: Int) {
    CLASSIC("Classic", 50, 60, 35, 5),
    ENCHANTED("Enchanted", 100, 25, 65, 10),
    AURORA("Aurora", 200, 10, 60, 30)
}

data class Member(val id: String, val name: String, val avatar: Int, val points: Int = 0, val weeklyPoints: Int = 0)
data class Creature(
    val id: String, val owner: String, val element: Element, val rarity: Rarity,
    val xp: Int = 0, val stage: Int = 0, val nickname: String = "",
    val adopted: String, val lovedOn: String = "", val favorite: Boolean = false
) {
    val species        get() = element.names[stage.coerceIn(0, 2)]
    val name           get() = nickname.ifBlank { species }
    val level          get() = xp / 40 + 1
    val nextEvolutionXp get() = when (stage) { 0 -> 360; 1 -> 960; else -> null }
    val canEvolve      get() = nextEvolutionXp?.let { xp >= it } ?: false
}
data class Chore(
    val id: String, val title: String, val description: String, val points: Int,
    val element: Element, val assigned: List<String>, val steps: List<String>,
    val schedule: Schedule = Schedule.DAILY, val approval: Boolean = false, val archived: Boolean = false
)
data class Completion(
    val id: String, val choreId: String, val memberId: String, val date: String,
    val title: String, val points: Int, val xp: Int, val creatureId: String,
    val status: CompletionStatus, val photo: String = ""
)
data class Preferences(val sound: Boolean = false, val haptics: Boolean = true, val reducedMotion: Boolean = false)
data class GameState(
    val household: String = "", val inviteCode: String = "", val onboarded: Boolean = false,
    val activeMember: String = "", val activeCreature: String = "",
    val members: List<Member> = emptyList(), val creatures: List<Creature> = emptyList(),
    val chores: List<Chore> = emptyList(), val completions: List<Completion> = emptyList(),
    val week: String = weekOf(LocalDate.now()).toString(),
    val pinSalt: String = "", val pinHash: String = "",
    val preferences: Preferences = Preferences(), val claimedMilestones: Set<String> = emptySet()
) {
    val member    get() = members.first { it.id == activeMember }
    val team      get() = creatures.filter { it.owner == activeMember }
    val companion get() = team.firstOrNull { it.id == activeCreature } ?: team.first()
}
data class ChoreReward(val points: Int, val xp: Int, val pending: Boolean, val creatureName: String)
data class HatchResult(val creature: Creature, val kind: EggKind)

fun weekOf(day: LocalDate): LocalDate = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

object GameRules {
    fun refresh(state: GameState, today: LocalDate): GameState =
        if (state.week == weekOf(today).toString()) state
        else state.copy(week = weekOf(today).toString(), members = state.members.map { it.copy(weeklyPoints = 0) })

    fun isDue(chore: Chore, day: LocalDate): Boolean = !chore.archived && when (chore.schedule) {
        Schedule.WEEKDAYS -> day.dayOfWeek.value <= 5
        Schedule.WEEKENDS -> day.dayOfWeek.value >= 6
        else              -> true
    }

    fun completionFor(state: GameState, chore: Chore, memberId: String, day: LocalDate) =
        state.completions.lastOrNull {
            it.choreId == chore.id && it.memberId == memberId &&
                it.status != CompletionStatus.REJECTED &&
                (chore.schedule == Schedule.ONCE || it.date == day.toString())
        }

    fun available(state: GameState, day: LocalDate) = state.chores.filter {
        isDue(it, day) && state.activeMember in it.assigned &&
            completionFor(state, it, state.activeMember, day) == null
    }

    fun mood(state: GameState, creature: Creature, today: LocalDate): Mood {
        val adopted = LocalDate.parse(creature.adopted).toEpochDay()
        val last = maxOf(
            adopted,
            state.completions
                .filter { it.memberId == creature.owner && it.status == CompletionStatus.APPROVED }
                .maxOfOrNull { LocalDate.parse(it.date).toEpochDay() } ?: adopted
        )
        val age  = (today.toEpochDay() - last).coerceAtLeast(0)
        val base = when { age <= 1 -> Mood.HAPPY; age <= 3 -> Mood.CONTENT; else -> Mood.SAD }
        return if (creature.lovedOn == today.toString())
            when (base) { Mood.SAD -> Mood.CONTENT; else -> Mood.HAPPY }
        else base
    }

    fun streak(state: GameState, memberId: String, today: LocalDate): Int {
        val days = state.completions
            .filter { it.memberId == memberId && it.status == CompletionStatus.APPROVED }
            .map { LocalDate.parse(it.date) }.toSet()
        var cursor = if (today in days) today else today.minusDays(1)
        var count  = 0
        while (cursor in days) { count++; cursor = cursor.minusDays(1) }
        return count
    }

    fun complete(state: GameState, choreId: String, today: LocalDate, photo: String = ""): Pair<GameState, ChoreReward> {
        val current = refresh(state, today)
        val chore   = current.chores.firstOrNull { it.id == choreId } ?: error("This quest is no longer available.")
        require(chore in available(current, today)) { "This quest has already been completed or isn't assigned to you." }
        val pet = current.companion
        val xp  = (chore.points * mood(current, pet, today).multiplier).roundToInt().coerceAtLeast(1)
        val entry = Completion(
            UUID.randomUUID().toString(), chore.id, current.activeMember, today.toString(),
            chore.title, chore.points, xp, pet.id,
            if (chore.approval) CompletionStatus.PENDING else CompletionStatus.APPROVED,
            photo
        )
        val updated = current.copy(completions = current.completions + entry)
        return (if (chore.approval) updated else grant(updated, entry)) to
            ChoreReward(chore.points, xp, chore.approval, pet.name)
    }

    // Internal in Android; exposed here so CompletionRepository can call it after DB persist.
    fun grant(state: GameState, entry: Completion): GameState = state.copy(
        members   = state.members.map {
            if (it.id == entry.memberId) it.copy(points = it.points + entry.points, weeklyPoints = it.weeklyPoints + entry.points) else it
        },
        creatures = state.creatures.map {
            if (it.id == entry.creatureId) it.copy(xp = it.xp + entry.xp) else it
        }
    )

    fun review(state: GameState, completionId: String, approve: Boolean, today: LocalDate): GameState {
        val current = refresh(state, today)
        val entry   = current.completions.first { it.id == completionId }
        require(entry.status == CompletionStatus.PENDING) { "This quest has already been reviewed." }
        val updated = current.copy(completions = current.completions.map {
            if (it.id == completionId) it.copy(status = if (approve) CompletionStatus.APPROVED else CompletionStatus.REJECTED) else it
        })
        return if (approve) grant(updated, entry) else updated
    }

    fun hatch(state: GameState, kind: EggKind, today: LocalDate, random: Random = Random.Default): Pair<GameState, HatchResult> {
        val current = refresh(state, today)
        val member  = current.members.first { it.id == current.activeMember }
        require(member.points >= kind.price) { "Complete a few more quests to earn this egg." }
        val roll    = random.nextInt(100)
        val rarity  = when { roll < kind.common -> Rarity.COMMON; roll < kind.common + kind.rare -> Rarity.RARE; else -> Rarity.LEGENDARY }
        val creature = Creature(UUID.randomUUID().toString(), current.activeMember, Element.entries.random(random), rarity, adopted = today.toString())
        return current.copy(
            members   = current.members.map { if (it.id == current.activeMember) it.copy(points = it.points - kind.price) else it },
            creatures = current.creatures + creature
        ) to HatchResult(creature, kind)
    }

    fun evolve(state: GameState, creatureId: String, memberId: String): GameState {
        val creature = state.creatures.first { it.id == creatureId && it.owner == memberId }
        require(creature.canEvolve) { "A few more adventures will help your buddy evolve." }
        return state.copy(creatures = state.creatures.map { if (it.id == creatureId) it.copy(stage = it.stage + 1) else it })
    }

    fun claimMilestone(state: GameState): GameState {
        val completed = state.completions.count { it.memberId == state.activeMember && it.status == CompletionStatus.APPROVED }
        val milestone = completed / 10
        val key       = "${state.activeMember}:$milestone"
        require(milestone > 0 && key !in state.claimedMilestones) { "Keep going to unlock your next gift." }
        return state.copy(
            claimedMilestones = state.claimedMilestones + key,
            members = state.members.map { if (it.id == state.activeMember) it.copy(points = it.points + 25) else it }
        )
    }
}
