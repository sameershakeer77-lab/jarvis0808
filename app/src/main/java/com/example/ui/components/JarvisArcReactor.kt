package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExecutionStatus
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisError
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisWarning
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisArcReactor(
  status: ExecutionStatus,
  isListening: Boolean,
  isSpeaking: Boolean,
  soundLevel: Float,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "arc_reactor")

  val rotationOuter by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = if (status != ExecutionStatus.IDLE) 4000 else 12000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotation_outer"
  )

  val rotationInner by infiniteTransition.animateFloat(
    initialValue = 360f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = if (status != ExecutionStatus.IDLE) 3000 else 9000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotation_inner"
  )

  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.92f,
    targetValue = 1.08f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  val coreColor = when (status) {
    ExecutionStatus.IDLE -> if (isListening) JarvisCyan else JarvisBlue
    ExecutionStatus.ANALYZING, ExecutionStatus.PLANNING -> JarvisCyan
    ExecutionStatus.EXECUTING, ExecutionStatus.TESTING -> JarvisCyan
    ExecutionStatus.REPAIRING -> JarvisWarning
    ExecutionStatus.AWAITING_APPROVAL -> JarvisWarning
    ExecutionStatus.COMPLETE -> Color(0xFF00E676)
    ExecutionStatus.FAILED -> JarvisError
  }

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(140.dp)
        .testTag("jarvis_arc_reactor")
        .clickable(onClick = onClick),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.size(136.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.width / 2f - 4.dp.toPx()

        // Outer glow aura
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(coreColor.copy(alpha = 0.25f), Color.Transparent),
            center = center,
            radius = maxRadius * 1.15f
          ),
          center = center,
          radius = maxRadius * 1.15f
        )

        // Outer notched orbit ring
        val outerRadius = maxRadius * 0.95f
        drawCircle(
          color = coreColor.copy(alpha = 0.25f),
          radius = outerRadius,
          center = center,
          style = Stroke(width = 1.5.dp.toPx())
        )

        // Outer tick marks
        val ticks = 16
        for (i in 0 until ticks) {
          val angle = Math.toRadians((i * (360f / ticks) + rotationOuter).toDouble())
          val start = Offset(
            x = center.x + (outerRadius - 6.dp.toPx()) * cos(angle).toFloat(),
            y = center.y + (outerRadius - 6.dp.toPx()) * sin(angle).toFloat()
          )
          val end = Offset(
            x = center.x + outerRadius * cos(angle).toFloat(),
            y = center.y + outerRadius * sin(angle).toFloat()
          )
          drawLine(
            color = coreColor.copy(alpha = if (i % 4 == 0) 0.9f else 0.4f),
            start = start,
            end = end,
            strokeWidth = if (i % 4 == 0) 2.dp.toPx() else 1.dp.toPx(),
            cap = StrokeCap.Round
          )
        }

        // Inner counter-rotating ring
        val innerRadius = maxRadius * 0.72f
        drawCircle(
          color = coreColor.copy(alpha = 0.35f),
          radius = innerRadius,
          center = center,
          style = Stroke(width = 2.dp.toPx())
        )

        // Rotating segment arcs
        for (i in 0 until 4) {
          val startAngle = rotationInner + i * 90f + 10f
          drawArc(
            color = coreColor.copy(alpha = 0.85f),
            startAngle = startAngle,
            sweepAngle = 70f,
            useCenter = false,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
          )
        }

        // Central core sphere with reactive audio level pulsing
        val corePulse = if (isListening) 1f + soundLevel * 0.3f else pulseScale
        val coreRadius = (maxRadius * 0.42f) * corePulse

        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color.White,
              coreColor,
              coreColor.copy(alpha = 0.6f),
              Color.Transparent
            ),
            center = center,
            radius = coreRadius
          ),
          radius = coreRadius,
          center = center
        )

        // Center dot
        drawCircle(
          color = Color.White,
          radius = 3.5.dp.toPx(),
          center = center
        )
      }

      // Center icon overlay if speaking or listening
      if (isListening) {
        Icon(
          imageVector = Icons.Default.Mic,
          contentDescription = "Listening",
          tint = Color.White,
          modifier = Modifier.size(24.dp)
        )
      } else if (isSpeaking) {
        Icon(
          imageVector = Icons.Default.VolumeUp,
          contentDescription = "Speaking",
          tint = Color.White,
          modifier = Modifier.size(24.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Status pill
    Row(
      modifier = Modifier
        .clip(RoundedCornerShape(12.dp))
        .background(JarvisSurfaceVariant)
        .border(1.dp, coreColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        .padding(horizontal = 12.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(coreColor)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = if (isListening) "VOICE INPUT ACTIVE" else status.label,
        color = JarvisTextPrimary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 0.8.sp
      )
    }
  }
}
