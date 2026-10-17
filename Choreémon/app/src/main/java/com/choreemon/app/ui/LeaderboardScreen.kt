package com.choreemon.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.choreemon.app.data.*
import java.time.LocalDate

@Composable fun LeaderboardScreen(state: GameState) {
    var todayOnly by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    fun score(member: Member) = if (todayOnly) state.completions.filter { it.memberId == member.id && it.date == today.toString() && it.status == CompletionStatus.APPROVED }.sumOf { it.points } else member.weeklyPoints
    val ranking = state.members.sortedWith(compareByDescending<Member> { score(it) }.thenBy { it.name })
    val mostConsistent = state.members.maxByOrNull { GameRules.streak(state, it.id, today) }
    val mostChores = state.members.maxByOrNull { m -> state.completions.count { it.memberId == m.id && it.status == CompletionStatus.APPROVED && LocalDate.parse(it.date) >= weekOf(today) } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(17.dp)) {
        PageHeading("TEAMWORK MAKES A BRIGHTER HOME", "Household heroes", "A little friendly competition. A lot of helping.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(!todayOnly, { todayOnly = false }, { Text("This week") }, leadingIcon = { Icon(Icons.Rounded.DateRange, null, Modifier.size(17.dp)) })
            FilterChip(todayOnly, { todayOnly = true }, { Text("Today") })
        }
        GameCard(Modifier.fillMaxWidth(), Color(0xFFFFF7E4)) {
            Text(if (todayOnly) "Today's dream team" else "This week's dream team", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth().heightIn(min = 222.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                val podiumOrder = if (ranking.size >= 3) listOf(1, 0, 2) else ranking.indices.toList()
                podiumOrder.forEach { index ->
                    val member = ranking[index]
                    val color = listOf(Gold, Color(0xFF8DABDA), Color(0xFFECA177))[index]
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (index == 0) Icon(Icons.Rounded.WorkspacePremium, null, Modifier.size(30.dp), tint = Gold)
                        Avatar(member, size = if (index == 0) 67.dp else 55.dp)
                        Spacer(Modifier.height(9.dp))
                        Column(Modifier.fillMaxWidth().height(if (index == 0) 130.dp else if (index == 1) 106.dp else 94.dp).clip(RoundedCornerShape(17.dp, 17.dp, 7.dp, 7.dp)).background(color.copy(alpha = .28f)).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text("${index + 1}", style = MaterialTheme.typography.headlineMedium, color = if (index == 0) Color(0xFFBD860D) else Ink)
                            Text(member.name, style = MaterialTheme.typography.titleSmall)
                            Text("${score(member)} pts", style = MaterialTheme.typography.labelMedium, color = Muted)
                        }
                    }
                }
            }
        }
        SectionTitle(if (todayOnly) "Today's rankings" else "Weekly rankings")
        ranking.forEachIndexed { index, member ->
            GameCard(Modifier.fillMaxWidth(), if (member.id == state.activeMember) Sky else Color.White) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${index + 1}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(22.dp))
                    Avatar(member, size = 43.dp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(member.name + if (member.id == state.activeMember) " (you)" else "", style = MaterialTheme.typography.titleMedium)
                        val streak = GameRules.streak(state, member.id, today)
                        Text("$streak day streak", style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Rounded.Star, null, Modifier.size(18.dp), tint = Gold)
                    Spacer(Modifier.width(4.dp)); Text("${score(member)}", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (mostConsistent != null) GameCard(Modifier.weight(1f), Color(0xFFFFF2DC)) {
                Icon(Icons.Rounded.LocalFireDepartment, null, Modifier.size(29.dp), tint = Color(0xFFFA9766))
                Spacer(Modifier.height(9.dp)); Text("Streak star", style = MaterialTheme.typography.titleSmall)
                Text("${mostConsistent.name} • ${GameRules.streak(state, mostConsistent.id, today)} days", style = MaterialTheme.typography.bodySmall)
            }
            if (mostChores != null) GameCard(Modifier.weight(1f), Color(0xFFE7F8F2)) {
                Icon(Icons.Rounded.TaskAlt, null, Modifier.size(29.dp), tint = Green)
                Spacer(Modifier.height(9.dp)); Text("Helping hero", style = MaterialTheme.typography.titleSmall)
                Text("${mostChores.name} • ${state.completions.count { it.memberId == mostChores.id && it.status == CompletionStatus.APPROVED && LocalDate.parse(it.date) >= weekOf(today) }} quests", style = MaterialTheme.typography.bodySmall)
            }
        }
        Text("Weekly scores reset on Monday. Spending points never changes your ranking. Every helper is a hero.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
    }
}
