package hu.pixelgotchi.data

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import hu.pixelgotchi.engine.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class Preferences(var sound: Boolean = true, var vibration: Boolean = true, var notifications: Boolean = false,
    var colors: MutableMap<String,Int> = mutableMapOf("Lili" to 0,"Léna" to 1),var introduced: Boolean = false)
data class World(val format: Int = 1, var clock: ClockAnchor = ClockAnchor(),
    var profiles: MutableMap<String,Pet?> = mutableMapOf("Lili" to null,"Léna" to null),var preferences: Preferences = Preferences())
class GameRepository private constructor(context: Context) {
    val database = Room.databaseBuilder(context,GameDatabase::class.java,"pixelgotchi.db")
        .addMigrations(GameDatabase.MIGRATION_1_2).build()
    private val clock=GameClock(context)
    private val gson=Gson()
    private val mutex=Mutex()
    private val engine=Simulator()
    private val mutable=MutableStateFlow<World?>(null)
    val state=mutable.asStateFlow()

    suspend fun transact(block: (World,Long)->Unit = {_,_->}): World = withContext(Dispatchers.IO) {
        mutex.withLock {
            var result: World?=null
            database.runInTransaction {
                val raw=database.game().read()
                val w=if(raw==null) World() else decode(raw.json)
                val now=ClockMath.advance(w.clock,clock.read())
                w.profiles.values.filterNotNull().forEach { engine.advance(it,now) }
                block(w,now)
                w.profiles.values.filterNotNull().filter { !it.alive }.forEach {
                    database.game().remember(Memory(it.id,it.profile,gson.toJson(it)))
                }
                database.game().save(Snapshot(json=gson.toJson(w)))
                result=w
            }
            result!!.also { mutable.value=it }
        }
    }
    private fun decode(json: String): World {
        val w=gson.fromJson(json,World::class.java) ?: error("Unreadable save")
        require(w.format==1 && w.profiles.keys==setOf("Lili","Léna"))
        w.profiles.forEach { (key,p) -> if(p!=null) { require(p.profile==key && p.id.isNotEmpty() && p.rulesVersion==GameBalance.VERSION && p.deficits.size==4);java.time.ZoneId.of(p.schedule.zone) } }
        return w
    }
    suspend fun create(profile: String,name: String) = transact { w,now ->
        require(profile in w.profiles && name.trim().isNotEmpty())
        val old=w.profiles[profile]
        require(old==null || !old.alive)
        if(old!=null)database.game().remember(Memory(old.id,old.profile,gson.toJson(old)))
        w.profiles[profile]=Pet(profile=profile,name=name.trim().take(24),createdAt=now,schedule=old?.schedule ?: Schedule())
    }
    suspend fun action(profile: String,id: String,action: Action): Feedback {
        var feedback=Feedback.DEAD
        transact { w,now -> w.profiles[profile]?.takeIf { it.id==id }?.let { feedback=engine.act(it,now,action) } }
        return feedback
    }
    suspend fun guess(profile: String,id: String,direction: Int): Feedback {
        var feedback=Feedback.DEAD
        transact { w,now -> w.profiles[profile]?.takeIf{it.id==id}?.let { feedback=engine.guess(it,now,direction) } }
        return feedback
    }
    suspend fun schedule(profile: String,schedule: Schedule) = transact { w,now ->
        w.profiles[profile]?.let { engine.changeSchedule(it,now,schedule) }
    }
    suspend fun album(profile: String): List<Pet> = withContext(Dispatchers.IO) {
        mutex.withLock { database.game().album(profile).map { gson.fromJson(it.json,Pet::class.java) } }
    }
    suspend fun erase() = withContext(Dispatchers.IO) {
        mutex.withLock {
            database.runInTransaction { database.game().clearSnapshot();database.game().clearMemories() }
            mutable.value=null
        }
        transact()
    }
    companion object {
        @Volatile private var instance: GameRepository?=null
        fun get(context: Context) = instance ?: synchronized(this) {
            instance ?: GameRepository(context.applicationContext).also { instance=it }
        }
    }
}
