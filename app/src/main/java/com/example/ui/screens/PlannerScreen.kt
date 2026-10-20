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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.JarvisViewModel
import com.example.ui.components.TaskExecutionCard
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
fun PlannerScreen(
  viewModel: JarvisViewModel,
  modifier: Modifier = Modifier
) {
  val activePlan by viewModel.activePlan.collectAsStateWithLifecycle()
  val checkpoints by viewModel.checkpoints.collectAsStateWithLifecycle()
  val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(JarvisSurface)
      .padding(14.dp)
  ) {
    // 1. Active Task Plan
    if (activePlan != null) {
      item {
        Text(
          text = "ACTIVE TASK EXECUTION",
          color = JarvisCyan,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        TaskExecutionCard(
          plan = activePlan!!,
          onViewDiff = {}
        )
        Spacer(modifier = Modifier.height(16.dp))
      }
    }

    // 2. Recoverable Checkpoints & Rollback
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Restore,
          contentDescription = "Checkpoints",
          tint = JarvisCyan,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "RECOVERABLE SNAPSHOT CHECKPOINTS",
          color = JarvisTextPrimary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 0.5.sp
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
    }

    if (checkpoints.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = JarvisSurfaceCard),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text(
            text = "No checkpoints created yet. Auto-created before Phase 4 code modifications.",
            color = JarvisTextMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(12.dp)
          )
        }
        Spacer(modifier = Modifier.height(16.dp))
      }
    } else {
      items(checkpoints) { cp ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("checkpoint_item_${cp.checkpointId}"),
          colors = CardDefaults.cardColors(containerColor = JarvisSurfaceCard),
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = cp.checkpointId,
                color = JarvisCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = cp.description,
                color = JarvisTextPrimary,
                fontSize = 12.sp
              )
              Text(
                text = cp.snapshotSummary,
                color = JarvisTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
              )
            }
            OutlinedButton(
              onClick = { viewModel.restoreCheckpoint(cp.checkpointId) },
              modifier = Modifier.testTag("restore_checkpoint_${cp.checkpointId}"),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisWarning),
              border = androidx.compose.foundation.BorderStroke(1.dp, JarvisWarning.copy(alpha = 0.5f))
            ) {
              Text("Rollback", fontSize = 11.sp)
            }
          }
        }
      }
      item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    // 3. Tool Audit Trail
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = "Audit Log",
          tint = JarvisCyan,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "TOOL EXECUTION AUDIT TRAIL",
          color = JarvisTextPrimary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 0.5.sp
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
    }

    if (auditLogs.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = JarvisSurfaceCard),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text(
            text = "Audit log is empty. Tool executions are recorded to persistent storage.",
            color = JarvisTextMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(12.dp)
          )
        }
      }
    } else {
      items(auditLogs.take(20)) { log ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
          colors = CardDefaults.cardColors(containerColor = JarvisSurfaceCard),
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (log.success) Icons.Default.CheckCircle else Icons.Default.Error,
                  contentDescription = if (log.success) "Success" else "Failed",
                  tint = if (log.success) JarvisSuccess else JarvisError,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = log.toolName,
                  color = JarvisTextPrimary,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(JarvisBlue.copy(alpha = 0.2f))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "LEVEL ${log.permissionLevel}",
                  color = JarvisBlue,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Args: ${log.inputPayload.take(100)}",
              color = JarvisTextMuted,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
            if (log.outputPayload.isNotBlank()) {
              Text(
                text = "Output: ${log.outputPayload.take(120)}",
                color = JarvisTextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }
    }
  }
}
