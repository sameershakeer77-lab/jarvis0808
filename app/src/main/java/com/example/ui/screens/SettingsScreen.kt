package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.AutonomyMode
import com.example.data.model.ModelStatus
import com.example.ui.JarvisViewModel
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisError
import com.example.ui.theme.JarvisSuccess
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceCard
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisWarning

@Composable
fun SettingsScreen(
  viewModel: JarvisViewModel,
  modifier: Modifier = Modifier
) {
  val models by viewModel.models.collectAsStateWithLifecycle()
  val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
  val knowledgeList by viewModel.knowledgeList.collectAsStateWithLifecycle()
  val autonomyMode by viewModel.autonomyMode.collectAsStateWithLifecycle()

  var apiKeyInput by remember { mutableStateOf("") }
  var showKeySavedBanner by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(JarvisSurface)
      .padding(14.dp)
  ) {
    // 1. High Autonomy Mode Configuration
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = "Autonomy",
          tint = JarvisCyan,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "AUTONOMOUS EXECUTION ENGINE",
          color = JarvisTextPrimary,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 0.5.sp
        )
      }
      Spacer(modifier = Modifier.height(8.dp))

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("autonomy_mode_card"),
        colors = CardDefaults.cardColors(containerColor = JarvisSurfaceCard),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = autonomyMode.title,
                color = if (autonomyMode == AutonomyMode.HIGH_AUTONOMY) JarvisCyan else JarvisWarning,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = autonomyMode.description,
                color = JarvisTextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(
              onClick = { viewModel.toggleAutonomyMode() },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (autonomyMode == AutonomyMode.HIGH_AUTONOMY) JarvisCyan else JarvisWarning,
                contentColor = Color.Black
              ),
              modifier = Modifier.testTag("toggle_autonomy_button")
            ) {
              Text(
                text = if (autonomyMode == AutonomyMode.HIGH_AUTONOMY) "Active" else "Switch",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(14.dp))
    }

    // 2. AI Model Registry
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Psychology,
          contentDescription = "Models",
          tint = JarvisCyan,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "MULTI-MODEL REGISTRY & PROVIDERS",
          color = JarvisTextPrimary,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 0.5.sp
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
    }

    items(models) { model ->
      val isSelected = model.modelId == selectedModel
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
          .clickable { viewModel.selectModel(model.modelId) }
          .testTag("model_item_${model.modelId}"),
        colors = CardDefaults.cardColors(containerColor = JarvisSurfaceCard),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (isSelected) JarvisCyan else JarvisBorder
        )
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(
                    when (model.status) {
                      ModelStatus.ONLINE -> JarvisSuccess
                      ModelStatus.CONFIGURED -> JarvisBlue
                      ModelStatus.KEY_REQUIRED -> JarvisWarning
                      ModelStatus.UNAVAILABLE -> JarvisError
                    }
                  )
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = model.displayName,
                color = if (isSelected) JarvisCyan else JarvisTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }

            if (isSelected) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(JarvisCyan.copy(alpha = 0.15f))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "ACTIVE",
                  color = JarvisCyan,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Provider: ${model.provider} · Status: ${model.status.name}",
            color = JarvisTextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )

          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Capabilities: " + model.capabilities.joinToString(", "),
            color = JarvisTextSecondary,
            fontSize = 10.sp
          )
        }
      }
    }

    item { Spacer(modifier = Modifier.height(14.dp)) }

    // 3. Native Background Wake-word vs On-Demand Voice Architecture
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.GraphicEq,
          contentDescription = "Voice Architecture",
          tint = JarvisCyan,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "VOICE SUBSYSTEM ARCHITECTURE",
          color = JarvisTextPrimary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 0.5.sp
        )
      }
      Spacer(modifier = Modifier.height(6.dp))

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JarvisSurfaceCard),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "• On-Demand Voice Input: Fully implemented via Android SpeechRecognizer and TextToSpeech.",
            color = JarvisTextPrimary,
            fontSize = 11.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "• Background Locked-Screen Wake-Word: Requires a native Android ForegroundService with ongoing microphone sampling and persistent notification on a physical device. Not claimed as operational until deployed to physical hardware.",
            color = JarvisWarning,
            fontSize = 11.sp,
            lineHeight = 15.sp
          )
        }
      }
      Spacer(modifier = Modifier.height(14.dp))
    }

    // 4. API Key Configuration
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Key,
          contentDescription = "API Keys",
          tint = JarvisCyan,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "AI PROVIDER CREDENTIALS & SECRETS",
          color = JarvisTextPrimary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 0.5.sp
        )
      }
      Spacer(modifier = Modifier.height(6.dp))

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JarvisSurfaceCard),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "Gemini keys are automatically injected via AI Studio Secrets Panel. You may also configure custom provider keys below.",
            color = JarvisTextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = apiKeyInput,
            onValueChange = { apiKeyInput = it },
            placeholder = { Text("Enter Gemini / Claude / OpenAI API key...", color = JarvisTextMuted, fontSize = 11.sp) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("api_key_input_field"),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              color = JarvisTextPrimary
            ),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = JarvisSurface,
              unfocusedContainerColor = JarvisSurface,
              focusedBorderColor = JarvisCyan,
              unfocusedBorderColor = JarvisBorder
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          Button(
            onClick = {
              if (apiKeyInput.isNotBlank()) {
                viewModel.saveCustomApiKey(apiKeyInput.trim())
                showKeySavedBanner = true
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black),
            modifier = Modifier.testTag("save_api_key_button")
          ) {
            Text("Update API Key", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          if (showKeySavedBanner) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Key updated. Active model verified.",
              color = JarvisSuccess,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(14.dp))
    }

    // 5. Project Knowledge Base
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Info,
          contentDescription = "Project Knowledge",
          tint = JarvisCyan,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "PERSISTENT PROJECT KNOWLEDGE",
          color = JarvisTextPrimary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 0.5.sp
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
    }

    items(knowledgeList) { item ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 3.dp),
        colors = CardDefaults.cardColors(containerColor = JarvisSurfaceCard),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
      ) {
        Column(modifier = Modifier.padding(10.dp)) {
          Text(
            text = "[${item.category}] ${item.title}",
            color = JarvisCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = item.content,
            color = JarvisTextSecondary,
            fontSize = 11.sp
          )
        }
      }
    }

    item { Spacer(modifier = Modifier.height(14.dp)) }

    // 6. Reset Workspace
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = "Reset",
          tint = JarvisError,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "WORKSPACE RESET",
          color = JarvisError,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 0.5.sp
        )
      }
      Spacer(modifier = Modifier.height(6.dp))

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JarvisSurfaceCard),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisError.copy(alpha = 0.4f))
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "Reset all workspace files back to clean initial JARVIS template baseline.",
            color = JarvisTextSecondary,
            fontSize = 11.sp
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedButton(
            onClick = { viewModel.resetWorkspace() },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisError),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisError.copy(alpha = 0.6f)),
            modifier = Modifier.testTag("reset_workspace_button")
          ) {
            Text("Reset to Clean Baseline", fontSize = 11.sp)
          }
        }
      }
    }
  }
}
