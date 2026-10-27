package hu.pixelgotchi

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.graphics.asAndroidBitmap
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import hu.pixelgotchi.data.GameRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UiTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private fun capture(name: String) {
        val image=compose.onRoot().captureToImage().asAndroidBitmap()
        val directory=compose.activity.getExternalFilesDir("screenshots")!!;directory.mkdirs()
        FileOutputStream(File(directory,name+".png")).use { image.compress(Bitmap.CompressFormat.PNG,100,it) }
    }
    @Test fun profilesNamingCareControlsAndRotation() {
        runBlocking { GameRepository.get(compose.activity).erase() }
        compose.waitForIdle()
        compose.onNodeWithText("Lili",useUnmergedTree=true).performClick()
        compose.onNodeWithText("Pötyi").performClick()
        compose.onNodeWithText("Jöhet a tojás!").performClick()
        compose.waitUntil(10000) { GameRepository.get(compose.activity).state.value?.profiles?.get("Lili")!=null }
        compose.onNodeWithText("Értem").performClick()
        compose.onNodeWithText("Nemsokára kikelek!").assertExists()
        capture("device-portrait")
        val id=GameRepository.get(compose.activity).state.value!!.profiles["Lili"]!!.id
        compose.activityRule.scenario.recreate()
        compose.waitUntil(10000){compose.onAllNodesWithText("Nemsokára kikelek!").fetchSemanticsNodes().isNotEmpty()}
        assertEquals(id,GameRepository.get(compose.activity).state.value!!.profiles["Lili"]!!.id)
        compose.activity.requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        compose.waitForIdle()
        compose.onNodeWithText("PIXELGOTCHI").assertExists()
        capture("device-landscape")
        assertNull(GameRepository.get(compose.activity).state.value!!.profiles["Léna"])
    }
}
