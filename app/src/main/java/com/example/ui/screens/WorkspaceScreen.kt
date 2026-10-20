package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.JarvisViewModel
import com.example.ui.components.CodeDiffViewer
import com.example.ui.components.CreateCheckpointDialog
import com.example.ui.components.CreateFileDialog
import com.example.ui.components.FileEditorView
import com.example.ui.components.FileExplorerList
import com.example.ui.theme.JarvisSurface

@Composable
fun WorkspaceScreen(
  viewModel: JarvisViewModel,
  modifier: Modifier = Modifier
) {
  val files by viewModel.workspaceFiles.collectAsStateWithLifecycle()
  val selectedFile by viewModel.selectedFile.collectAsStateWithLifecycle()
  val recentDiff by viewModel.recentDiff.collectAsStateWithLifecycle()

  var showCreateDialog by remember { mutableStateOf(false) }
  var showCheckpointDialog by remember { mutableStateOf(false) }
  var showDiffViewer by remember { mutableStateOf(false) }

  if (showCreateDialog) {
    CreateFileDialog(
      onDismiss = { showCreateDialog = false },
      onConfirm = { path, content ->
        showCreateDialog = false
        viewModel.createNewFile(path, content)
      }
    )
  }

  if (showCheckpointDialog) {
    CreateCheckpointDialog(
      onDismiss = { showCheckpointDialog = false },
      onConfirm = { desc ->
        showCheckpointDialog = false
        viewModel.createManualCheckpoint(desc)
      }
    )
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(JarvisSurface)
      .padding(14.dp)
  ) {
    // 1. File Explorer
    item {
      FileExplorerList(
        files = files,
        selectedFile = selectedFile,
        onSelectFile = { viewModel.selectFile(it) },
        onCreateFileClick = { showCreateDialog = true },
        onCheckpointClick = { showCheckpointDialog = true }
      )
      Spacer(modifier = Modifier.height(12.dp))
    }

    // 2. Active File Editor
    if (selectedFile != null) {
      item {
        FileEditorView(
          file = selectedFile!!,
          onSave = { updatedContent ->
            viewModel.saveFileContent(selectedFile!!.relativePath, updatedContent)
          },
          onDelete = {
            viewModel.deleteFile(selectedFile!!.relativePath)
          },
          onClose = {
            viewModel.selectFile(null)
          }
        )
        Spacer(modifier = Modifier.height(12.dp))
      }
    }

    // 3. Code Diff Viewer
    if (recentDiff != null) {
      item {
        CodeDiffViewer(
          diff = recentDiff!!,
          onDismiss = { /* keep available */ }
        )
      }
    }
  }
}
