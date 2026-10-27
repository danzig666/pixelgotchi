package hu.pixelgotchi

import android.os.SystemClock
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import hu.pixelgotchi.data.GameRepository
import hu.pixelgotchi.engine.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.Instant
import java.time.ZoneId

/** Uses supported UiAutomation input, avoiding Espresso's removed InputManager reflection on API 37. */
@RunWith(AndroidJUnit4::class)
class UiTest {
    @get:Rule val activity=ActivityScenarioRule(MainActivity::class.java)
    private val instrumentation get()=InstrumentationRegistry.getInstrumentation()
    private val device get()=UiDevice.getInstance(instrumentation)
    private val repo get()=GameRepository.get(instrumentation.targetContext)
    private fun tap(selector: BySelector) {
        val node=device.wait(Until.findObject(selector),20_000)
        if(node==null) {
            capture("missing-control")
            val directory=instrumentation.targetContext.getExternalFilesDir("screenshots")!!
            device.dumpWindowHierarchy(File(directory,"missing-control.xml"))
        }
        assertNotNull("Missing control: $selector",node);node!!.click();device.waitForIdle()
    }
    private fun await(predicate: ()->Boolean) {
        val deadline=SystemClock.elapsedRealtime()+20_000
        while(!predicate() && SystemClock.elapsedRealtime()<deadline)SystemClock.sleep(100)
        assertTrue("State did not settle",predicate())
    }
    private fun capture(name: String) {
        val directory=instrumentation.targetContext.getExternalFilesDir("screenshots")!!;directory.mkdirs()
        assertTrue(device.takeScreenshot(File(directory,"$name.png")))
    }
    @Test fun profilesNamingCareControlsAndRotation() {
        runBlocking { repo.erase() }
        assertTrue(device.wait(Until.hasObject(By.descContains("Lili profilja")),20_000))
        capture("profiles")
        tap(By.descContains("Lili profilja"));tap(By.text("Pötyi"));tap(By.text("Jöhet a tojás!"))
        await { repo.state.value?.profiles?.get("Lili")!=null }
        tap(By.text("Értem"))
        val id=repo.state.value!!.profiles["Lili"]!!.id
        capture("egg-portrait")
        // Only the test APK injects historical timestamps; production always uses real time.
        runBlocking {
            repo.transact { w,now ->
                val minute=Instant.ofEpochMilli(now).atZone(ZoneId.of("UTC")).let{it.hour*60+it.minute}
                val bedtime=(minute+120)%1440
                w.profiles["Lili"]=w.profiles["Lili"]!!.copy(createdAt=now-3*GameBalance.HOUR-GameBalance.MINUTE,
                    bornAt=now-3*GameBalance.HOUR,processedAt=now-3*GameBalance.HOUR,stage=Stage.BABY,
                    schedule=Schedule(bedtime,(bedtime+600)%1440,"UTC"))
            }
            repo.transact()
        }
        tap(By.descContains("Étel"));tap(By.text("Rendes étel"))
        await { repo.state.value!!.profiles["Lili"]!!.food==4 }
        runBlocking { repo.transact { w,_->w.profiles["Lili"]!!.poop=1;w.profiles["Lili"]!!.sick=true } }
        tap(By.desc("Takarítás"));await{repo.state.value!!.profiles["Lili"]!!.poop==0}
        tap(By.desc("Gyógyszer"));await{!repo.state.value!!.profiles["Lili"]!!.sick}
        capture("device-portrait")
        activity.scenario.recreate()
        assertTrue(device.wait(Until.hasObject(By.desc("Pötyi pixelképe")),20_000))
        assertEquals(id,repo.state.value!!.profiles["Lili"]!!.id)
        device.setOrientationLeft();device.waitForIdle();capture("device-landscape")
        device.setOrientationNatural()
        assertNull(repo.state.value!!.profiles["Léna"])
    }
}
