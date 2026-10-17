package com.choreemon.app.data

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import kotlin.random.Random

class GameRulesTest {
    private val day = LocalDate.of(2026, 10, 17)
    private fun state() = demoState(day)

    @Test fun completingQuestAwardsPointsAndActiveBuddyXp() {
        val before = state()
        val (after, reward) = GameRules.complete(before, "dishes", day)
        assertEquals(130, after.member.points)
        assertEquals(330, after.member.weeklyPoints)
        assertEquals(120, after.companion.xp)
        assertEquals(12, reward.xp)
        assertFalse(reward.pending)
        assertEquals(3, GameRules.available(after, day).size)
        assertEquals(before.creatures[4].xp, after.creatures[4].xp)
    }

    @Test fun sameQuestCannotBeRewardedTwiceOnSameDay() {
        val (after, _) = GameRules.complete(state(), "dishes", day)
        assertThrows(IllegalArgumentException::class.java) { GameRules.complete(after, "dishes", day) }
    }

    @Test fun dailyQuestReturnsTomorrow() {
        val (after, _) = GameRules.complete(state(), "dishes", day)
        assertTrue(GameRules.available(after, day.plusDays(1)).any { it.id == "dishes" })
    }

    @Test fun oneTimeQuestStaysFinished() {
        val initial = state().let { it.copy(chores = it.chores.map { c -> if (c.id == "dishes") c.copy(schedule = Schedule.ONCE) else c }, completions = it.completions.filter { c -> c.choreId != "dishes" }) }
        val (after, _) = GameRules.complete(initial, "dishes", day)
        assertFalse(GameRules.available(after, day.plusDays(30)).any { it.id == "dishes" })
    }

    @Test fun pendingQuestHasNoRewardsUntilApproved() {
        val initial = state().let { it.copy(chores = it.chores.map { c -> c.copy(approval = true) }) }
        val (pending, reward) = GameRules.complete(initial, "dishes", day)
        assertTrue(reward.pending)
        assertEquals(initial.member.points, pending.member.points)
        assertEquals(initial.companion.xp, pending.companion.xp)
        val approved = GameRules.review(pending, pending.completions.last().id, true, day)
        assertEquals(130, approved.member.points)
        assertEquals(120, approved.companion.xp)
        assertThrows(IllegalArgumentException::class.java) { GameRules.review(approved, approved.completions.last().id, true, day) }
    }

    @Test fun rejectedQuestCanBeTriedAgainWithoutAwardingPoints() {
        val initial = state().let { it.copy(chores = it.chores.map { c -> c.copy(approval = true) }) }
        val (pending, _) = GameRules.complete(initial, "dishes", day)
        val rejected = GameRules.review(pending, pending.completions.last().id, false, day)
        assertTrue(GameRules.available(rejected, day).any { it.id == "dishes" })
        assertEquals(120, rejected.member.points)
    }

    @Test fun hatchDeductsPointsAndKeepsLeaderboardScore() {
        val initial = state()
        val (after, result) = GameRules.hatch(initial, EggKind.CLASSIC, day, Random(1))
        assertEquals(70, after.member.points)
        assertEquals(initial.member.weeklyPoints, after.member.weeklyPoints)
        assertEquals(initial.creatures.size + 1, after.creatures.size)
        assertEquals(initial.activeMember, result.creature.owner)
        assertEquals(initial.companion.id, after.companion.id)
    }

    @Test fun cannotHatchWithoutEnoughPoints() {
        assertThrows(IllegalArgumentException::class.java) { GameRules.hatch(state(), EggKind.AURORA, day, Random(1)) }
    }

    @Test fun rarityBoundariesMatchPublishedOdds() {
        class RollRandom(private val roll: Int) : Random() {
            override fun nextBits(bitCount: Int) = 0
            override fun nextInt(until: Int) = if (until == 100) roll else 0
        }
        val base = state().let { it.copy(members = it.members.map { m -> m.copy(points = 500) }) }
        EggKind.entries.forEach { kind ->
            fun rarity(roll: Int) = GameRules.hatch(base, kind, day, RollRandom(roll)).second.creature.rarity
            assertEquals(Rarity.COMMON, rarity(0))
            assertEquals(Rarity.COMMON, rarity(kind.common - 1))
            assertEquals(Rarity.RARE, rarity(kind.common))
            assertEquals(Rarity.RARE, rarity(kind.common + kind.rare - 1))
            assertEquals(Rarity.LEGENDARY, rarity(kind.common + kind.rare))
            assertEquals(Rarity.LEGENDARY, rarity(99))
        }
    }

    @Test fun streakBridgesYesterdayButResetsAfterMissedDay() {
        assertEquals(5, GameRules.streak(state(), "mia", day))
        val (after, _) = GameRules.complete(state(), "dishes", day)
        assertEquals(6, GameRules.streak(after, "mia", day))
        assertEquals(0, GameRules.streak(after, "mia", day.plusDays(2)))
    }

    @Test fun careChangesMoodAndLoveOnlyLiftsOneStep() {
        val initial = state()
        assertEquals(Mood.HAPPY, GameRules.mood(initial, initial.companion, day))
        assertEquals(Mood.CONTENT, GameRules.mood(initial, initial.companion, day.plusDays(2)))
        val later = day.plusDays(5)
        assertEquals(Mood.SAD, GameRules.mood(initial, initial.companion, later))
        assertEquals(Mood.CONTENT, GameRules.mood(initial, initial.companion.copy(lovedOn = later.toString()), later))
        assertEquals(Mood.SAD, GameRules.mood(initial, initial.companion.copy(lovedOn = later.toString()), later.plusDays(1)))
    }

    @Test fun allBuddiesBenefitFromAChoreButOnlyActiveBuddyGainsXp() {
        val initial = state()
        val late = day.plusDays(5)
        val (after, _) = GameRules.complete(initial, "dishes", late)
        assertTrue(after.team.all { GameRules.mood(after, it, late) == Mood.HAPPY })
        assertEquals(initial.creatures[4].xp, after.creatures[4].xp)
    }

    @Test fun evolutionRequiresCorrectXpAndCannotSkipStage() {
        val initial = state()
        assertThrows(IllegalArgumentException::class.java) { GameRules.evolve(initial, initial.companion.id) }
        val ready = initial.copy(creatures = initial.creatures.map { if (it.id == initial.companion.id) it.copy(xp = 360) else it })
        val evolved = GameRules.evolve(ready, ready.companion.id)
        assertEquals("Bubblescale", evolved.companion.species)
        assertEquals(1, evolved.companion.stage)
        assertThrows(IllegalArgumentException::class.java) { GameRules.evolve(evolved, evolved.companion.id) }
    }

    @Test fun finalFormIsAvailableForAllRaritiesAndStopsEvolving() {
        Rarity.entries.forEach { rarity ->
            val initial = state().let { it.copy(creatures = it.creatures.map { c -> c.copy(xp = 960, stage = 1, rarity = rarity) }) }
            val after = GameRules.evolve(initial, initial.companion.id)
            assertEquals("Dishdrake", after.companion.species)
            assertFalse(after.companion.canEvolve)
        }
    }

    @Test fun weeklyRolloverKeepsWalletAndResetsRanking() {
        val refreshed = GameRules.refresh(state(), day.plusDays(2))
        assertTrue(refreshed.members.all { it.weeklyPoints == 0 })
        assertEquals(120, refreshed.member.points)
    }

    @Test fun scheduledAndUnassignedAndArchivedQuestsAreUnavailable() {
        val initial = state().let { it.copy(chores = it.chores.mapIndexed { index, chore -> when(index) { 0 -> chore.copy(schedule = Schedule.WEEKDAYS); 1 -> chore.copy(assigned = listOf("leo")); 2 -> chore.copy(archived = true); else -> chore } }) }
        assertEquals(listOf("trash"), GameRules.available(initial, day).map { it.id })
        assertThrows(IllegalArgumentException::class.java) { GameRules.complete(initial, "laundry", day) }
    }

    @Test fun tenthQuestGiftIsOneTimeAndDoesNotInflateWeeklyRank() {
        var current = state()
        listOf("dishes", "laundry", "vacuum").forEach { current = GameRules.complete(current, it, day).first }
        val balance = current.member.points
        val rank = current.member.weeklyPoints
        current = GameRules.claimMilestone(current)
        assertEquals(balance + 25, current.member.points)
        assertEquals(rank, current.member.weeklyPoints)
        assertThrows(IllegalArgumentException::class.java) { GameRules.claimMilestone(current) }
    }

    @Test fun switchingProfileUsesThatMembersTeamAndBalance() {
        val after = GameRules.switchMember(state(), "leo")
        assertEquals("leo", after.companion.owner)
        assertEquals(95, after.member.points)
        assertTrue(after.team.all { it.owner == "leo" })
    }

    @Test fun emptyNamesAndInvalidPointValuesCannotBeSaved() {
        val initial = state()
        assertThrows(IllegalArgumentException::class.java) { GameRules.validateChore(initial.chores.first().copy(title = ""), initial) }
        assertThrows(IllegalArgumentException::class.java) { GameRules.validateChore(initial.chores.first().copy(points = 101), initial) }
        assertThrows(IllegalArgumentException::class.java) { GameRules.validateChore(initial.chores.first().copy(assigned = emptyList()), initial) }
    }

    @Test fun pendingSubmissionDoesNotIncreaseStreak() {
        val initial = state().let { it.copy(chores = it.chores.map { c -> c.copy(approval = true) }) }
        val (pending, _) = GameRules.complete(initial, "dishes", day)
        assertEquals(5, GameRules.streak(pending, "mia", day))
    }

    @Test fun aNewHatchStartsHappyEvenWhenOwnerHasBeenAway() {
        val old = state()
        val later = day.plusDays(8)
        val (after, result) = GameRules.hatch(old, EggKind.CLASSIC, later, Random(2))
        assertEquals(Mood.HAPPY, GameRules.mood(after, result.creature, later))
        assertEquals(Mood.SAD, GameRules.mood(after, old.companion, later))
    }
}
