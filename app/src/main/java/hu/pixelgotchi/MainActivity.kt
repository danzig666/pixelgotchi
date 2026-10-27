package hu.pixelgotchi

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import hu.pixelgotchi.ui.PixelgotchiApp

class MainActivity: ComponentActivity() {
    private var destination by mutableStateOf<Pair<String?,String?>>(null to null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);enableEdgeToEdge()
        destination=intent.getStringExtra("profile") to intent.getStringExtra("petId")
        setContent { PixelgotchiApp(viewModel(),destination) }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent);setIntent(intent)
        destination=intent.getStringExtra("profile") to intent.getStringExtra("petId")
    }
}
