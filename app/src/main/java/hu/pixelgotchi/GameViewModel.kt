package hu.pixelgotchi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import hu.pixelgotchi.data.*
import hu.pixelgotchi.engine.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class GameViewModel(application: Application): AndroidViewModel(application) {
    private val repo=GameRepository.get(application)
    val world=repo.state
    val error=MutableStateFlow(false)
    val feedback=MutableStateFlow<Feedback?>(null)
    val album=MutableStateFlow<List<Pet>>(emptyList())
    val busy=MutableStateFlow(false)
    init { refresh() }
    private fun run(block: suspend ()->Unit) { viewModelScope.launch {
        busy.value=true
        try { block();error.value=false } catch (_: Exception) { error.value=true } finally { busy.value=false }
    } }
    fun refresh()=run { repo.transact() }
    suspend fun tick() { try { repo.transact();error.value=false } catch (_: Exception) { error.value=true } }
    fun create(profile: String,name: String)=run { repo.create(profile,name);Reminders.cancel(getApplication(),profile) }
    fun act(p: Pet,action: Action)=run { feedback.value=repo.action(p.profile,p.id,action);if(world.value?.profiles?.get(p.profile)?.severity()==0)Reminders.cancel(getApplication(),p.profile) }
    fun guess(p: Pet,direction: Int)=run { feedback.value=repo.guess(p.profile,p.id,direction) }
    fun preferences(change: (Preferences)->Unit)=run { repo.transact { w,_->change(w.preferences) };if(world.value?.preferences?.notifications==false)listOf("Lili","Léna").forEach { Reminders.cancel(getApplication(),it) } }
    fun schedule(profile: String,schedule: Schedule)=run { repo.schedule(profile,schedule) }
    fun album(profile: String)=run { repo.transact();album.value=repo.album(profile) }
    fun erase()=run { repo.erase();listOf("Lili","Léna").forEach { Reminders.cancel(getApplication(),it) } }
}
