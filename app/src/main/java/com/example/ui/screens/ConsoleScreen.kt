package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSender
import com.example.ui.JarvisViewModel
import com.example.ui.components.ApprovalDialog
import com.example.ui.components.CodeDiffViewer
import com.example.ui.components.JarvisArcReactor
import com.example.ui.components.TaskExecutionCard
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisError
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceCard
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisWarning

@Composable
fun ConsoleScreen(
  viewModel: JarvisViewModel,
  modifier: Modifier = Modifier
) {
  val executionStatus by viewModel.executionStatus.collectAsStateWithLifecycle()
  val activePlan by viewModel.activePlan.collectAsStateWithLifecycle()
  val messages by viewModel.messages.collectAsStateWithLifecycle()
  val pendingApproval by viewModel.pendingApprovalPlan.collectAsStateWithLifecycle()
  val recentDiff by viewModel.recentDiff.collectAsStateWithLifecycle()
  val isListening by viewModel.voiceManager.isListening.collectAsStateWithLifecycle()
  val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsStateWithLifecycle()
  val soundLevel by viewModel.voiceManager.soundLevel.collectAsStateWithLifecycle()
  val liveTranscript by viewModel.voiceManager.liveTranscript.collectAsStateWithLifecycle()
  val voiceError by viewModel.voiceManager.voiceError.collectAsStateWithLifecycle()
  val speechEnabled by viewModel.speechOutputEnabled.collectAsStateWithLifecycle()
  val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()

  var inputText by remember { mutableStateOf("") }
  var showDiffViewer by remember { mutableStateOf(false) }
  val listState = rememberLazyListState()

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(JarvisSurface)
      .padding(horizontal = 14.dp, vertical = 8.dp)
  ) {
    // HUD Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(Color(0xFF00E676))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "J.A.R.V.I.S.",
          color = JarvisCyan,
          fontSize = 15.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 1.5.sp,
          fontFamily = FontFamily.Monospace
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        val autonomyMode by viewModel.autonomyMode.collectAsStateWithLifecycle()
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (autonomyMode == com.example.data.model.AutonomyMode.HIGH_AUTONOMY) JarvisCyan.copy(alpha = 0.15f) else JarvisWarning.copy(alpha = 0.15f))
            .border(
              1.dp,
              if (autonomyMode == com.example.data.model.AutonomyMode.HIGH_AUTONOMY) JarvisCyan.copy(alpha = 0.6f) else JarvisWarning.copy(alpha = 0.6f),
              RoundedCornerShape(8.dp)
            )
            .clickable { viewModel.toggleAutonomyMode() }
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .testTag("toggle_autonomy_mode_chip")
        ) {
          Text(
            text = if (autonomyMode == com.example.data.model.AutonomyMode.HIGH_AUTONOMY) "HIGH AUTONOMY" else "GUIDED",
            color = if (autonomyMode == com.example.data.model.AutonomyMode.HIGH_AUTONOMY) JarvisCyan else JarvisWarning,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(JarvisSurfaceVariant)
            .border(1.dp, JarvisBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = selectedModel.uppercase(),
            color = JarvisTextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
        Spacer(modifier = Modifier.width(6.dp))
        IconButton(
          onClick = { viewModel.toggleSpeechOutput() },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = if (speechEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
            contentDescription = "Toggle Speech",
            tint = if (speechEnabled) JarvisCyan else JarvisTextMuted,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    // Scrollable Console Body
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      // 1. Central Arc Reactor
      item {
        JarvisArcReactor(
          status = executionStatus,
          isListening = isListening,
          isSpeaking = isSpeaking,
          soundLevel = soundLevel,
          onClick = { viewModel.toggleVoiceListening() },
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
        )
      }

      // Voice Error / Live Transcript Banner
      if (isListening && liveTranscript.isNotBlank()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(JarvisCyan.copy(alpha = 0.1f))
              .border(1.dp, JarvisCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
              .padding(8.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = "Live Audio",
                tint = JarvisCyan,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Listening: $liveTranscript",
                color = JarvisTextPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
        }
      }

      if (voiceError != null) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(JarvisWarning.copy(alpha = 0.1f))
              .border(1.dp, JarvisWarning.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
              .padding(8.dp)
          ) {
            Text(
              text = voiceError ?: "",
              color = JarvisWarning,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
        }
      }

      // Quick Suggestion Chips
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp)
        ) {
          QuickChip("⚡ Run self-improvement protocol") { viewModel.triggerSelfImprovement() }
          Spacer(modifier = Modifier.width(6.dp))
          QuickChip("Build vector memory module") { viewModel.submitUserPrompt("Build vector memory module for yourself") }
          Spacer(modifier = Modifier.width(6.dp))
          QuickChip("Build diagnostics monitor module") { viewModel.submitUserPrompt("Build diagnostics monitor module") }
          Spacer(modifier = Modifier.width(6.dp))
          QuickChip("Add speech-to-text to yourself") { viewModel.submitUserPrompt(it) }
          Spacer(modifier = Modifier.width(6.dp))
          QuickChip("Run Acceptance Tests A-K") { viewModel.runAcceptanceTests() }
          Spacer(modifier = Modifier.width(6.dp))
          QuickChip("Inspect project and find broken imports") { viewModel.submitUserPrompt(it) }
          Spacer(modifier = Modifier.width(6.dp))
          QuickChip("Create recovery checkpoint") { viewModel.createManualCheckpoint("Manual backup") }
        }
        Spacer(modifier = Modifier.height(6.dp))
      }

      // Pending Approval Card
      if (pendingApproval != null) {
        item {
          ApprovalDialog(
            plan = pendingApproval!!,
            onApprove = { viewModel.approvePendingPlan() },
            onReject = { viewModel.rejectPendingPlan() }
          )
          Spacer(modifier = Modifier.height(8.dp))
        }
      }

      // Active Plan Execution
      if (activePlan != null) {
        item {
          TaskExecutionCard(
            plan = activePlan!!,
            onViewDiff = { showDiffViewer = true },
            modifier = Modifier.padding(bottom = 8.dp)
          )
        }
      }

      // Code Diff Viewer
      if (showDiffViewer && recentDiff != null) {
        item {
          CodeDiffViewer(
            diff = recentDiff!!,
            onDismiss = { showDiffViewer = false },
            modifier = Modifier.padding(bottom = 8.dp)
          )
        }
      }

      // Messages Stream
      items(messages) { msg ->
        ChatMessageBubble(message = msg)
        Spacer(modifier = Modifier.height(6.dp))
      }
    }

    // Bottom Command Input
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = { viewModel.toggleVoiceListening() },
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(if (isListening) JarvisCyan else JarvisSurfaceCard)
          .border(1.dp, if (isListening) JarvisCyan else JarvisBorder, CircleShape)
          .testTag("voice_microphone_button")
      ) {
        Icon(
          imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
          contentDescription = if (isListening) "Stop Listening" else "Start Listening",
          tint = if (isListening) Color.Black else JarvisCyan,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      OutlinedTextField(
        value = inputText,
        onValueChange = { inputText = it },
        placeholder = { Text("Command JARVIS...", color = JarvisTextMuted, fontSize = 12.sp) },
        modifier = Modifier
          .weight(1f)
          .height(50.dp)
          .testTag("command_input_field"),
        textStyle = androidx.compose.ui.text.TextStyle(
          color = JarvisTextPrimary,
          fontSize = 12.sp,
          fontFamily = FontFamily.Monospace
        ),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = JarvisSurfaceCard,
          unfocusedContainerColor = JarvisSurfaceCard,
          focusedBorderColor = JarvisCyan,
          unfocusedBorderColor = JarvisBorder
        )
      )

      Spacer(modifier = Modifier.width(8.dp))

      IconButton(
        onClick = {
          if (inputText.isNotBlank()) {
            val text = inputText
            inputText = ""
            viewModel.submitUserPrompt(text)
          }
        },
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(JarvisCyan)
          .testTag("submit_command_button")
      ) {
        Icon(
          imageVector = Icons.Default.Send,
          contentDescription = "Submit Command",
          tint = Color.Black,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
fun QuickChip(text: String, onClick: (String) -> Unit) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(14.dp))
      .background(JarvisSurfaceCard)
      .border(1.dp, JarvisBorder, RoundedCornerShape(14.dp))
      .clickable { onClick(text) }
      .padding(horizontal = 10.dp, vertical = 6.dp)
  ) {
    Text(
      text = text,
      color = JarvisTextSecondary,
      fontSize = 10.sp,
      fontFamily = FontFamily.Monospace
    )
  }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
  val isUser = message.sender == ChatSender.USER
  val isSystem = message.sender == ChatSender.SYSTEM

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(if (isUser) 0.82f else 0.94f)
        .testTag("chat_message_${message.id.take(8)}"),
      colors = CardDefaults.cardColors(
        containerColor = when {
          isUser -> JarvisBlue.copy(alpha = 0.2f)
          isSystem -> JarvisSurfaceVariant
          else -> JarvisSurfaceCard
        }
      ),
      shape = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 12.dp,
        bottomStart = if (isUser) 12.dp else 2.dp,
        bottomEnd = if (isUser) 2.dp else 12.dp
      ),
      border = androidx.compose.foundation.BorderStroke(
        1.dp,
        when {
          isUser -> JarvisBlue.copy(alpha = 0.5f)
          isSystem -> JarvisBorder
          else -> JarvisCyan.copy(alpha = 0.35f)
        }
      )
    ) {
      Column(modifier = Modifier.padding(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = when (message.sender) {
              ChatSender.USER -> "OPERATOR"
              ChatSender.JARVIS -> "J.A.R.V.I.S."
              ChatSender.SYSTEM -> "SYSTEM LOG"
              ChatSender.TOOL -> "TOOL OUTPUT"
            },
            color = when (message.sender) {
              ChatSender.USER -> JarvisBlue
              ChatSender.JARVIS -> JarvisCyan
              ChatSender.SYSTEM -> JarvisWarning
              ChatSender.TOOL -> JarvisTextMuted
            },
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
          )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = message.text,
          color = JarvisTextPrimary,
          fontSize = 12.sp,
          lineHeight = 17.sp,
          fontFamily = if (isSystem) FontFamily.Monospace else FontFamily.Default
        )
      }
    }
  }
}
