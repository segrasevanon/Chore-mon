package com.choreemon.backend.repositories

import com.choreemon.backend.db.tables.Creatures
import com.choreemon.backend.db.tables.Households
import com.choreemon.backend.db.tables.Members
import com.choreemon.backend.db.tables.Societies
import com.choreemon.backend.domain.Element
import com.choreemon.backend.domain.Rarity
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.*

class SocietyRepositoryTest {

    private lateinit var societyRepo: SocietyRepository
    private lateinit var householdRepo: HouseholdRepository
    private lateinit var memberRepo: MemberRepository
    private lateinit var creatureRepo: CreatureRepository

    @BeforeTest
    fun setUp() {
        Database.connect("jdbc:h2:mem:test_society_${System.currentTimeMillis()};DB_CLOSE_DELAY=-1;", "org.h2.Driver")
        transaction {
            SchemaUtils.create(Societies, Households, Members, Creatures)
        }
        societyRepo = SocietyRepository()
        householdRepo = HouseholdRepository()
        memberRepo = MemberRepository()
        creatureRepo = CreatureRepository()
    }

    @AfterTest
    fun tearDown() {
        transaction {
            SchemaUtils.drop(Creatures, Members, Households, Societies)
        }
    }

    @Test
    fun testCreateAndFindSociety() = runBlocking {
        val created = societyRepo.createSociety("Greenwood Heights", "GWH01", "Seattle, WA")
        assertEquals("Greenwood Heights", created.name)
        assertEquals("GWH01", created.code)
        assertEquals("Seattle, WA", created.city)
        assertNotNull(created.id)

        val foundById = societyRepo.findById(created.id)
        assertNotNull(foundById)
        assertEquals(created.name, foundById.name)

        val foundByCode = societyRepo.findByCode("gwh01")
        assertNotNull(foundByCode)
        assertEquals(created.id, foundByCode.id)
    }

    @Test
    fun testEnsureDefaultSociety() = runBlocking {
        val soc1 = societyRepo.ensureDefaultSociety()
        assertEquals("Sunshine Valley Community", soc1.name)
        assertEquals("SUNVALLEY", soc1.code)

        val soc2 = societyRepo.ensureDefaultSociety()
        assertEquals(soc1.id, soc2.id)
    }

    @Test
    fun testSocietyLeaderboardRankings() = runBlocking {
        val society = societyRepo.createSociety("Oakridge Society", "OAK01", "Austin, TX")

        // Household A: "The Chen Family" with 2 members (Mia: 300 pts, Leo: 200 pts => 500 weekly pts)
        val householdA = householdRepo.createHousehold("The Chen Family", societyId = society.id)
        val mia = memberRepo.createMember(householdA.id, "Mia", avatar = 0)
        val leo = memberRepo.createMember(householdA.id, "Leo", avatar = 1)
        memberRepo.updatePoints(mia.member.id, pointsDelta = 1000, weeklyPointsDelta = 300)
        memberRepo.updatePoints(leo.member.id, pointsDelta = 600, weeklyPointsDelta = 200)

        // Set active companion for Mia (Sudsy)
        val sudsy = creatureRepo.createCreature(mia.member.id, Element.WATER, Rarity.COMMON, "2026-10-17", nickname = "Sudsy")
        creatureRepo.setActive(sudsy.id, mia.member.id)

        // Household B: "The Miller Family" with 1 member (Sam: 600 pts => 600 weekly pts)
        val householdB = householdRepo.createHousehold("The Miller Family", societyId = society.id)
        val sam = memberRepo.createMember(householdB.id, "Sam", avatar = 2)
        memberRepo.updatePoints(sam.member.id, pointsDelta = 1200, weeklyPointsDelta = 600)

        // Household C: "The Patel Family" with 1 member (Aarav: 150 pts => 150 weekly pts)
        val householdC = householdRepo.createHousehold("The Patel Family", societyId = society.id)
        val aarav = memberRepo.createMember(householdC.id, "Aarav", avatar = 3)
        memberRepo.updatePoints(aarav.member.id, pointsDelta = 500, weeklyPointsDelta = 150)

        // Fetch Weekly Society Leaderboard from perspective of Household A and Mia
        val leaderboard = societyRepo.getSocietyLeaderboard(
            societyId = society.id,
            currentHouseholdId = householdA.id,
            currentMemberId = mia.member.id,
            period = "weekly"
        )

        // Verify Society Summary
        assertEquals("Oakridge Society", leaderboard.society.societyName)
        assertEquals(3, leaderboard.society.totalHouseholds)
        assertEquals(4, leaderboard.society.totalAdventurers)
        assertEquals(1250, leaderboard.society.totalWeeklyPoints) // 500 + 600 + 150
        assertEquals(3300, leaderboard.society.totalPoints) // 1600 + 1200 + 500

        // Verify Household Rankings:
        // Rank 1: The Miller Family (600 pts)
        // Rank 2: The Chen Family (500 pts)
        // Rank 3: The Patel Family (150 pts)
        assertEquals(3, leaderboard.householdRankings.size)
        assertEquals("The Miller Family", leaderboard.householdRankings[0].householdName)
        assertEquals(1, leaderboard.householdRankings[0].rank)
        assertEquals(600, leaderboard.householdRankings[0].weeklyPoints)
        assertFalse(leaderboard.householdRankings[0].isCurrentHousehold)

        assertEquals("The Chen Family", leaderboard.householdRankings[1].householdName)
        assertEquals(2, leaderboard.householdRankings[1].rank)
        assertEquals(500, leaderboard.householdRankings[1].weeklyPoints)
        assertTrue(leaderboard.householdRankings[1].isCurrentHousehold)

        assertEquals("The Patel Family", leaderboard.householdRankings[2].householdName)
        assertEquals(3, leaderboard.householdRankings[2].rank)

        assertEquals(2, leaderboard.myHouseholdRank)

        // Verify Adventurer Rankings:
        // Rank 1: Sam (600 pts)
        // Rank 2: Mia (300 pts)
        // Rank 3: Leo (200 pts)
        // Rank 4: Aarav (150 pts)
        assertEquals(4, leaderboard.adventurerRankings.size)
        assertEquals("Sam", leaderboard.adventurerRankings[0].memberName)
        assertEquals(1, leaderboard.adventurerRankings[0].rank)
        assertEquals(600, leaderboard.adventurerRankings[0].weeklyPoints)

        assertEquals("Mia", leaderboard.adventurerRankings[1].memberName)
        assertEquals(2, leaderboard.adventurerRankings[1].rank)
        assertTrue(leaderboard.adventurerRankings[1].isCurrentMember)
        assertEquals("Sudsy", leaderboard.adventurerRankings[1].companionName)
        assertEquals("WATER", leaderboard.adventurerRankings[1].companionElement)

        assertEquals("Leo", leaderboard.adventurerRankings[2].memberName)
        assertEquals(3, leaderboard.adventurerRankings[2].rank)

        assertEquals("Aarav", leaderboard.adventurerRankings[3].memberName)
        assertEquals(4, leaderboard.adventurerRankings[3].rank)

        assertEquals(2, leaderboard.myMemberRank)
    }

    @Test
    fun testAllTimeSocietyLeaderboard() = runBlocking {
        val society = societyRepo.createSociety("Maple Grove", "MPL01")
        val householdA = householdRepo.createHousehold("Household A", societyId = society.id)
        val householdB = householdRepo.createHousehold("Household B", societyId = society.id)

        val mA = memberRepo.createMember(householdA.id, "Alice")
        val mB = memberRepo.createMember(householdB.id, "Bob")

        // Alice has higher total points (2000), Bob has higher weekly points (500 vs 200)
        memberRepo.updatePoints(mA.member.id, pointsDelta = 2000, weeklyPointsDelta = 200)
        memberRepo.updatePoints(mB.member.id, pointsDelta = 1000, weeklyPointsDelta = 500)

        val weeklyLeaderboard = societyRepo.getSocietyLeaderboard(society.id, period = "weekly")
        assertEquals("Bob", weeklyLeaderboard.adventurerRankings[0].memberName)

        val allTimeLeaderboard = societyRepo.getSocietyLeaderboard(society.id, period = "allTime")
        assertEquals("Alice", allTimeLeaderboard.adventurerRankings[0].memberName)
        assertEquals("Household A", allTimeLeaderboard.householdRankings[0].householdName)
    }

    @Test
    fun testGlobalSocietiesLeaderboard() = runBlocking {
        val soc1 = societyRepo.createSociety("Society Alpha", "ALP01")
        val soc2 = societyRepo.createSociety("Society Beta", "BET01")

        val h1 = householdRepo.createHousehold("Alpha H1", societyId = soc1.id)
        val m1 = memberRepo.createMember(h1.id, "A1")
        memberRepo.updatePoints(m1.member.id, pointsDelta = 500, weeklyPointsDelta = 500)

        val h2 = householdRepo.createHousehold("Beta H1", societyId = soc2.id)
        val m2 = memberRepo.createMember(h2.id, "B1")
        memberRepo.updatePoints(m2.member.id, pointsDelta = 1000, weeklyPointsDelta = 1000)

        val global = societyRepo.getGlobalSocietiesLeaderboard(period = "weekly")
        assertEquals(2, global.size)
        assertEquals("Society Beta", global[0].societyName)
        assertEquals(1, global[0].rank)
        assertEquals(1000, global[0].totalWeeklyPoints)

        assertEquals("Society Alpha", global[1].societyName)
        assertEquals(2, global[1].rank)
        assertEquals(500, global[1].totalWeeklyPoints)
    }

    @Test
    fun testAssignHouseholdToSociety() = runBlocking {
        val society = societyRepo.createSociety("Riverfront Community", "RVR01")
        val household = householdRepo.createHousehold("The Taylors")
        assertNull(household.societyId)

        societyRepo.assignHousehold(household.id, society.id)

        val assignedSoc = societyRepo.getSocietyForHousehold(household.id)
        assertNotNull(assignedSoc)
        assertEquals(society.id, assignedSoc.id)
        assertEquals("Riverfront Community", assignedSoc.name)
    }
}
