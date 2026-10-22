package com.example.drumpads

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.drumpads.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var audioManager: PadAudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        audioManager = PadAudioManager(applicationContext)

        setContent {
            DrumPadsTheme {
                DrumPadsScreen(audioManager = audioManager)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioManager.release()
    }
}

@Composable
fun DrumPadsScreen(audioManager: PadAudioManager) {
    var pendingPadIndexForPicker by remember { mutableIntStateOf(-1) }
    val coroutineScope = rememberCoroutineScope()

    // File selection launcher for WAV, MP3, OGG, M4A, FLAC, AAC, etc.
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null && pendingPadIndexForPicker in 0..3) {
            val targetPad = pendingPadIndexForPicker
            coroutineScope.launch {
                audioManager.assignSample(targetPad, uri)
            }
        }
    }

    // 4 Distinct Pad Colors
    val padColors = listOf(PadRed, PadBlue, PadYellow, PadGreen)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Row 1: Pads 0 and 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                PadView(
                    index = 0,
                    baseColor = padColors[0],
                    audioManager = audioManager,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onLongPress = {
                        pendingPadIndexForPicker = 0
                        filePickerLauncher.launch(arrayOf("audio/*"))
                    }
                )
                Spacer(modifier = Modifier.width(4.dp))
                PadView(
                    index = 1,
                    baseColor = padColors[1],
                    audioManager = audioManager,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onLongPress = {
                        pendingPadIndexForPicker = 1
                        filePickerLauncher.launch(arrayOf("audio/*"))
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 2: Pads 2 and 3
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                PadView(
                    index = 2,
                    baseColor = padColors[2],
                    audioManager = audioManager,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onLongPress = {
                        pendingPadIndexForPicker = 2
                        filePickerLauncher.launch(arrayOf("audio/*"))
                    }
                )
                Spacer(modifier = Modifier.width(4.dp))
                PadView(
                    index = 3,
                    baseColor = padColors[3],
                    audioManager = audioManager,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onLongPress = {
                        pendingPadIndexForPicker = 3
                        filePickerLauncher.launch(arrayOf("audio/*"))
                    }
                )
            }
        }
    }
}

@Composable
fun PadView(
    index: Int,
    baseColor: Color,
    audioManager: PadAudioManager,
    modifier: Modifier = Modifier,
    onLongPress: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }
    var isAssignedFlash by remember { mutableStateOf(false) }

    // Visual feedback color transition:
    // Flashes bright white on press/tap, giving tactile live visual response
    val currentColor by animateColorAsState(
        targetValue = when {
            isAssignedFlash -> Color.White
            isPressed -> baseColor.copy(alpha = 0.55f)
            else -> baseColor
        },
        animationSpec = tween(durationMillis = 60),
        label = "PadColorAnimation"
    )

    Box(
        modifier = modifier
            .background(currentColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        val released = tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        // Triggers immediately on tap without blocking or opening file picker
                        audioManager.play(index)
                    },
                    onLongPress = {
                        // Visual pulse indicating sample assignment trigger
                        coroutineScope.launch {
                            isAssignedFlash = true
                            delay(180)
                            isAssignedFlash = false
                        }
                        onLongPress()
                    }
                )
            }
    )
}