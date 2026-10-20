package com.example.engine.workspace

import android.content.Context
import com.example.data.model.DiffChange
import com.example.data.model.DiffLine
import com.example.data.model.DiffLineType
import com.example.data.model.ProjectFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class WorkspaceManager(private val context: Context) {

  private val workspaceRoot: File by lazy {
    val dir = File(context.filesDir, "jarvis_workspace")
    if (!dir.exists()) {
      dir.mkdirs()
    }
    dir
  }

  private val checkpointsRoot: File by lazy {
    val dir = File(context.filesDir, "jarvis_checkpoints")
    if (!dir.exists()) {
      dir.mkdirs()
    }
    dir
  }

  suspend fun initializeWorkspaceIfNeeded() = withContext(Dispatchers.IO) {
    if (workspaceRoot.listFiles()?.isEmpty() != false) {
      bootstrapInitialProject()
    }
  }

  private fun bootstrapInitialProject() {
    createFileDirectly(
      "src/main/kotlin/AssistantCore.kt",
      """
      package com.jarvis.core
      
      /**
       * Core autonomous assistant module for JARVIS.
       * Handles executive telemetry, memory hooks, and capability registration.
       */
      class AssistantCore {
          val systemName: String = "JARVIS"
          val version: String = "1.0.0"
          private val activeModules = mutableListOf<String>("telemetry", "planner", "file_engine")
      
          fun getSystemStatus(): String {
              return "JARVIS Core v${'$'}version | Active modules: ${'$'}{activeModules.size}"
          }
      
          fun registerCapability(name: String): Boolean {
              if (!activeModules.contains(name)) {
                  activeModules.add(name)
                  return true
              }
              return false
          }
          
          fun getRegisteredCapabilities(): List<String> = activeModules.toList()
      }
      """.trimIndent()
    )

    createFileDirectly(
      "src/main/kotlin/SpeechModule.kt",
      """
      package com.jarvis.voice
      
      /**
       * Voice recognition and speech telemetry connector.
       */
      class SpeechModule {
          var isListening: Boolean = false
          private var wakeWord: String = "Hey Jarvis"
      
          fun checkWakeWord(input: String): Boolean {
              return input.trim().equals(wakeWord, ignoreCase = true)
          }
          
          fun setWakeWord(word: String) {
              wakeWord = word
          }
      }
      """.trimIndent()
    )

    createFileDirectly(
      "src/main/kotlin/Config.kt",
      """
      package com.jarvis.config
      
      object JarvisConfig {
          const val WORKSPACE_ENV = "SANDBOX_SECURE"
          const val MAX_AUTO_REPAIR_ATTEMPTS = 3
          const val DEFAULT_MODEL = "gemini-2.5-flash"
      }
      """.trimIndent()
    )

    createFileDirectly(
      "tests/AssistantTest.kt",
      """
      package com.jarvis.tests
      
      import com.jarvis.core.AssistantCore
      import com.jarvis.voice.SpeechModule
      
      class AssistantTest {
          fun testStatusReport(): Boolean {
              val core = AssistantCore()
              return core.systemName == "JARVIS" && core.getSystemStatus().contains("v1.0.0")
          }
          
          fun testRegisterCapability(): Boolean {
              val core = AssistantCore()
              val registered = core.registerCapability("browser_automation")
              return registered && core.getRegisteredCapabilities().contains("browser_automation")
          }
          
          fun testWakeWordMatching(): Boolean {
              val speech = SpeechModule()
              return speech.checkWakeWord("Hey Jarvis") && !speech.checkWakeWord("Hello Siri")
          }
      }
      """.trimIndent()
    )

    createFileDirectly(
      "jarvis.config.json",
      """
      {
        "agent": "JARVIS",
        "autonomy_level": "LEVEL_2_REVERSIBLE",
        "safe_workspace": true,
        "supported_languages": ["kotlin", "json", "gradle"]
      }
      """.trimIndent()
    )

    createFileDirectly(
      "README.md",
      """
      # JARVIS Workspace Project
      
      Authorized self-building AI assistant development environment.
      Source of truth for modular capability development, automated testing, and checkpoints.
      """.trimIndent()
    )
  }

  private fun createFileDirectly(relativePath: String, content: String) {
    val file = File(workspaceRoot, relativePath)
    file.parentFile?.mkdirs()
    file.writeText(content, Charsets.UTF_8)
  }

  suspend fun listFiles(): List<ProjectFile> = withContext(Dispatchers.IO) {
    val results = mutableListOf<ProjectFile>()
    fun traverse(dir: File) {
      val children = dir.listFiles() ?: return
      for (child in children) {
        val relPath = child.relativeTo(workspaceRoot).path
        if (child.isDirectory) {
          results.add(
            ProjectFile(
              relativePath = relPath,
              name = child.name,
              isDirectory = true,
              lastModified = child.lastModified()
            )
          )
          traverse(child)
        } else {
          val content = try { child.readText() } catch (e: Exception) { "" }
          results.add(
            ProjectFile(
              relativePath = relPath,
              name = child.name,
              content = content,
              sizeBytes = child.length(),
              isDirectory = false,
              lastModified = child.lastModified()
            )
          )
        }
      }
    }
    traverse(workspaceRoot)
    results.sortedWith(compareBy<ProjectFile> { !it.isDirectory }.thenBy { it.relativePath })
  }

  suspend fun readFile(relativePath: String): Result<String> = withContext(Dispatchers.IO) {
    try {
      val file = File(workspaceRoot, relativePath)
      if (!file.exists()) {
        Result.failure(NoSuchFileException(file, reason = "File not found in workspace: $relativePath"))
      } else if (file.isDirectory) {
        Result.failure(IllegalArgumentException("Cannot read directory as file: $relativePath"))
      } else {
        Result.success(file.readText(Charsets.UTF_8))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun createFile(relativePath: String, content: String): Result<Boolean> = withContext(Dispatchers.IO) {
    try {
      val file = File(workspaceRoot, relativePath)
      file.parentFile?.mkdirs()
      file.writeText(content, Charsets.UTF_8)
      Result.success(true)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun editFile(relativePath: String, targetContent: String, replacementContent: String): Result<Boolean> = withContext(Dispatchers.IO) {
    try {
      val file = File(workspaceRoot, relativePath)
      if (!file.exists()) {
        return@withContext Result.failure(NoSuchFileException(file, reason = "Cannot edit missing file: $relativePath"))
      }
      val current = file.readText(Charsets.UTF_8)
      if (!current.contains(targetContent)) {
        return@withContext Result.failure(IllegalArgumentException("Target content not found in $relativePath. Preservation check aborted."))
      }
      val occurrences = current.split(targetContent).size - 1
      if (occurrences > 1) {
        return@withContext Result.failure(IllegalArgumentException("Ambiguous target: target content occurs $occurrences times in $relativePath."))
      }
      val updated = current.replace(targetContent, replacementContent)
      file.writeText(updated, Charsets.UTF_8)
      Result.success(true)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun writeFile(relativePath: String, content: String): Result<Boolean> = withContext(Dispatchers.IO) {
    try {
      val file = File(workspaceRoot, relativePath)
      file.parentFile?.mkdirs()
      file.writeText(content, Charsets.UTF_8)
      Result.success(true)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun renameFile(oldPath: String, newPath: String): Result<Boolean> = withContext(Dispatchers.IO) {
    try {
      val src = File(workspaceRoot, oldPath)
      val dest = File(workspaceRoot, newPath)
      if (!src.exists()) {
        return@withContext Result.failure(NoSuchFileException(src, reason = "Source does not exist: $oldPath"))
      }
      dest.parentFile?.mkdirs()
      val ok = src.renameTo(dest)
      Result.success(ok)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun deleteFile(relativePath: String): Result<Boolean> = withContext(Dispatchers.IO) {
    try {
      val file = File(workspaceRoot, relativePath)
      if (!file.exists()) {
        return@withContext Result.failure(NoSuchFileException(file, reason = "File not found: $relativePath"))
      }
      val deleted = file.deleteRecursively()
      Result.success(deleted)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun searchCode(query: String): List<Pair<String, Int>> = withContext(Dispatchers.IO) {
    val results = mutableListOf<Pair<String, Int>>()
    val files = listFiles().filter { !it.isDirectory }
    for (f in files) {
      val lines = f.content.lines()
      lines.forEachIndexed { index, line ->
        if (line.contains(query, ignoreCase = true)) {
          results.add("${f.relativePath}:${index + 1}: ${line.trim()}" to (index + 1))
        }
      }
    }
    results
  }

  suspend fun createCheckpoint(checkpointId: String, description: String): Result<Int> = withContext(Dispatchers.IO) {
    try {
      val cpDir = File(checkpointsRoot, checkpointId)
      if (cpDir.exists()) cpDir.deleteRecursively()
      cpDir.mkdirs()

      var count = 0
      workspaceRoot.walkTopDown().forEach { file ->
        val rel = file.relativeTo(workspaceRoot).path
        if (rel.isNotEmpty()) {
          val target = File(cpDir, rel)
          if (file.isDirectory) {
            target.mkdirs()
          } else {
            target.parentFile?.mkdirs()
            file.copyTo(target, overwrite = true)
            count++
          }
        }
      }
      Result.success(count)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun restoreCheckpoint(checkpointId: String): Result<Boolean> = withContext(Dispatchers.IO) {
    try {
      val cpDir = File(checkpointsRoot, checkpointId)
      if (!cpDir.exists()) {
        return@withContext Result.failure(NoSuchFileException(cpDir, reason = "Checkpoint $checkpointId not found."))
      }
      // Clear workspace and copy from checkpoint
      workspaceRoot.deleteRecursively()
      workspaceRoot.mkdirs()
      cpDir.walkTopDown().forEach { file ->
        val rel = file.relativeTo(cpDir).path
        if (rel.isNotEmpty()) {
          val target = File(workspaceRoot, rel)
          if (file.isDirectory) {
            target.mkdirs()
          } else {
            target.parentFile?.mkdirs()
            file.copyTo(target, overwrite = true)
          }
        }
      }
      Result.success(true)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun resetToDefault(): Result<Boolean> = withContext(Dispatchers.IO) {
    try {
      workspaceRoot.deleteRecursively()
      workspaceRoot.mkdirs()
      bootstrapInitialProject()
      Result.success(true)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun computeDiff(filePath: String, oldContent: String, newContent: String): DiffChange {
    val oldLines = oldContent.lines()
    val newLines = newContent.lines()
    val lines = mutableListOf<DiffLine>()

    var additions = 0
    var deletions = 0

    val maxLen = maxOf(oldLines.size, newLines.size)
    for (i in 0 until maxLen) {
      val oldLine = oldLines.getOrNull(i)
      val newLine = newLines.getOrNull(i)

      if (oldLine == newLine && oldLine != null) {
        lines.add(DiffLine(DiffLineType.UNCHANGED, oldLine, i + 1, i + 1))
      } else {
        if (oldLine != null) {
          lines.add(DiffLine(DiffLineType.REMOVED, oldLine, i + 1, null))
          deletions++
        }
        if (newLine != null) {
          lines.add(DiffLine(DiffLineType.ADDED, newLine, null, i + 1))
          additions++
        }
      }
    }
    return DiffChange(
      filePath = filePath,
      additionsCount = additions,
      deletionsCount = deletions,
      lines = lines
    )
  }
}
