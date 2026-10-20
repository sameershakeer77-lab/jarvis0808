package com.example.engine.tools

import com.example.data.database.AuditLogDao
import com.example.data.database.AuditLogEntity
import com.example.data.model.PermissionLevel
import com.example.data.model.ToolDefinition
import com.example.data.model.ToolResult
import com.example.engine.testing.CodeValidator
import com.example.engine.workspace.WorkspaceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ToolRegistry(
  private val workspaceManager: WorkspaceManager,
  private val codeValidator: CodeValidator,
  private val auditLogDao: AuditLogDao
) {

  private val registeredTools = mutableMapOf<String, ToolDefinition>()

  init {
    registerDefaults()
  }

  private fun registerDefaults() {
    register(
      ToolDefinition(
        name = "list_files",
        description = "List all files and directories in the authorized workspace",
        permissionLevel = PermissionLevel.LEVEL_1_READ,
        parameterSchema = "{}"
      )
    )
    register(
      ToolDefinition(
        name = "read_file",
        description = "Read the full contents of a file in the workspace",
        permissionLevel = PermissionLevel.LEVEL_1_READ,
        parameterSchema = "{\"path\": \"string\"}"
      )
    )
    register(
      ToolDefinition(
        name = "search_code",
        description = "Search across workspace source files for a pattern or keyword",
        permissionLevel = PermissionLevel.LEVEL_1_READ,
        parameterSchema = "{\"query\": \"string\"}"
      )
    )
    register(
      ToolDefinition(
        name = "create_file",
        description = "Create a new file with specified content in the workspace",
        permissionLevel = PermissionLevel.LEVEL_2_REVERSIBLE,
        parameterSchema = "{\"path\": \"string\", \"content\": \"string\"}"
      )
    )
    register(
      ToolDefinition(
        name = "edit_file",
        description = "Replace unique target content with replacement in an existing workspace file",
        permissionLevel = PermissionLevel.LEVEL_2_REVERSIBLE,
        parameterSchema = "{\"path\": \"string\", \"target_content\": \"string\", \"replacement_content\": \"string\"}"
      )
    )
    register(
      ToolDefinition(
        name = "write_file",
        description = "Overwrite complete contents of an authorized file",
        permissionLevel = PermissionLevel.LEVEL_2_REVERSIBLE,
        parameterSchema = "{\"path\": \"string\", \"content\": \"string\"}"
      )
    )
    register(
      ToolDefinition(
        name = "run_syntax_check",
        description = "Perform syntax validation on all workspace files",
        permissionLevel = PermissionLevel.LEVEL_2_REVERSIBLE,
        parameterSchema = "{}"
      )
    )
    register(
      ToolDefinition(
        name = "run_unit_tests",
        description = "Execute workspace unit test suites and verify assertions",
        permissionLevel = PermissionLevel.LEVEL_2_REVERSIBLE,
        parameterSchema = "{}"
      )
    )
    register(
      ToolDefinition(
        name = "create_checkpoint",
        description = "Create a full snapshot checkpoint of current workspace state",
        permissionLevel = PermissionLevel.LEVEL_2_REVERSIBLE,
        parameterSchema = "{\"checkpoint_id\": \"string\", \"description\": \"string\"}"
      )
    )
    register(
      ToolDefinition(
        name = "restore_checkpoint",
        description = "Rollback workspace to a previously saved checkpoint",
        permissionLevel = PermissionLevel.LEVEL_2_REVERSIBLE,
        parameterSchema = "{\"checkpoint_id\": \"string\"}"
      )
    )
    register(
      ToolDefinition(
        name = "rename_file",
        description = "Move or rename a workspace file",
        permissionLevel = PermissionLevel.LEVEL_3_ENVIRONMENT,
        parameterSchema = "{\"old_path\": \"string\", \"new_path\": \"string\"}"
      )
    )
    register(
      ToolDefinition(
        name = "delete_file",
        description = "Delete a file from the workspace (recoverable via checkpoint)",
        permissionLevel = PermissionLevel.LEVEL_4_EXTERNAL,
        parameterSchema = "{\"path\": \"string\"}"
      )
    )
  }

  fun register(definition: ToolDefinition) {
    registeredTools[definition.name] = definition
  }

  fun getRegisteredTools(): List<ToolDefinition> = registeredTools.values.toList()

  fun getTool(name: String): ToolDefinition? = registeredTools[name]

  suspend fun executeTool(
    taskId: String,
    toolName: String,
    arguments: Map<String, String>
  ): ToolResult = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()
    val toolDef = registeredTools[toolName]
      ?: return@withContext ToolResult(
        toolName = toolName,
        success = false,
        output = "",
        errorMessage = "Tool '$toolName' is not registered in JARVIS ToolRegistry."
      )

    var result: ToolResult = try {
      when (toolName) {
        "list_files" -> {
          val files = workspaceManager.listFiles()
          val listing = files.joinToString("\n") { f ->
            if (f.isDirectory) "[DIR]  ${f.relativePath}/"
            else "[FILE] ${f.relativePath} (${f.sizeBytes} B)"
          }
          ToolResult(toolName, true, "Workspace files (${files.size} items):\n$listing")
        }

        "read_file" -> {
          val path = arguments["path"] ?: return@withContext errorResult(toolName, "Missing required argument 'path'")
          val res = workspaceManager.readFile(path)
          if (res.isSuccess) {
            ToolResult(toolName, true, res.getOrThrow())
          } else {
            ToolResult(toolName, false, "", res.exceptionOrNull()?.message)
          }
        }

        "search_code" -> {
          val query = arguments["query"] ?: return@withContext errorResult(toolName, "Missing argument 'query'")
          val matches = workspaceManager.searchCode(query)
          if (matches.isEmpty()) {
            ToolResult(toolName, true, "No matches found for query: '$query'")
          } else {
            val text = matches.joinToString("\n") { it.first }
            ToolResult(toolName, true, "Found ${matches.size} matches:\n$text")
          }
        }

        "create_file" -> {
          val path = arguments["path"] ?: return@withContext errorResult(toolName, "Missing argument 'path'")
          val content = arguments["content"] ?: ""
          val res = workspaceManager.createFile(path, content)
          if (res.isSuccess) {
            ToolResult(toolName, true, "File created successfully: $path (${content.length} chars)", modifiedFile = path)
          } else {
            ToolResult(toolName, false, "", res.exceptionOrNull()?.message, modifiedFile = path)
          }
        }

        "edit_file" -> {
          val path = arguments["path"] ?: return@withContext errorResult(toolName, "Missing argument 'path'")
          val target = arguments["target_content"] ?: return@withContext errorResult(toolName, "Missing 'target_content'")
          val replacement = arguments["replacement_content"] ?: return@withContext errorResult(toolName, "Missing 'replacement_content'")
          val res = workspaceManager.editFile(path, target, replacement)
          if (res.isSuccess) {
            ToolResult(toolName, true, "File modified successfully: $path", modifiedFile = path)
          } else {
            ToolResult(toolName, false, "", res.exceptionOrNull()?.message, modifiedFile = path)
          }
        }

        "write_file" -> {
          val path = arguments["path"] ?: return@withContext errorResult(toolName, "Missing argument 'path'")
          val content = arguments["content"] ?: ""
          val res = workspaceManager.writeFile(path, content)
          if (res.isSuccess) {
            ToolResult(toolName, true, "File written successfully: $path", modifiedFile = path)
          } else {
            ToolResult(toolName, false, "", res.exceptionOrNull()?.message, modifiedFile = path)
          }
        }

        "run_syntax_check" -> {
          val report = codeValidator.checkSyntax()
          if (report.isValid) {
            ToolResult(toolName, true, "Syntax verification PASSED: Checked ${report.totalFilesChecked} files with 0 errors.")
          } else {
            val errs = report.issuesFound.joinToString("\n")
            ToolResult(toolName, false, "Syntax check completed with errors:\n$errs", errorMessage = "${report.issuesFound.size} syntax issues found")
          }
        }

        "run_unit_tests" -> {
          val testResults = codeValidator.runUnitTests()
          val passed = testResults.count { it.passed }
          val failed = testResults.count { !it.passed }
          val summary = testResults.joinToString("\n") { t ->
            val icon = if (t.passed) "[PASS]" else "[FAIL]"
            "$icon ${t.suiteName}.${t.testName} (${t.executionTimeMs}ms): ${t.message}" +
              (if (t.failureReason != null) " -> Reason: ${t.failureReason}" else "")
          }
          ToolResult(
            toolName = toolName,
            success = failed == 0,
            output = "Test Results: $passed passed, $failed failed:\n$summary",
            errorMessage = if (failed > 0) "$failed test(s) failed in workspace" else null
          )
        }

        "create_checkpoint" -> {
          val cpId = arguments["checkpoint_id"] ?: "cp_${System.currentTimeMillis()}"
          val desc = arguments["description"] ?: "Automated pre-change checkpoint"
          val res = workspaceManager.createCheckpoint(cpId, desc)
          if (res.isSuccess) {
            ToolResult(toolName, true, "Checkpoint created: $cpId (${res.getOrNull()} files preserved)")
          } else {
            ToolResult(toolName, false, "", res.exceptionOrNull()?.message)
          }
        }

        "restore_checkpoint" -> {
          val cpId = arguments["checkpoint_id"] ?: return@withContext errorResult(toolName, "Missing checkpoint_id")
          val res = workspaceManager.restoreCheckpoint(cpId)
          if (res.isSuccess) {
            ToolResult(toolName, true, "Rollback successful: Workspace restored to checkpoint $cpId")
          } else {
            ToolResult(toolName, false, "", res.exceptionOrNull()?.message)
          }
        }

        "rename_file" -> {
          val oldPath = arguments["old_path"] ?: return@withContext errorResult(toolName, "Missing old_path")
          val newPath = arguments["new_path"] ?: return@withContext errorResult(toolName, "Missing new_path")
          val res = workspaceManager.renameFile(oldPath, newPath)
          if (res.isSuccess) {
            ToolResult(toolName, true, "Renamed $oldPath -> $newPath", modifiedFile = newPath)
          } else {
            ToolResult(toolName, false, "", res.exceptionOrNull()?.message)
          }
        }

        "delete_file" -> {
          val path = arguments["path"] ?: return@withContext errorResult(toolName, "Missing path")
          val res = workspaceManager.deleteFile(path)
          if (res.isSuccess) {
            ToolResult(toolName, true, "Deleted file: $path", modifiedFile = path)
          } else {
            ToolResult(toolName, false, "", res.exceptionOrNull()?.message)
          }
        }

        else -> ToolResult(toolName, false, "", "Unhandled tool: $toolName")
      }
    } catch (e: Exception) {
      ToolResult(toolName, false, "", e.message ?: "Unknown execution error")
    }

    val duration = System.currentTimeMillis() - startTime
    result = result.copy(executionTimeMs = duration)

    // Audit log persist
    try {
      auditLogDao.insertLog(
        AuditLogEntity(
          taskId = taskId,
          toolName = toolName,
          permissionLevel = toolDef.permissionLevel.levelNumber,
          inputPayload = arguments.toString(),
          outputPayload = result.output.take(500),
          success = result.success,
          errorMessage = result.errorMessage,
          timestamp = System.currentTimeMillis()
        )
      )
    } catch (_: Exception) {}

    result
  }

  private fun errorResult(tool: String, msg: String): ToolResult {
    return ToolResult(tool, false, "", msg)
  }
}
