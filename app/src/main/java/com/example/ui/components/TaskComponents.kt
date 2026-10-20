package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StepStatus
import com.example.data.model.TaskExecutionState
import com.example.data.model.TaskPlan
import com.example.data.model.TaskStep
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisError
import com.example.ui.theme.JarvisSuccess
import com.example.ui.theme.JarvisSurfaceCard
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisWarning

@Composable
fun TaskExecutionCard(
  plan: TaskPlan,
  onViewDiff: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isExpanded by remember { mutableStateOf(true) }

  val completed = plan.steps.count { it.status == StepStatus.SUCCESS || it.status == StepStatus.SKIPPED }
  val total = plan.steps.size
  val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f

  val stateColor = Color(plan.factualState.colorHex)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("task_execution_card"),
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
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(JarvisCyan.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = plan.classification.displayName.uppercase(),
                color = JarvisCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            // Factual Status Badge
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(stateColor.copy(alpha = 0.2f))
                .border(1.dp, stateColor.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = plan.factualState.label.uppercase(),
                color = stateColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = plan.summary,
            color = JarvisTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "$completed/$total",
            color = JarvisCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = JarvisTextSecondary,
            modifier = Modifier
              .size(22.dp)
              .clickable { isExpanded = !isExpanded }
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
          .fillMaxWidth()
          .height(5.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = stateColor,
        trackColor = JarvisSurfaceVariant
      )

      AnimatedVisibility(visible = isExpanded) {
        Column(modifier = Modifier.padding(top = 12.dp)) {
          // Execution Steps List
          plan.steps.forEach { step ->
            TaskStepRow(step = step)
            Spacer(modifier = Modifier.height(6.dp))
          }

          // Factual Execution Evidence Panel
          val ev = plan.evidence
          if (ev.filesModified.isNotEmpty() || ev.filesCreated.isNotEmpty() || ev.testsExecuted > 0) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(JarvisSurfaceVariant)
                .border(1.dp, JarvisBorder, RoundedCornerShape(8.dp))
                .padding(8.dp)
            ) {
              Column {
                Text(
                  text = "VERIFIED EXECUTION EVIDENCE",
                  color = JarvisCyan,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text(
                    text = "Modified: ${ev.filesModified.size} · Created: ${ev.filesCreated.size}",
                    color = JarvisTextPrimary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                  )
                  Text(
                    text = "+${ev.diffAdditions} / -${ev.diffDeletions} lines",
                    color = JarvisSuccess,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text(
                    text = "Tests: ${ev.testsPassed}/${ev.testsExecuted} passed",
                    color = if (ev.testsFailed == 0) JarvisSuccess else JarvisError,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                  )
                  if (ev.repairAttemptsUsed > 0) {
                    Text(
                      text = "Auto-Repair: ${ev.repairAttemptsUsed} attempts",
                      color = JarvisWarning,
                      fontSize = 10.sp,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                }
                if (ev.checkpointId != null) {
                  Text(
                    text = "Snapshot: ${ev.checkpointId} (${if (ev.rollbackTriggered) "Rolled Back" else "Preserved"})",
                    color = JarvisTextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }
            }
          }

          if (plan.targetFiles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Target: ${plan.targetFiles.joinToString()}",
                color = JarvisTextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
              OutlinedButton(
                onClick = onViewDiff,
                modifier = Modifier
                  .height(30.dp)
                  .testTag("view_code_diff_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisCyan),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.5f)),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
              ) {
                Text("View Diff", fontSize = 11.sp)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun TaskStepRow(step: TaskStep) {
  val infiniteTransition = rememberInfiniteTransition(label = "step_sync")
  val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(animation = tween(1200, easing = LinearEasing)),
    label = "step_rotate"
  )

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(6.dp))
      .background(JarvisSurfaceVariant.copy(alpha = 0.5f))
      .padding(horizontal = 8.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    when (step.status) {
      StepStatus.PENDING -> {
        Icon(
          imageVector = Icons.Default.RadioButtonUnchecked,
          contentDescription = "Pending",
          tint = JarvisTextMuted,
          modifier = Modifier.size(16.dp)
        )
      }
      StepStatus.IN_PROGRESS -> {
        Icon(
          imageVector = Icons.Default.Sync,
          contentDescription = "In Progress",
          tint = JarvisCyan,
          modifier = Modifier
            .size(16.dp)
            .rotate(rotation)
        )
      }
      StepStatus.SUCCESS -> {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = "Success",
          tint = JarvisSuccess,
          modifier = Modifier.size(16.dp)
        )
      }
      StepStatus.FAILED -> {
        Icon(
          imageVector = Icons.Default.Error,
          contentDescription = "Failed",
          tint = JarvisError,
          modifier = Modifier.size(16.dp)
        )
      }
      StepStatus.SKIPPED -> {
        Box(
          modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(JarvisTextMuted.copy(alpha = 0.3f)),
          contentAlignment = Alignment.Center
        ) {
          Text("-", color = JarvisTextMuted, fontSize = 10.sp)
        }
      }
    }

    Spacer(modifier = Modifier.width(8.dp))

    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = step.phase.title,
          color = when (step.status) {
            StepStatus.IN_PROGRESS -> JarvisCyan
            StepStatus.SUCCESS -> JarvisSuccess
            StepStatus.FAILED -> JarvisError
            else -> JarvisTextSecondary
          },
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        if (step.toolName != null) {
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "[${step.toolName}]",
            color = JarvisTextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }
      Text(
        text = step.description,
        color = JarvisTextPrimary,
        fontSize = 11.sp,
        maxLines = 2
      )
      if (step.output != null && step.status == StepStatus.SUCCESS) {
        Text(
          text = step.output!!.take(120),
          color = JarvisTextMuted,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace
        )
      }
      if (step.error != null) {
        Text(
          text = "Error: ${step.error}",
          color = JarvisError,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}

@Composable
fun ApprovalDialog(
  plan: TaskPlan,
  onApprove: () -> Unit,
  onReject: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
      .testTag("approval_request_card"),
    colors = CardDefaults.cardColors(containerColor = JarvisSurfaceCard),
    shape = RoundedCornerShape(14.dp),
    border = androidx.compose.foundation.BorderStroke(1.5.dp, JarvisWarning)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Warning,
          contentDescription = "Warning",
          tint = JarvisWarning,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "OPERATOR AUTHORIZATION REQUIRED",
          color = JarvisWarning,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "Task: ${plan.userPrompt}",
        color = JarvisTextPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Classification: ${plan.classification.displayName} (${plan.riskLevel.label})",
        color = JarvisTextSecondary,
        fontSize = 12.sp
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = plan.approvalExplanation ?: "This operation makes sensitive environment, security, or external changes.",
        color = JarvisTextMuted,
        fontSize = 11.sp
      )

      Spacer(modifier = Modifier.height(16.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        OutlinedButton(
          onClick = onReject,
          modifier = Modifier.testTag("reject_plan_button"),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisError)
        ) {
          Text("Deny Execution")
        }
        Spacer(modifier = Modifier.width(10.dp))
        Button(
          onClick = onApprove,
          modifier = Modifier.testTag("approve_plan_button"),
          colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black)
        ) {
          Text("Authorize")
        }
      }
    }
  }
}
