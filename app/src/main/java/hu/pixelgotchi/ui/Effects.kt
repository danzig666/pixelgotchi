package hu.pixelgotchi.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import hu.pixelgotchi.R
import hu.pixelgotchi.data.Preferences

class Effects(context: Context) {
    private val pool=SoundPool.Builder().setMaxStreams(2).setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build()
    private val sounds=listOf(R.raw.button,R.raw.happy,R.raw.attention,R.raw.evolution,R.raw.death).map { pool.load(context,it,1) }
    @Suppress("DEPRECATION") private val vibrator=context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    fun play(p: Preferences,event: Int=0) {
        if(p.sound)pool.play(sounds[event.coerceIn(0,4)],.35f,.35f,1,0,1f)
        if(p.vibration && vibrator.hasVibrator()) {
            if(Build.VERSION.SDK_INT>=26)vibrator.vibrate(VibrationEffect.createOneShot(25,VibrationEffect.DEFAULT_AMPLITUDE))
            else @Suppress("DEPRECATION") vibrator.vibrate(25)
        }
    }
    fun close()=pool.release()
}
