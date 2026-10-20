package com.example.data.model

enum class PermissionLevel(val levelNumber: Int, val title: String, val description: String) {
  LEVEL_1_READ(
    1,
    "Level 1: Read-Only",
    "Inspect workspace files, read source code, and analyze structure."
  ),
  LEVEL_2_REVERSIBLE(
    2,
    "Level 2: Reversible Development",
    "Create and edit files inside designated workspace, execute permitted tests."
  ),
  LEVEL_3_ENVIRONMENT(
    3,
    "Level 3: Environment Changes",
    "Modify project configuration, dependencies, or tool settings (Approval required)."
  ),
  LEVEL_4_EXTERNAL(
    4,
    "Level 4: External / Consequential",
    "Export, publication, external service integration, or irreversible deletions (Approval required)."
  )
}

data class ToolDefinition(
  val name: String,
  val description: String,
  val permissionLevel: PermissionLevel,
  val parameterSchema: String
)

data class ToolCall(
  val toolName: String,
  val arguments: Map<String, String>
)

data class ToolResult(
  val toolName: String,
  val success: Boolean,
  val output: String,
  val errorMessage: String? = null,
  val modifiedFile: String? = null,
  val executionTimeMs: Long = 0L
)
