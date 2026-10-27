package hu.pixelgotchi.data

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import hu.pixelgotchi.MainActivity
import hu.pixelgotchi.R
import hu.pixelgotchi.engine.GameBalance
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException

object Reminders {
    const val CHANNEL="gondozas"
    fun schedule(context: Context) {
        if(Build.VERSION.SDK_INT>=26)for(sound in listOf(false,true))for(vibration in listOf(false,true)) {
            val channel=NotificationChannel(channel(sound,vibration),context.getString(R.string.notification_channel),NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableVibration(vibration)
            channel.setSound(if(sound)RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) else null,
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val work=PeriodicWorkRequestBuilder<ReminderWorker>(30,TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("care-reminders",ExistingPeriodicWorkPolicy.KEEP,work)
    }
    fun channel(sound: Boolean,vibration: Boolean)="${CHANNEL}_${if(sound)1 else 0}_${if(vibration)1 else 0}"
    fun allowed(context: Context) = Build.VERSION.SDK_INT<33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED
    fun cancel(context: Context,profile: String) { NotificationManagerCompat.from(context).cancel(profile.hashCode()) }
}
class ReminderWorker(context: Context,params: WorkerParameters): CoroutineWorker(context,params) {
    override suspend fun doWork(): Result = try {
        val context=applicationContext
        val repo=GameRepository.get(context)
        repo.transact { w,now ->
            w.profiles.forEach { (profile,p) ->
                val severity=p?.severity() ?: 0
                if(p==null || severity==0 || !w.preferences.notifications || !Reminders.allowed(context)) {
                    Reminders.cancel(context,profile)
                    if(p!=null && severity==0)p.noticeSeverity=0
                } else if(p.lastNotice==0L || now-p.lastNotice>=GameBalance.NOTIFICATION_GAP || severity>p.noticeSeverity) {
                    val reason=context.getString(when { p.sick -> R.string.notice_sick; p.food==0 -> R.string.notice_hungry; p.poop>0 -> R.string.notice_dirty; else -> R.string.notice_play })
                    val intent=Intent(context,MainActivity::class.java).putExtra("profile",profile).putExtra("petId",p.id)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    val pending=PendingIntent.getActivity(context,profile.hashCode(),intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    val notification=NotificationCompat.Builder(context,Reminders.channel(w.preferences.sound,w.preferences.vibration)).setSmallIcon(R.drawable.ic_pet)
                        .setContentTitle(context.getString(R.string.notice_title,profile,p.name)).setContentText(reason)
                        .setContentIntent(pending).setAutoCancel(true)
                        .setSound(if(w.preferences.sound)RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) else null).setVibrate(if(w.preferences.vibration)longArrayOf(0,60) else longArrayOf(0)).build()
                    try {
                        NotificationManagerCompat.from(context).notify(profile.hashCode(),notification)
                        p.lastNotice=now;p.noticeSeverity=severity
                    } catch (_: SecurityException) { /* Permission may be revoked between check and delivery. */ }
                }
            }
        }
        Result.success()
    } catch (cancelled: CancellationException) { throw cancelled } catch (_: Exception) { Result.retry() }
}
