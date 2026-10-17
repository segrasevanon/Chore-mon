package com.choreemon.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDate

private fun <T> Iterable<T>.json(block: (T) -> JSONObject): JSONArray = JSONArray().also { out -> forEach { out.put(block(it)) } }
private fun List<String>.strings() = JSONArray(this)
private fun JSONArray.strings() = (0 until length()).map { getString(it) }
private fun <T> JSONArray.items(block: (JSONObject) -> T) = (0 until length()).map { block(getJSONObject(it)) }

object SaveCodec {
    const val VERSION = 1
    fun encode(s: GameState): String = JSONObject().apply {
        put("version", VERSION); put("household", s.household); put("inviteCode", s.inviteCode)
        put("onboarded", s.onboarded); put("activeMember", s.activeMember); put("activeCreature", s.activeCreature)
        put("week", s.week); put("pinSalt", s.pinSalt); put("pinHash", s.pinHash)
        put("claimedMilestones", s.claimedMilestones.toList().strings())
        put("preferences", JSONObject().put("sound", s.preferences.sound).put("haptics", s.preferences.haptics).put("reducedMotion", s.preferences.reducedMotion))
        put("members", s.members.json { JSONObject().put("id", it.id).put("name", it.name).put("avatar", it.avatar).put("points", it.points).put("weeklyPoints", it.weeklyPoints) })
        put("creatures", s.creatures.json { JSONObject().put("id", it.id).put("owner", it.owner).put("element", it.element.name).put("rarity", it.rarity.name).put("xp", it.xp).put("stage", it.stage).put("nickname", it.nickname).put("adopted", it.adopted).put("lovedOn", it.lovedOn).put("favorite", it.favorite) })
        put("chores", s.chores.json { JSONObject().put("id", it.id).put("title", it.title).put("description", it.description).put("points", it.points).put("element", it.element.name).put("assigned", it.assigned.strings()).put("steps", it.steps.strings()).put("schedule", it.schedule.name).put("approval", it.approval).put("archived", it.archived) })
        put("completions", s.completions.json { JSONObject().put("id", it.id).put("choreId", it.choreId).put("memberId", it.memberId).put("date", it.date).put("title", it.title).put("points", it.points).put("xp", it.xp).put("creatureId", it.creatureId).put("status", it.status.name).put("photo", it.photo) })
    }.toString()

    fun decode(text: String): GameState {
        val j = JSONObject(text)
        require(j.getInt("version") == VERSION) { "Unsupported save version" }
        val p = j.getJSONObject("preferences")
        val state = GameState(
            household = j.getString("household"), inviteCode = j.getString("inviteCode"), onboarded = j.getBoolean("onboarded"),
            activeMember = j.getString("activeMember"), activeCreature = j.getString("activeCreature"), week = j.getString("week"),
            pinSalt = j.getString("pinSalt"), pinHash = j.getString("pinHash"),
            preferences = Preferences(p.getBoolean("sound"), p.getBoolean("haptics"), p.getBoolean("reducedMotion")),
            claimedMilestones = j.getJSONArray("claimedMilestones").strings().toSet(),
            members = j.getJSONArray("members").items { Member(it.getString("id"), it.getString("name"), it.getInt("avatar"), it.getInt("points"), it.getInt("weeklyPoints")) },
            creatures = j.getJSONArray("creatures").items { Creature(it.getString("id"), it.getString("owner"), Element.valueOf(it.getString("element")), Rarity.valueOf(it.getString("rarity")), it.getInt("xp"), it.getInt("stage"), it.getString("nickname"), it.getString("adopted"), it.getString("lovedOn"), it.getBoolean("favorite")) },
            chores = j.getJSONArray("chores").items { Chore(it.getString("id"), it.getString("title"), it.getString("description"), it.getInt("points"), Element.valueOf(it.getString("element")), it.getJSONArray("assigned").strings(), it.getJSONArray("steps").strings(), Schedule.valueOf(it.getString("schedule")), it.getBoolean("approval"), it.getBoolean("archived")) },
            completions = j.getJSONArray("completions").items { Completion(it.getString("id"), it.getString("choreId"), it.getString("memberId"), it.getString("date"), it.getString("title"), it.getInt("points"), it.getInt("xp"), it.getString("creatureId"), CompletionStatus.valueOf(it.getString("status")), it.getString("photo")) }
        )
        require(state.members.isNotEmpty() && state.members.any { it.id == state.activeMember })
        require(state.members.all { m -> m.points >= 0 && state.creatures.any { it.owner == m.id } })
        return state
    }
}

object ParentPin {
    fun salt(): String = ByteArray(16).also { SecureRandom().nextBytes(it) }.joinToString("") { "%02x".format(it) }
    fun hash(pin: String, salt: String): String = MessageDigest.getInstance("SHA-256").digest("$salt:$pin".toByteArray()).joinToString("") { "%02x".format(it) }
    fun verify(pin: String, state: GameState): Boolean = MessageDigest.isEqual(hash(pin, state.pinSalt).toByteArray(), state.pinHash.toByteArray())
}

class GameRepository(context: Context) {
    private val prefs = context.getSharedPreferences("choreemon_household", Context.MODE_PRIVATE)
    var recovered = false
        private set
    fun read(): GameState {
        val saved = prefs.getString("state", null) ?: return demoState(LocalDate.now())
        return runCatching { SaveCodec.decode(saved) }.getOrElse {
            // Preserve the original rather than discarding a damaged save.
            prefs.edit().putString("recovery_${System.currentTimeMillis()}", saved).apply()
            recovered = true
            demoState(LocalDate.now())
        }
    }
    fun save(state: GameState) { prefs.edit().putString("state", SaveCodec.encode(state)).apply() }
}
