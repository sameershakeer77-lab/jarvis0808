package com.example.data.model

data class ProjectFile(
  val relativePath: String,
  val name: String,
  val content: String = "",
  val sizeBytes: Long = 0L,
  val isDirectory: Boolean = false,
  val lastModified: Long = System.currentTimeMillis()
)

data class DiffLine(
  val type: DiffLineType,
  val text: String,
  val oldLineNumber: Int? = null,
  val newLineNumber: Int? = null
)

enum class DiffLineType {
  UNCHANGED,
  ADDED,
  REMOVED
}

data class DiffChange(
  val filePath: String,
  val additionsCount: Int,
  val deletionsCount: Int,
  val lines: List<DiffLine>
)

data class TestResult(
  val suiteName: String,
  val testName: String,
  val passed: Boolean,
  val executionTimeMs: Long,
  val message: String,
  val failureReason: String? = null
)

enum class ModelStatus {
  ONLINE,
  CONFIGURED,
  KEY_REQUIRED,
  UNAVAILABLE
}

data class ModelConnectorInfo(
  val provider: String,
  val modelId: String,
  val displayName: String,
  val status: ModelStatus,
  val isConfigured: Boolean,
  val capabilities: List<String>,
  val lastLatencyMs: Long? = null,
  val lastError: String? = null
)

data class ChatMessage(
  val id: String,
  val sender: ChatSender,
  val text: String,
  val timestamp: Long = System.currentTimeMillis(),
  val planId: String? = null,
  val toolSummary: String? = null,
  val isThinking: Boolean = false
)

enum class ChatSender {
  USER,
  JARVIS,
  SYSTEM,
  TOOL
}

enum class AcceptanceTestOutcome {
  NOT_RUN,
  RUNNING,
  PASSED,
  FAILED,
  UNAVAILABLE
}

data class AcceptanceTestItem(
  val id: String,
  val title: String,
  val description: String,
  val outcome: AcceptanceTestOutcome = AcceptanceTestOutcome.NOT_RUN,
  val details: String = "",
  val explanation: String = ""
)
