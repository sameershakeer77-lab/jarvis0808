package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.AcceptanceTestOutcome
import com.example.ui.JarvisViewModel
import com.example.ui.components.AcceptanceTestCard
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
fun DiagnosticsScreen(
  viewModel: JarvisViewModel,
  modifier: Modifier = Modifier
) {
  val acceptanceTests by viewModel.acceptanceTests.collectAsStateWithLifecycle()
  val isRunningTests by viewModel.isRunningAcceptanceTests.collectAsStateWithLifecycle()
  val unitTests by viewModel.unitTestResults.collectAsStateWithLifecycle()

  val passedCount = acceptanceTests.count { it.outcome == AcceptanceTestOutcome.PASSED }
  val failedCount = acceptanceTests.count { it.outcome == AcceptanceTestOutcome.FAILED }
  val unavailCount = acceptanceTests.count { it.outcome == AcceptanceTestOutcome.UNAVAILABLE }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(JarvisSurface)
      .padding(14.dp)
  ) {
    // 1. Acceptance Test Header & Run Action
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JarvisSurfaceCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Science,
                  contentDescription = "Acceptance Tests",
                  tint = JarvisCyan,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "ACCEPTANCE TESTS (A TO K)",
                  color = JarvisTextPrimary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
              Text(
                text = "$passedCount Passed · $failedCount Failed · $unavailCount Unavailable",
                color = JarvisTextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
            }

            Row {
              Button(
                onClick = { viewModel.triggerSelfImprovement() },
                enabled = !isRunningTests,
                modifier = Modifier.testTag("trigger_self_improvement_button"),
                colors = ButtonDefaults.buttonColors(containerColor = JarvisBlue, contentColor = Color.White)
              ) {
                Text("⚡ Self-Improve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
              Spacer(modifier = Modifier.width(6.dp))
              Button(
                onClick = { viewModel.runAcceptanceTests() },
                enabled = !isRunningTests,
                modifier = Modifier.testTag("run_all_acceptance_tests_button"),
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black)
              ) {
                Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = "Run All",
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Run All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          if (isRunningTests) {
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
              modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
              color = JarvisCyan,
              trackColor = JarvisSurfaceVariant
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
    }

    // 2. Acceptance Test Items List
    items(acceptanceTests) { test ->
      AcceptanceTestCard(test = test)
      Spacer(modifier = Modifier.height(6.dp))
    }

    item { Spacer(modifier = Modifier.height(10.dp)) }

    // 3. Workspace Unit Test Assertion Breakdown
    if (unitTests.isNotEmpty()) {
      item {
        Text(
          text = "WORKSPACE UNIT TEST SUITES",
          color = JarvisCyan,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(6.dp))
      }
      items(unitTests) { t ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
          colors = CardDefaults.cardColors(containerColor = JarvisSurfaceCard),
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (t.passed) Icons.Default.CheckCircle else Icons.Default.Error,
              contentDescription = if (t.passed) "Passed" else "Failed",
              tint = if (t.passed) JarvisSuccess else JarvisError,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "${t.suiteName}.${t.testName} (${t.executionTimeMs}ms)",
                color = JarvisTextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = t.message,
                color = JarvisTextSecondary,
                fontSize = 10.sp
              )
            }
          }
        }
      }
      item { Spacer(modifier = Modifier.height(10.dp)) }
    }

    // 4. Platform Capability Matrix (Real vs Hardware-gated)
    item {
      Text(
        text = "SYSTEM CAPABILITY MATRIX",
        color = JarvisCyan,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Spacer(modifier = Modifier.height(6.dp))

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JarvisSurfaceCard),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          CapabilityRow("AI Conversation & Tool Calling", "Implemented & Operational via Gemini API / Local Brain", true)
          CapabilityRow("Physical Workspace File Sandbox", "Implemented & Real filesystem operations", true)
          CapabilityRow("AST Syntax & Bracket Verification", "Implemented & Genuine static analyzer", true)
          CapabilityRow("Pre-change Checkpoints & Rollback", "Implemented & Persistent file snapshot engine", true)
          CapabilityRow("Room Audit Log & Project Memory", "Implemented & SQLite SQLite Room persistence", true)
          CapabilityRow("On-Demand SpeechRecognizer & TTS", "Implemented & Android speech engine integration", true)
          CapabilityRow("Always-On Background Wake-Word", "Requires native Android Foreground Service with ongoing microphone sampling", false)
          CapabilityRow("Arbitrary OS Root Access", "Blocked by Android security sandbox boundaries", false)
        }
      }
    }
  }
}

@Composable
fun CapabilityRow(feature: String, status: String, isReal: Boolean) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(androidx.compose.foundation.shape.CircleShape)
        .background(if (isReal) JarvisSuccess else JarvisWarning)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Column {
      Text(
        text = feature,
        color = JarvisTextPrimary,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold
      )
      Text(
        text = status,
        color = JarvisTextMuted,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}
