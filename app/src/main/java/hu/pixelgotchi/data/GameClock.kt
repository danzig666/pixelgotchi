package hu.pixelgotchi.data

import android.content.Context
import android.os.SystemClock
import android.provider.Settings

data class ClockAnchor(var logical: Long = 0, var elapsed: Long = 0, var boot: Long = -1)
data class ClockReading(val utc: Long, val elapsed: Long, val boot: Long)
object ClockMath {
    fun advance(anchor: ClockAnchor, r: ClockReading): Long {
        val now = if(anchor.logical == 0L) r.utc else if(anchor.boot == r.boot && r.elapsed >= anchor.elapsed)
            anchor.logical + r.elapsed - anchor.elapsed else maxOf(anchor.logical,r.utc)
        anchor.logical=now;anchor.elapsed=r.elapsed;anchor.boot=r.boot
        return now
    }
}
class GameClock(private val context: Context) {
    fun read() = ClockReading(System.currentTimeMillis(), SystemClock.elapsedRealtime(),
        Settings.Global.getInt(context.contentResolver,Settings.Global.BOOT_COUNT,0).toLong())
}
