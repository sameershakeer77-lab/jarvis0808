package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiffChange
import com.example.data.model.DiffLineType
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisError
import com.example.ui.theme.JarvisSuccess
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceCard
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary

@Composable
fun CodeDiffViewer(
  diff: DiffChange,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("code_diff_viewer"),
    colors = CardDefaults.cardColors(containerColor = JarvisSurfaceCard),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "CODE CHANGE DIFF",
            color = JarvisCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
          )
          Text(
            text = diff.filePath,
            color = JarvisTextPrimary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "+${diff.additionsCount}",
            color = JarvisSuccess,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "-${diff.deletionsCount}",
            color = JarvisError,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.width(8.dp))
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close Diff",
              tint = JarvisTextMuted,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(260.dp)
          .border(1.dp, JarvisBorder, RoundedCornerShape(8.dp))
          .background(JarvisSurface)
      ) {
        LazyColumn(modifier = Modifier.padding(6.dp)) {
          items(diff.lines) { line ->
            val bg = when (line.type) {
              DiffLineType.ADDED -> JarvisSuccess.copy(alpha = 0.15f)
              DiffLineType.REMOVED -> JarvisError.copy(alpha = 0.15f)
              DiffLineType.UNCHANGED -> Color.Transparent
            }
            val textCol = when (line.type) {
              DiffLineType.ADDED -> JarvisSuccess
              DiffLineType.REMOVED -> JarvisError
              DiffLineType.UNCHANGED -> JarvisTextPrimary
            }
            val prefix = when (line.type) {
              DiffLineType.ADDED -> "+"
              DiffLineType.REMOVED -> "-"
              DiffLineType.UNCHANGED -> " "
            }

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(bg)
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 1.dp, horizontal = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = (line.newLineNumber ?: line.oldLineNumber ?: 0).toString().padStart(3, ' '),
                color = JarvisTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(28.dp)
              )
              Text(
                text = "$prefix ${line.text}",
                color = textCol,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }
    }
  }
}
