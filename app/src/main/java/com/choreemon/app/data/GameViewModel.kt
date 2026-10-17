package com.choreemon.app.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.util.UUID

class GameViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = GameRepository(app)
    private val mutable = MutableStateFlow(GameRules.refresh(repository.read(), LocalDate.now()))
    val state = mutable.asStateFlow()
    val recovered get() = repository.recovered
    fun update(block: (GameState) -> GameState) {
        val updated = block(GameRules.refresh(mutable.value, LocalDate.now()))
        repository.save(updated)
        mutable.value = updated
    }
    fun refresh() = update { it }
    fun startDemo() = update {
        val salt = ParentPin.salt()
        it.copy(onboarded = true, pinSalt = salt, pinHash = ParentPin.hash("2468", salt))
    }
    fun complete(id: String, photo: String): ChoreReward {
        val (next, reward) = GameRules.complete(mutable.value, id, LocalDate.now(), photo)
        update { next }; return reward
    }
    fun hatch(kind: EggKind): HatchResult {
        val (next, result) = GameRules.hatch(mutable.value, kind, LocalDate.now())
        update { next }; return result
    }
    fun review(id: String, approve: Boolean) = update { GameRules.review(it, id, approve, LocalDate.now()) }
    fun switchMember(id: String) = update { GameRules.switchMember(it, id) }
    fun favorite(id: String) = update { s -> s.copy(creatures = s.creatures.map { if (it.id == id) it.copy(favorite = !it.favorite) else it }) }
    fun love(id: String) = update { s -> s.copy(creatures = s.creatures.map { if (it.id == id && it.owner == s.activeMember) it.copy(lovedOn = LocalDate.now().toString()) else it }) }
    fun name(id: String, name: String) = update { s -> s.copy(creatures = s.creatures.map { if (it.id == id && it.owner == s.activeMember) it.copy(nickname = name.trim().take(20)) else it }) }
    fun choose(id: String) = update { s -> require(s.team.any { it.id == id }); s.copy(activeCreature = id) }
    fun evolve(id: String) = update { GameRules.evolve(it, id) }
    fun claimGift() = update { GameRules.claimMilestone(it) }
    fun saveChore(chore: Chore) = update { s ->
        GameRules.validateChore(chore, s)
        s.copy(chores = if (s.chores.any { it.id == chore.id }) s.chores.map { if (it.id == chore.id) chore else it } else s.chores + chore)
    }
    fun archive(id: String) = update { s -> s.copy(chores = s.chores.map { if (it.id == id) it.copy(archived = !it.archived) else it }) }
    fun addMember(name: String) = update { s ->
        require(name.trim().length in 2..20) { "Use a name with 2–20 characters." }
        require(s.members.none { it.name.equals(name.trim(), true) }) { "That adventurer is already here." }
        require(s.members.size < 8) { "Your household can have up to 8 adventurers." }
        val id = UUID.randomUUID().toString()
        s.copy(members = s.members + Member(id, name.trim(), s.members.size % 4), creatures = s.creatures + Creature(UUID.randomUUID().toString(), id, Element.WATER, Rarity.COMMON, adopted = LocalDate.now().toString()))
    }
    fun createHousehold(household: String, child: String, pin: String) = update {
        require(household.trim().length in 2..32 && child.trim().length in 2..20) { "Add a household name and your first adventurer." }
        require(pin.length == 4 && pin.all(Char::isDigit)) { "Choose a four-digit parent PIN." }
        val salt = ParentPin.salt()
        val member = Member("member-1", child.trim(), 0)
        val pet = Creature("starter-1", member.id, Element.WATER, Rarity.COMMON, adopted = LocalDate.now().toString())
        val chores = demoState(LocalDate.now()).chores.map { it.copy(assigned = listOf(member.id)) }
        GameState(household = household.trim(), inviteCode = UUID.randomUUID().toString().take(6).uppercase(), onboarded = true,
            activeMember = member.id, activeCreature = pet.id, members = listOf(member), creatures = listOf(pet), chores = chores,
            pinSalt = salt, pinHash = ParentPin.hash(pin, salt))
    }
    fun changePin(pin: String) = update { s ->
        require(pin.length == 4 && pin.all(Char::isDigit)) { "Use exactly four digits." }
        val salt = ParentPin.salt(); s.copy(pinSalt = salt, pinHash = ParentPin.hash(pin, salt))
    }
    fun resetDemo() = update {
        val salt = ParentPin.salt()
        demoState(LocalDate.now()).copy(onboarded = true, pinSalt = salt, pinHash = ParentPin.hash("2468", salt))
    }
}
