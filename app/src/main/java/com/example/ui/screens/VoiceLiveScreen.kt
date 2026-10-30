package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.ai.ChatMessage
import com.example.data.ai.VoiceConversationManager
import com.example.data.ai.VoiceState
import com.example.ui.theme.AquaAccent
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.RoyalBlueBright

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceLiveScreen(
    voiceMessages: List<ChatMessage>,
    isAiGenerating: Boolean,
    onSendVoiceQuery: (query: String, onReply: (String) -> Unit) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
    }

    var textFallbackInput by remember { mutableStateOf("") }
    var voiceState by remember { mutableStateOf(VoiceState.IDLE) }
    var currentTranscript by remember { mutableStateOf("") }
    var latestReplyText by remember { mutableStateOf("") }

    var voiceManagerRef by remember { mutableStateOf<VoiceConversationManager?>(null) }
    val voiceManager = remember {
        val vm = VoiceConversationManager(
            context = context,
            onSpeechRecognized = { spokenText ->
                currentTranscript = spokenText
                voiceState = VoiceState.PROCESSING
                onSendVoiceQuery(spokenText) { reply ->
                    latestReplyText = reply
                    voiceState = VoiceState.SPEAKING
                    // Speak the response using real-time TTS
                    voiceManagerRef?.speak(reply) {
                        voiceState = VoiceState.IDLE
                    }
                }
            }
        )
        voiceManagerRef = vm
        vm
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.destroy()
        }
    }

    // Pulsing animation for active audio listening
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (voiceState == VoiceState.LISTENING) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val listState = rememberLazyListState()
    LaunchedEffect(voiceMessages.size) {
        if (voiceMessages.isNotEmpty()) {
            listState.animateScrollToItem(voiceMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF061526)) // Deep Night Blue Saas theme
            .testTag("voice_live_screen")
    ) {
        CenterAlignedTopAppBar(
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Gemini 3.8 Live",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Real-time Live Voice Conversation",
                        style = MaterialTheme.typography.labelSmall,
                        color = AquaAccent,
                        fontSize = 11.sp
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("voice_back_btn")) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = NavyPrimary)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Status Header
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = when (voiceState) {
                    VoiceState.LISTENING -> AquaAccent.copy(alpha = 0.2f)
                    VoiceState.PROCESSING -> RoyalBlueBright.copy(alpha = 0.2f)
                    VoiceState.SPEAKING -> EmeraldSuccess.copy(alpha = 0.2f)
                    else -> Color.White.copy(alpha = 0.1f)
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                when (voiceState) {
                                    VoiceState.LISTENING -> AquaAccent
                                    VoiceState.PROCESSING -> RoyalBlueBright
                                    VoiceState.SPEAKING -> EmeraldSuccess
                                    else -> Color.Gray
                                },
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (voiceState) {
                            VoiceState.LISTENING -> "Listening to your voice..."
                            VoiceState.PROCESSING -> "Gemini 3.8 Live is thinking..."
                            VoiceState.SPEAKING -> "Gemini is speaking..."
                            else -> "Live Voice Ready • Tap to Talk"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Glowing Animated Voice Orb
            Box(
                modifier = Modifier
                    .size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background halo ripple
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .scale(if (voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING) pulseScale else 1f)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    when (voiceState) {
                                        VoiceState.LISTENING -> AquaAccent.copy(alpha = 0.4f)
                                        VoiceState.SPEAKING -> EmeraldSuccess.copy(alpha = 0.4f)
                                        VoiceState.PROCESSING -> RoyalBlueBright.copy(alpha = 0.4f)
                                        else -> NavyPrimary.copy(alpha = 0.4f)
                                    },
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Central Mic Button
                Surface(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (!hasMicPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                return@clickable
                            }

                            if (voiceState == VoiceState.LISTENING) {
                                voiceManager.stopListening()
                                voiceState = VoiceState.IDLE
                            } else if (voiceState == VoiceState.SPEAKING) {
                                voiceManager.stopSpeaking()
                                voiceState = VoiceState.IDLE
                            } else {
                                currentTranscript = ""
                                voiceManager.startListening()
                                voiceState = VoiceState.LISTENING
                            }
                        }
                        .testTag("voice_mic_orb"),
                    shape = CircleShape,
                    color = when (voiceState) {
                        VoiceState.LISTENING -> AquaAccent
                        VoiceState.SPEAKING -> EmeraldSuccess
                        VoiceState.PROCESSING -> RoyalBlueBright
                        else -> NavyPrimary
                    },
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (voiceState) {
                                VoiceState.SPEAKING -> Icons.Default.VolumeUp
                                VoiceState.LISTENING -> Icons.Default.Mic
                                VoiceState.PROCESSING -> Icons.Default.Stop
                                else -> Icons.Default.Mic
                            },
                            contentDescription = "Microphone",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real-time transcript preview
            if (currentTranscript.isNotBlank()) {
                Text(
                    text = "\"$currentTranscript\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AquaAccent,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            } else {
                Text(
                    text = "Tap the glowing microphone to speak naturally.\nGemini 3.8 Live will answer with real-time speech.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Conversation history thread
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2238)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (voiceMessages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No voice exchanges yet.\nTry asking: 'What was my gross profit today?' or 'How much does Sita owe in Udhaar?'",
                            color = Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(voiceMessages) { msg ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (msg.role == com.example.data.ai.MessageRole.USER) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (msg.role == com.example.data.ai.MessageRole.USER) RoyalBlueBright else Color(0xFF1E3A5F),
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = msg.text,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick text input fallback bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textFallbackInput,
                    onValueChange = { textFallbackInput = it },
                    placeholder = { Text("Or type question for Live model...", fontSize = 12.sp, color = Color.Gray) },
                    modifier = Modifier.weight(1f).testTag("voice_text_fallback_input"),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (textFallbackInput.isNotBlank()) {
                            val text = textFallbackInput
                            textFallbackInput = ""
                            voiceState = VoiceState.PROCESSING
                            onSendVoiceQuery(text) { reply ->
                                latestReplyText = reply
                                voiceState = VoiceState.SPEAKING
                                voiceManager.speak(reply) {
                                    voiceState = VoiceState.IDLE
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .background(AquaAccent, CircleShape)
                        .testTag("voice_send_fallback_btn")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = NavyPrimary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
