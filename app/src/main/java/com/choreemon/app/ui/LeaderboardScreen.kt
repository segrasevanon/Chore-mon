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

enum class LeaderboardScope(val label: String) {
    HOUSEHOLD("Household"), SOCIETY("Society")
}

enum class SocietyTab(val label: String) {
    HOUSEHOLDS("Households"), ADVENTURERS("Adventurers")
}

data class SampleSocietyHousehold(
    val rank: Int,
    val name: String,
    val weeklyPoints: Int,
    val memberCount: Int,
    val isCurrent: Boolean = false
)

data class SampleSocietyAdventurer(
    val rank: Int,
    val name: String,
    val householdName: String,
    val avatar: Int,
    val weeklyPoints: Int,
    val companionName: String,
    val companionElement: Element,
    val isCurrent: Boolean = false
)

@Composable fun LeaderboardScreen(state: GameState) {
    var scope by remember { mutableStateOf(LeaderboardScope.HOUSEHOLD) }
    var todayOnly by remember { mutableStateOf(false) }
    var societySubTab by remember { mutableStateOf(SocietyTab.HOUSEHOLDS) }

    val today = LocalDate.now()
    fun score(member: Member) = if (todayOnly) state.completions.filter { it.memberId == member.id && it.date == today.toString() && it.status == CompletionStatus.APPROVED }.sumOf { it.points } else member.weeklyPoints
    val ranking = state.members.sortedWith(compareByDescending<Member> { score(it) }.thenBy { it.name })
    val mostConsistent = state.members.maxByOrNull { GameRules.streak(state, it.id, today) }
    val mostChores = state.members.maxByOrNull { m -> state.completions.count { it.memberId == m.id && it.status == CompletionStatus.APPROVED && LocalDate.parse(it.date) >= weekOf(today) } }

    // Society Rankings (integrated with current household score)
    val householdWeeklyTotal = state.members.sumOf { it.weeklyPoints }
    val societyHouseholds = remember(householdWeeklyTotal, state.household) {
        listOf(
            SampleSocietyHousehold(1, "The Miller Family", 1020, 3),
            SampleSocietyHousehold(2, state.household, maxOf(householdWeeklyTotal, 890), state.members.size, isCurrent = true),
            SampleSocietyHousehold(3, "The Patel Family", 760, 4),
            SampleSocietyHousehold(4, "The Tanaka Family", 610, 2),
            SampleSocietyHousehold(5, "The Garcia Family", 540, 3),
            SampleSocietyHousehold(6, "The Andersson Family", 420, 2),
            SampleSocietyHousehold(7, "The Dubois Family", 380, 3),
            SampleSocietyHousehold(8, "The Kim Family", 290, 2)
        ).sortedByDescending { it.weeklyPoints }.mapIndexed { index, item -> item.copy(rank = index + 1) }
    }

    val societyAdventurers = remember(state.members, state.activeMember) {
        val currentMember = state.member
        listOf(
            SampleSocietyAdventurer(1, "Sam", "The Miller Family", 2, 450, "Whirlpup", Element.AIR),
            SampleSocietyAdventurer(2, currentMember.name, state.household, currentMember.avatar, maxOf(currentMember.weeklyPoints, 320), state.companion.name, state.companion.element, isCurrent = true),
            SampleSocietyAdventurer(3, "Leo", state.household, 1, 245, "Sockmite", Element.EARTH),
            SampleSocietyAdventurer(4, "Aarav", "The Patel Family", 3, 230, "Stinkle", Element.POISON),
            SampleSocietyAdventurer(5, "Chloe", "The Miller Family", 0, 215, "Bubblescale", Element.WATER),
            SampleSocietyAdventurer(6, "Kenji", "The Tanaka Family", 1, 195, "Sockgolem", Element.EARTH),
            SampleSocietyAdventurer(7, "Sofia", "The Garcia Family", 2, 180, "Sudsy", Element.WATER),
            SampleSocietyAdventurer(8, "Ava", state.household, 2, 190, "Dustbunny", Element.AIR)
        ).sortedByDescending { it.weeklyPoints }.mapIndexed { index, item -> item.copy(rank = index + 1) }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(17.dp)) {
        // Page Heading
        PageHeading(
            if (scope == LeaderboardScope.HOUSEHOLD) "TEAMWORK MAKES A BRIGHTER HOME" else "COMMUNITY CHALLENGE • AZURE SQL",
            if (scope == LeaderboardScope.HOUSEHOLD) "Household heroes" else "Society leaderboard",
            if (scope == LeaderboardScope.HOUSEHOLD) "A little friendly competition. A lot of helping." else "Compete together with neighboring households across Sunshine Valley."
        )

        // Scope Switcher (Household vs Society)
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE4ECF6), RoundedCornerShape(18.dp))
                .padding(4.dp)
        ) {
            LeaderboardScope.entries.forEach { item ->
                val selected = scope == item
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                        .background(if (selected) Blue else Color.Transparent)
                        .clickable { scope = item }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            if (item == LeaderboardScope.HOUSEHOLD) Icons.Rounded.Home else Icons.Rounded.Apartment,
                            null,
                            Modifier.size(16.dp),
                            tint = if (selected) Color.White else Muted
                        )
                        Text(
                            item.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) Color.White else Muted
                        )
                    }
                }
            }
        }

        if (scope == LeaderboardScope.HOUSEHOLD) {
            // Household Leaderboard Content
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
        } else {
            // Society Leaderboard Content
            GameCard(Modifier.fillMaxWidth(), Color(0xFFF3EBFF)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color.White), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Apartment, null, Modifier.size(28.dp), tint = Purple)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Sunshine Valley Community", style = MaterialTheme.typography.titleMedium)
                        Text("8 Households • Code: SUNVALLEY", style = MaterialTheme.typography.bodySmall)
                    }
                    Pill("Ranked #2", Purple, Color.White, Icons.Rounded.WorkspacePremium)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    societySubTab == SocietyTab.HOUSEHOLDS,
                    { societySubTab = SocietyTab.HOUSEHOLDS },
                    { Text("Households") },
                    leadingIcon = { Icon(Icons.Rounded.HolidayVillage, null, Modifier.size(17.dp)) }
                )
                FilterChip(
                    societySubTab == SocietyTab.ADVENTURERS,
                    { societySubTab = SocietyTab.ADVENTURERS },
                    { Text("Society Adventurers") },
                    leadingIcon = { Icon(Icons.Rounded.Groups, null, Modifier.size(17.dp)) }
                )
            }

            if (societySubTab == SocietyTab.HOUSEHOLDS) {
                // Podium for Top 3 Households
                GameCard(Modifier.fillMaxWidth(), Color(0xFFFFF7E4)) {
                    Text("Top community households", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(Modifier.height(20.dp))
                    Row(Modifier.fillMaxWidth().heightIn(min = 210.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                        val podiumOrder = if (societyHouseholds.size >= 3) listOf(1, 0, 2) else societyHouseholds.indices.toList()
                        podiumOrder.forEach { index ->
                            val h = societyHouseholds[index]
                            val color = listOf(Gold, Color(0xFF8DABDA), Color(0xFFECA177))[index]
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                if (index == 0) Icon(Icons.Rounded.WorkspacePremium, null, Modifier.size(30.dp), tint = Gold)
                                Box(
                                    Modifier.size(if (index == 0) 62.dp else 50.dp).clip(CircleShape).background(color.copy(alpha = .18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Home, null, Modifier.size(if (index == 0) 32.dp else 24.dp), tint = if (index == 0) Gold else Ink)
                                }
                                Spacer(Modifier.height(8.dp))
                                Column(
                                    Modifier.fillMaxWidth().height(if (index == 0) 124.dp else if (index == 1) 102.dp else 90.dp)
                                        .clip(RoundedCornerShape(17.dp, 17.dp, 7.dp, 7.dp))
                                        .background(color.copy(alpha = .28f))
                                        .padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("${index + 1}", style = MaterialTheme.typography.headlineMedium, color = if (index == 0) Color(0xFFBD860D) else Ink)
                                    Text(h.name.replace("The ", "").replace(" Family", ""), style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                    Text("${h.weeklyPoints} pts", style = MaterialTheme.typography.labelMedium, color = Muted)
                                }
                            }
                        }
                    }
                }

                SectionTitle("Community households")
                societyHouseholds.forEach { h ->
                    GameCard(Modifier.fillMaxWidth(), if (h.isCurrent) Sky else Color.White) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${h.rank}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(24.dp))
                            Box(Modifier.size(40.dp).clip(CircleShape).background(if (h.isCurrent) Blue.copy(alpha = .15f) else Color(0xFFF0F4FA)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Home, null, Modifier.size(20.dp), tint = if (h.isCurrent) Blue else Muted)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(h.name + if (h.isCurrent) " (Your household)" else "", style = MaterialTheme.typography.titleMedium)
                                Text("${h.memberCount} adventurers • Active this week", style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(Icons.Rounded.Star, null, Modifier.size(18.dp), tint = Gold)
                            Spacer(Modifier.width(4.dp)); Text("${h.weeklyPoints}", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            } else {
                // Society Adventurers List
                SectionTitle("Top adventurers in society")
                societyAdventurers.forEach { adv ->
                    GameCard(Modifier.fillMaxWidth(), if (adv.isCurrent) Sky else Color.White) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${adv.rank}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(24.dp))
                            Box(Modifier.size(42.dp).clip(CircleShape).background(adv.companionElement.color.copy(alpha = .15f)), contentAlignment = Alignment.Center) {
                                Icon(when (adv.avatar % 4) { 0 -> Icons.Rounded.Face3; 1 -> Icons.Rounded.Face6; 2 -> Icons.Rounded.Face4; else -> Icons.Rounded.Face }, null, Modifier.size(26.dp), tint = adv.companionElement.color)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(adv.name + if (adv.isCurrent) " (You)" else "", style = MaterialTheme.typography.titleMedium)
                                Text("${adv.householdName} • ${adv.companionName}", style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(Icons.Rounded.Star, null, Modifier.size(18.dp), tint = Gold)
                            Spacer(Modifier.width(4.dp)); Text("${adv.weeklyPoints}", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            // Society Community Banner
            GameCard(Modifier.fillMaxWidth(), Color(0xFFE8F8F2)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Diversity1, null, Modifier.size(32.dp), tint = Green)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Connected via Azure SQL", style = MaterialTheme.typography.titleSmall, color = Green)
                        Text("3,450 points earned across the community this week. Every home shines brighter together!", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Text("Weekly scores reset on Monday. Spending points never changes your ranking. Every helper is a hero.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
    }
}
