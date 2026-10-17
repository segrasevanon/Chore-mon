package com.choreemon.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.choreemon.app.R
import com.choreemon.app.data.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable fun HomeScreen(state: GameState, onChore: (Chore) -> Unit, onQuests: () -> Unit, onHatch: () -> Unit, onPet: () -> Unit, onProfiles: () -> Unit, onClaim: () -> Unit) {
    val day = LocalDate.now()
    val quests = GameRules.available(state, day)
    val streak = GameRules.streak(state, state.activeMember, day)
    val count = state.completions.count { it.memberId == state.activeMember && it.status == CompletionStatus.APPROVED }
    val milestone = count / 10
    val claimable = milestone > 0 && "${state.activeMember}:$milestone" !in state.claimedMilestones
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(17.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f).clip(RoundedCornerShape(15.dp)).clickable(onClickLabel = "Switch adventurer", onClick = onProfiles), verticalAlignment = Alignment.CenterVertically) {
                Avatar(state.member, size = 49.dp)
                Spacer(Modifier.width(9.dp))
                Column {
                    Text("Hi, ${state.member.name}!", style = MaterialTheme.typography.titleLarge)
                    Text("Let's make a little magic.", style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Pill("${state.member.points}", Color(0xFFB17604), Color(0xFFFFF1CA), Icons.Rounded.Star)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocalFireDepartment, null, Modifier.size(15.dp), tint = Color(0xFFFF865A))
                    Text("$streak day streak", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        BuddyRoom(state, onPet)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Today's quests", "View all", onQuests)
            if (quests.isEmpty()) EmptyState("You did the thing!", "All your quests are done. Your buddy is beaming.", Icons.Rounded.TaskAlt, "Visit the hatchery", onHatch)
            else quests.take(4).chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { ChoreTile(it, Modifier.weight(1f), onChore) }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        GameCard(Modifier.fillMaxWidth(), Color(0xFFFFF7E5), onHatch) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EggArt(Modifier.size(90.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(if (claimable) "Your quest gift is ready!" else "Tiny chores. New friends.", style = MaterialTheme.typography.titleMedium)
                    Text(if (claimable) "You earned a 25-point milestone gift." else "${10 - count % 10} more quests to your next gift!", style = MaterialTheme.typography.bodySmall)
                    XpBar(if (claimable) 1f else (count % 10) / 10f, color = Gold, track = Color(0xFFF0E6CC))
                    if (claimable) TextButton(onClaim, Modifier.heightIn(min = 40.dp), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Rounded.Redeem, null, Modifier.size(18.dp)); Spacer(Modifier.width(5.dp)); Text("Claim 25 points") }
                    else Text("${count % 10} / 10 quests   •   Explore eggs", style = MaterialTheme.typography.labelMedium, color = Color(0xFFB17F27))
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(bottom = 17.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Favorite, null, Modifier.size(13.dp), tint = Pink)
            Spacer(Modifier.width(5.dp)); Text("A brighter home, one little quest at a time.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable fun BuddyRoom(state: GameState, onPet: () -> Unit) {
    val pet = state.companion
    val mood = GameRules.mood(state, pet, LocalDate.now())
    val shape = RoundedCornerShape(27.dp)
    var greeting by remember(pet.id) { mutableIntStateOf(0) }
    val messages = when (mood) {
        Mood.HAPPY -> listOf("A little sparkle looks good on us!", "You're my favorite adventure buddy!", "Those bubbles? That's my happy dance!")
        Mood.CONTENT -> listOf("A little tidy-up would make my day.", "We make a pretty great team.")
        Mood.SAD -> listOf("Ready when you are, buddy.", "A tiny quest could brighten our room.")
    }
    Box(Modifier.fillMaxWidth().height(274.dp).shadow(9.dp, shape, ambientColor = Blue.copy(alpha = .14f), spotColor = Blue.copy(alpha = .14f)).clip(shape).border(2.dp, Color.White, shape)) {
        Image(painterResource(R.drawable.room), "Your creature's cozy room", Modifier.matchParentSize(), contentScale = ContentScale.Crop)
        Box(Modifier.matchParentSize().background(Brush.horizontalGradient(listOf(Color(0xD024555F), Color(0x2424555F), Color.Transparent))))
        Column(Modifier.align(Alignment.TopStart).padding(18.dp)) {
            Text("YOUR BUBBLE BUDDY".let { if (pet.element == Element.WATER) it else "YOUR LITTLE BUDDY" }, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = .8f))
            Spacer(Modifier.height(2.dp))
            Text(pet.name, style = MaterialTheme.typography.headlineLarge, color = Color.White)
            Text("Lv. ${pet.level}  •  ${pet.element.label}", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = .9f))
            Spacer(Modifier.height(9.dp))
            Pill(mood.label, if (mood == Mood.HAPPY) Color(0xFF157550) else Ink, if (mood == Mood.HAPPY) Color(0xFFCCF7DC) else Color(0xFFFFEBC4), Icons.Rounded.SentimentSatisfiedAlt)
        }
        Box(Modifier.align(Alignment.BottomStart).padding(start = 15.dp, bottom = 57.dp).width(133.dp).clip(RoundedCornerShape(15.dp, 15.dp, 15.dp, 3.dp)).background(Color.White.copy(alpha = .95f)).padding(11.dp)) {
            Text(messages[greeting % messages.size], style = MaterialTheme.typography.bodySmall, color = Ink, fontWeight = FontWeight.Bold)
        }
        PetArt(pet, Modifier.align(Alignment.BottomEnd).padding(end = 7.dp, bottom = 43.dp).size(205.dp), onPet = { greeting++; onPet() }, mood = mood)
        if (mood == Mood.SAD) {
            Row(Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 48.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Rounded.Inventory2, "Some tidying would help", Modifier.size(30.dp), tint = Color(0xFFB18970))
                Icon(Icons.Rounded.Checkroom, null, Modifier.size(30.dp), tint = Purple)
            }
        }
        Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(12.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xE6224664)).padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Bolt, null, Modifier.size(15.dp), tint = Color(0xFF8FE7FF))
            Spacer(Modifier.width(5.dp))
            XpBar((pet.xp % 40) / 40f, Modifier.weight(1f), Color(0xFF74DDFF), Color.White.copy(alpha = .2f))
            Spacer(Modifier.width(9.dp))
            Text("${pet.xp % 40}/40 XP", color = Color.White, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.width(8.dp)); Text("Lv. ${pet.level + 1}", color = Color(0xFFAEECFF), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable fun ChoreTile(chore: Chore, modifier: Modifier, onClick: (Chore) -> Unit) {
    GameCard(modifier, chore.element.pale, { onClick(chore) }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(39.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = .8f)), contentAlignment = Alignment.Center) {
                Icon(chore.element.choreIcon, null, Modifier.size(25.dp), tint = chore.element.color)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Star, null, Modifier.size(15.dp), tint = Color(0xFFF0AB24))
                Text("${chore.points}", style = MaterialTheme.typography.titleSmall)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(chore.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        Spacer(Modifier.height(3.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("${chore.steps.size} little steps", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.ArrowForward, null, Modifier.size(18.dp), tint = chore.element.color)
        }
    }
}

@Composable fun QuestsScreen(state: GameState, onChore: (Chore) -> Unit) {
    var filter by remember { mutableStateOf("To do") }
    val day = LocalDate.now()
    val all = state.chores.filter { GameRules.isDue(it, day) && state.activeMember in it.assigned }
    val rows = all.filter { chore ->
        val entry = GameRules.completionFor(state, chore, state.activeMember, day)
        when (filter) { "To do" -> entry == null; "In review" -> entry?.status == CompletionStatus.PENDING; else -> entry?.status == CompletionStatus.APPROVED }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        PageHeading(day.format(DateTimeFormatter.ofPattern("EEEE, MMM d")), "Small quests. Big impact.", "Every little bit helps your home and your buddy.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("To do", "In review", "Done").forEach { label -> FilterChip(filter == label, { filter = label }, { Text(label) }, leadingIcon = if (label == "Done") { { Icon(Icons.Rounded.TaskAlt, null, Modifier.size(17.dp)) } } else null) }
        }
        if (rows.isEmpty()) EmptyState(when (filter) { "To do" -> "All tidy for today!"; "In review" -> "Nothing waiting for review"; else -> "Your first win is waiting" }, when (filter) { "To do" -> "You've done your part. Come back for new quests tomorrow."; "In review" -> "Quests that need a grown-up's check will appear here."; else -> "Finish a quest and celebrate it here." }, Icons.Rounded.Checklist)
        rows.forEach { chore ->
            val entry = GameRules.completionFor(state, chore, state.activeMember, day)
            GameCard(Modifier.fillMaxWidth(), onClick = if (entry == null) { { onChore(chore) } } else null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(55.dp).clip(RoundedCornerShape(17.dp)).background(chore.element.pale), contentAlignment = Alignment.Center) { Icon(chore.element.choreIcon, null, Modifier.size(32.dp), tint = chore.element.color) }
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text(chore.title, style = MaterialTheme.typography.titleMedium)
                        Text("${chore.schedule.label} • ${chore.steps.size} steps", style = MaterialTheme.typography.bodySmall)
                        if (chore.approval) Text("Grown-up check", style = MaterialTheme.typography.labelSmall, color = Purple)
                    }
                    if (entry == null) Pill("${chore.points}", Color(0xFFAB750C), Color(0xFFFFF2CB), Icons.Rounded.Star)
                    else Icon(if (entry.status == CompletionStatus.PENDING) Icons.Rounded.Schedule else Icons.Rounded.CheckCircle, "${entry.status}", tint = if (entry.status == CompletionStatus.PENDING) Gold else Green)
                }
                if (entry == null) { Spacer(Modifier.height(15.dp)); GameButton("Let's do it", { onChore(chore) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.ArrowForward) }
                else { Spacer(Modifier.height(10.dp)); Text(if (entry.status == CompletionStatus.PENDING) "Your points are waiting for a grown-up's thumbs up." else "+${entry.points} points • +${entry.xp} XP earned. Nicely done!", style = MaterialTheme.typography.bodySmall) }
            }
        }
        GameCard(Modifier.fillMaxWidth(), Sky) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Lightbulb, null, Modifier.size(30.dp), tint = Gold)
                Spacer(Modifier.width(12.dp))
                Text("Big or tricky task? Team up with a grown-up. Helping counts!", style = MaterialTheme.typography.bodyMedium, color = Ink)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
