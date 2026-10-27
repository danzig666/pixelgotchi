package hu.pixelgotchi.data

import com.google.gson.GsonBuilder
import hu.pixelgotchi.engine.GameBalance

/** Explicit nulls preserve both unstarted profile slots and distinguish absence from damage. */
object SaveCodec {
    val gson=GsonBuilder().serializeNulls().create()
    fun decode(json: String): World {
        val w=gson.fromJson(json,World::class.java) ?: error("Unreadable save")
        require(w.format==1 && w.profiles.keys==setOf("Lili","Léna"))
        requireNotNull(w.clock);requireNotNull(w.preferences)
        require(w.preferences.colors.keys.containsAll(w.profiles.keys) && w.preferences.colors.values.all{it in 0..3})
        w.profiles.forEach { (key,p) -> if(p!=null) {
            require(p.profile==key && p.id.isNotEmpty() && p.name.isNotBlank() && p.name.length<=24)
            require(p.rulesVersion==GameBalance.VERSION && p.deficits.size==4)
            require(p.food in 0..4 && p.joy in 0..4 && p.healthUnits in 0..GameBalance.MAX_HEALTH)
            require(p.processedAt>=p.createdAt && (p.bornAt==0L || p.bornAt in p.createdAt..p.processedAt))
            require(p.diedAt==0L || (p.diedAt==p.processedAt && p.deathCause!=null))
            require(!p.alive || p.healthUnits>0)
            require(p.schedule.valid());java.time.ZoneId.of(p.schedule.zone)
            p.pendingSchedule?.let{require(it.schedule.valid());java.time.ZoneId.of(it.schedule.zone)}
        } }
        return w
    }
}
