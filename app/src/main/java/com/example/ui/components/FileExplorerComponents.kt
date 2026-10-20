package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.ProjectFile
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

@Composable
fun FileExplorerList(
  files: List<ProjectFile>,
  selectedFile: ProjectFile?,
  onSelectFile: (ProjectFile) -> Unit,
  onCreateFileClick: () -> Unit,
  onCheckpointClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
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
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = "Workspace",
            tint = JarvisCyan,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "WORKSPACE SANDBOX",
            color = JarvisTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }

        Row {
          IconButton(
            onClick = onCheckpointClick,
            modifier = Modifier
              .size(32.dp)
              .testTag("create_checkpoint_icon_button")
          ) {
            Icon(
              imageVector = Icons.Default.BookmarkBorder,
              contentDescription = "Snapshot Checkpoint",
              tint = JarvisCyan,
              modifier = Modifier.size(18.dp)
            )
          }
          IconButton(
            onClick = onCreateFileClick,
            modifier = Modifier
              .size(32.dp)
              .testTag("create_file_icon_button")
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "New File",
              tint = JarvisCyan,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      LazyColumn(modifier = Modifier.height(180.dp)) {
        items(files) { file ->
          val isSelected = selectedFile?.relativePath == file.relativePath
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(if (isSelected) JarvisCyan.copy(alpha = 0.15f) else Color.Transparent)
              .clickable { onSelectFile(file) }
              .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description,
              contentDescription = if (file.isDirectory) "Directory" else "File",
              tint = if (file.isDirectory) JarvisBlue else JarvisCyan,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = file.relativePath,
              color = if (isSelected) JarvisCyan else JarvisTextPrimary,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              modifier = Modifier.weight(1f)
            )
            if (!file.isDirectory) {
              Text(
                text = "${file.sizeBytes} B",
                color = JarvisTextMuted,
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

@Composable
fun FileEditorView(
  file: ProjectFile,
  onSave: (String) -> Unit,
  onDelete: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  var editedContent by remember(file.relativePath, file.content) { mutableStateOf(file.content) }
  var isModified by remember(editedContent, file.content) { mutableStateOf(editedContent != file.content) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("file_editor_view"),
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
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = file.name,
              color = JarvisTextPrimary,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            if (isModified) {
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "● Unsaved",
                color = JarvisCyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
          Text(
            text = file.relativePath,
            color = JarvisTextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )
        }

        Row {
          IconButton(
            onClick = { onSave(editedContent) },
            enabled = isModified,
            modifier = Modifier
              .size(32.dp)
              .testTag("save_file_button")
          ) {
            Icon(
              imageVector = Icons.Default.Save,
              contentDescription = "Save File",
              tint = if (isModified) JarvisCyan else JarvisTextMuted,
              modifier = Modifier.size(18.dp)
            )
          }
          IconButton(
            onClick = onDelete,
            modifier = Modifier
              .size(32.dp)
              .testTag("delete_file_button")
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete File",
              tint = JarvisError,
              modifier = Modifier.size(18.dp)
            )
          }
          IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close Editor",
              tint = JarvisTextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      OutlinedTextField(
        value = editedContent,
        onValueChange = { editedContent = it },
        modifier = Modifier
          .fillMaxWidth()
          .height(240.dp)
          .testTag("file_content_editor"),
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
    }
  }
}

@Composable
fun CreateFileDialog(
  onDismiss: () -> Unit,
  onConfirm: (path: String, content: String) -> Unit
) {
  var path by remember { mutableStateOf("src/main/kotlin/") }
  var content by remember { mutableStateOf("// New capability file\npackage com.jarvis\n\nclass CustomModule {\n}\n") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Create Workspace File",
        color = JarvisCyan,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
    },
    text = {
      Column {
        OutlinedTextField(
          value = path,
          onValueChange = { path = it },
          label = { Text("Relative Path") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = JarvisCyan,
            unfocusedBorderColor = JarvisBorder
          )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = content,
          onValueChange = { content = it },
          label = { Text("Initial Content") },
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = JarvisCyan,
            unfocusedBorderColor = JarvisBorder
          )
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (path.isNotBlank()) {
            onConfirm(path.trim(), content)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black)
      ) {
        Text("Create")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel", color = JarvisTextSecondary)
      }
    },
    containerColor = JarvisSurfaceCard
  )
}

@Composable
fun CreateCheckpointDialog(
  onDismiss: () -> Unit,
  onConfirm: (desc: String) -> Unit
) {
  var description by remember { mutableStateOf("Manual checkpoint snapshot") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Create Workspace Snapshot Checkpoint",
        color = JarvisCyan,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
    },
    text = {
      OutlinedTextField(
        value = description,
        onValueChange = { description = it },
        label = { Text("Checkpoint Description") },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = JarvisCyan,
          unfocusedBorderColor = JarvisBorder
        )
      )
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(description) },
        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black)
      ) {
        Text("Save Checkpoint")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel", color = JarvisTextSecondary)
      }
    },
    containerColor = JarvisSurfaceCard
  )
}
