package com.choreemon.app

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.choreemon.app.data.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import org.junit.Rule
import org.junit.rules.TestName
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class GameFlowTest {
    @get:Rule val compose = createEmptyComposeRule()
    @get:Rule val testName = TestName()
    private lateinit var scenario: ActivityScenario<MainActivity>
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before fun freshInstallState() {
        context.getSharedPreferences("choreemon_household", Context.MODE_PRIVATE).edit().clear().commit()
        scenario = ActivityScenario.launch(MainActivity::class.java)
        compose.waitForIdle()
    }

    @After fun closeActivity() {
        runCatching {
            val image = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            val file = java.io.File(context.getExternalFilesDir("qa"), "${testName.methodName}.png")
            file.outputStream().use { image?.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        }
        if (::scenario.isInitialized) scenario.close()
    }

    @Test fun choreToEggToSavedCompanion() {
        compose.onNodeWithText("Start playing").performScrollTo().performClick()
        compose.onNodeWithText("Hi, Mia!").assertExists()
        compose.onNodeWithText("Wash dishes").performClick()
        listOf("Rinse the dishes", "Add soap and wash", "Stack them neatly", "Wipe the counter").forEach { text -> compose.onNodeWithText(text).performScrollTo().performClick() }
        compose.onNodeWithText("Mark complete").performClick()
        compose.onNodeWithText("You did the thing!").assertExists()
        compose.onNodeWithText("+10").assertExists()
        compose.onNodeWithText("Awesome!").performClick()
        compose.onNodeWithText("130").assertExists()
        compose.onNodeWithText("Hatch", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Hatch for 50 points").performScrollTo().performClick()
        repeat(3) { compose.onNodeWithContentDescription("Hatch egg").performClick() }
        compose.waitUntil(10000) { compose.onAllNodesWithText("Adventure together").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Adventure together").performScrollTo().performClick()
        compose.onNodeWithText("3 buddies").assertExists()
        scenario.recreate()
        compose.onNodeWithText("3 buddies").assertExists()
        compose.onNodeWithText("Home", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Hi, Mia!").assertExists()
        compose.onNodeWithText("80").assertExists()
        compose.onNodeWithText("Buddies", useUnmergedTree = true).performClick()
        compose.onNodeWithText("3 buddies").assertExists()
    }

    @Test fun parentPinProtectsManagement() {
        compose.onNodeWithText("Start playing").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Settings").performClick()
        compose.onNodeWithText("Grown-up corner").performScrollTo().performClick()
        compose.onNodeWithText("Just for grown-ups").assertExists()
        compose.onNodeWithText("Parent PIN").performTextInput("0000")
        compose.onNodeWithText("Unlock").performClick()
        compose.onNodeWithText("That PIN didn't match. Try again.").assertExists()
        compose.onNodeWithText("Parent PIN").performTextInput("2468")
        compose.onNodeWithText("Unlock").performClick()
        compose.onNodeWithText("Household quests").performScrollTo().assertExists()
        compose.onNodeWithText("New quest").performClick()
        compose.onNodeWithText("Quest name").performTextInput("Water plants")
        compose.onNode(isToggleable()).performScrollTo().performClick()
        compose.onNodeWithText("Create quest").performScrollTo().performClick()
        compose.onNodeWithText("Water plants").performScrollTo().assertExists()
        compose.onNodeWithContentDescription("Back").performClick()
        compose.waitUntil(15000) { compose.onAllNodesWithText("Quests", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Quests", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Water plants").performScrollTo().performClick()
        listOf("Rinse the dishes", "Add soap and wash", "Stack them neatly", "Wipe the counter").forEach { text -> compose.onNodeWithText(text).performScrollTo().performClick() }
        compose.onNodeWithText("Send for a thumbs up").performClick()
        compose.onNodeWithText("Off for a thumbs up!").assertExists()
        compose.onNodeWithText("Got it!").performClick()
        assertEquals(120, GameRepository(context).read().member.points)
        compose.onNodeWithContentDescription("Settings").performClick()
        compose.onNodeWithText("Grown-up corner").performScrollTo().performClick()
        compose.onNodeWithText("Parent PIN").performTextInput("2468")
        compose.onNodeWithText("Unlock").performClick()
        compose.onNodeWithText("Approve").performScrollTo().performClick()
        assertEquals(130, GameRepository(context).read().member.points)
        compose.onNodeWithText("Approve").assertDoesNotExist()
    }

    @Test fun saveRoundtripPreservesEveryGameField() {
        val initial = demoState(LocalDate.now()).copy(onboarded = true, pinSalt = "salt", pinHash = ParentPin.hash("2468", "salt"), preferences = Preferences(true, false, true))
        val played = GameRules.complete(initial, "dishes", LocalDate.now(), "content://example/proof").first
        val decoded = SaveCodec.decode(SaveCodec.encode(played))
        assertEquals(played, decoded)
        assertTrue(ParentPin.verify("2468", decoded))
        assertFalse(ParentPin.verify("0000", decoded))
        val repository = GameRepository(context)
        repository.save(played)
        assertEquals(played, GameRepository(context).read())
    }

    @Test fun createRealHouseholdStartsWithFreeBuddyAndNoDemoHistory() {
        compose.onNodeWithText("Create our household").performScrollTo().performClick()
        compose.onNodeWithText("Household name").performTextInput("The Patel Family")
        compose.onNodeWithText("First adventurer's name").performTextInput("Sam")
        compose.onNodeWithText("Choose a four-digit parent PIN").performScrollTo().performTextInput("1234")
        compose.onNodeWithText("Repeat parent PIN").performScrollTo().performTextInput("1234")
        compose.onNodeWithText("Let our adventure begin").performScrollTo().performClick()
        compose.onNodeWithText("Hi, Sam!").assertExists()
        val saved = GameRepository(context).read()
        assertEquals("The Patel Family", saved.household)
        assertEquals(1, saved.team.size)
        assertEquals(0, saved.member.points)
        assertTrue(saved.completions.isEmpty())
    }
}
