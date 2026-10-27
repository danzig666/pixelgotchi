package hu.pixelgotchi

import android.app.Application
import hu.pixelgotchi.data.Reminders

class GameApplication: Application() {
    override fun onCreate() { super.onCreate();Reminders.schedule(this) }
}
