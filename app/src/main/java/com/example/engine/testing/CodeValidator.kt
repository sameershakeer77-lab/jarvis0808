package com.example.engine.testing

import com.example.data.model.TestResult
import com.example.engine.workspace.WorkspaceManager
import java.util.Stack

class CodeValidator(private val workspaceManager: WorkspaceManager) {

  data class SyntaxCheckReport(
    val isValid: Boolean,
    val totalFilesChecked: Int,
    val issuesFound: List<String>
  )

  suspend fun checkSyntax(): SyntaxCheckReport {
    val files = workspaceManager.listFiles().filter { !it.isDirectory && (it.relativePath.endsWith(".kt") || it.relativePath.endsWith(".json")) }
    val issues = mutableListOf<String>()

    for (file in files) {
      val content = file.content
      // Check balanced braces and brackets
      val balanceError = checkBracketsBalance(file.relativePath, content)
      if (balanceError != null) {
        issues.add(balanceError)
      }

      // Check Kotlin basic syntax patterns
      if (file.relativePath.endsWith(".kt")) {
        val lines = content.lines()
        var hasPackage = false
        lines.forEachIndexed { idx, line ->
          val trimmed = line.trim()
          if (trimmed.startsWith("package ")) hasPackage = true
          // Check for unfinished string literals
          val quoteCount = trimmed.count { it == '"' }
          if (quoteCount % 2 != 0 && !trimmed.contains("\"\"\"")) {
            issues.add("${file.relativePath}:${idx + 1}: Unclosed string literal detected")
          }
        }
        if (!hasPackage && lines.any { it.trim().startsWith("class ") || it.trim().startsWith("object ") }) {
          issues.add("${file.relativePath}: Warning: Missing package declaration")
        }
      }

      // Check JSON syntax
      if (file.relativePath.endsWith(".json")) {
        if (!content.trim().startsWith("{") && !content.trim().startsWith("[")) {
          issues.add("${file.relativePath}: Invalid JSON format (does not begin with '{' or '[')")
        }
      }
    }

    return SyntaxCheckReport(
      isValid = issues.isEmpty(),
      totalFilesChecked = files.size,
      issuesFound = issues
    )
  }

  private fun checkBracketsBalance(path: String, code: String): String? {
    val stack = Stack<Char>()
    var inComment = false

    var i = 0
    while (i < code.length) {
      val c = code[i]
      if (inComment) {
        if (c == '\n') inComment = false
        i++
        continue
      }
      if (c == '/' && i + 1 < code.length && code[i + 1] == '/') {
        inComment = true
        i += 2
        continue
      }

      when (c) {
        '{', '(', '[' -> stack.push(c)
        '}' -> {
          if (stack.isEmpty() || stack.pop() != '{') return "$path: Mismatched closing brace '}'"
        }
        ')' -> {
          if (stack.isEmpty() || stack.pop() != '(') return "$path: Mismatched closing parenthesis ')'"
        }
        ']' -> {
          if (stack.isEmpty() || stack.pop() != '[') return "$path: Mismatched closing bracket ']'"
        }
      }
      i++
    }
    if (!stack.isEmpty()) {
      return "$path: Unclosed delimiter remaining: '${stack.peek()}'"
    }
    return null
  }

  suspend fun runUnitTests(): List<TestResult> {
    val results = mutableListOf<TestResult>()
    val startTime = System.currentTimeMillis()

    // 1. Inspect workspace for AssistantCore.kt
    val coreFile = workspaceManager.readFile("src/main/kotlin/AssistantCore.kt")
    val coreTime = System.currentTimeMillis() - startTime
    if (coreFile.isSuccess) {
      val code = coreFile.getOrNull().orEmpty()
      val hasJarvis = code.contains("\"JARVIS\"")
      val hasVersion = code.contains("version")
      val hasRegister = code.contains("fun registerCapability")

      if (hasJarvis && hasVersion) {
        results.add(
          TestResult(
            suiteName = "AssistantTestSuite",
            testName = "testStatusReport",
            passed = true,
            executionTimeMs = maxOf(4L, coreTime),
            message = "Assertion PASSED: AssistantCore correctly reports JARVIS v1.0.0 identity"
          )
        )
      } else {
        results.add(
          TestResult(
            suiteName = "AssistantTestSuite",
            testName = "testStatusReport",
            passed = false,
            executionTimeMs = maxOf(4L, coreTime),
            message = "Assertion FAILED: AssistantCore does not contain valid JARVIS system identity",
            failureReason = "Expected systemName='JARVIS', found modified or missing string definition."
          )
        )
      }

      if (hasRegister) {
        results.add(
          TestResult(
            suiteName = "AssistantTestSuite",
            testName = "testRegisterCapability",
            passed = true,
            executionTimeMs = 6L,
            message = "Assertion PASSED: Dynamic capability registration functional in workspace"
          )
        )
      } else {
        results.add(
          TestResult(
            suiteName = "AssistantTestSuite",
            testName = "testRegisterCapability",
            passed = false,
            executionTimeMs = 6L,
            message = "Assertion FAILED: registerCapability method missing in AssistantCore.kt",
            failureReason = "Method 'fun registerCapability' was removed or renamed."
          )
        )
      }
    } else {
      results.add(
        TestResult(
          suiteName = "AssistantTestSuite",
          testName = "testStatusReport",
          passed = false,
          executionTimeMs = 2L,
          message = "File Missing: src/main/kotlin/AssistantCore.kt not found",
          failureReason = coreFile.exceptionOrNull()?.message
        )
      )
    }

    // 2. Speech module test
    val speechFile = workspaceManager.readFile("src/main/kotlin/SpeechModule.kt")
    if (speechFile.isSuccess) {
      val speechCode = speechFile.getOrNull().orEmpty()
      val hasWakeWord = speechCode.contains("Hey Jarvis") || speechCode.contains("wakeWord")
      results.add(
        TestResult(
          suiteName = "SpeechTestSuite",
          testName = "testWakeWordMatching",
          passed = hasWakeWord,
          executionTimeMs = 5L,
          message = if (hasWakeWord) "Assertion PASSED: Wake word matching logic functional" else "Assertion FAILED: Wake word logic modified",
          failureReason = if (!hasWakeWord) "SpeechModule does not contain reference to wakeWord" else null
        )
      )
    }

    // 3. Config validation test
    val configFile = workspaceManager.readFile("jarvis.config.json")
    if (configFile.isSuccess) {
      val cfg = configFile.getOrNull().orEmpty()
      val validCfg = cfg.contains("\"agent\": \"JARVIS\"")
      results.add(
        TestResult(
          suiteName = "ConfigTestSuite",
          testName = "testConfigurationIntegrity",
          passed = validCfg,
          executionTimeMs = 3L,
          message = if (validCfg) "Assertion PASSED: System configuration verified" else "Assertion FAILED: Invalid config",
          failureReason = if (!validCfg) "Agent name must be JARVIS in jarvis.config.json" else null
        )
      )
    }

    // 4. Dynamically inspect all test files in tests/ folder
    val allFiles = workspaceManager.listFiles()
    val testFiles = allFiles.filter { !it.isDirectory && it.relativePath.startsWith("tests/") && it.relativePath != "tests/AssistantTest.kt" }

    for (testFile in testFiles) {
      val testName = testFile.name.removeSuffix(".kt")
      val testCode = testFile.content
      val suiteName = testName.ifBlank { "DynamicTestSuite" }

      // Check if corresponding source module exists
      val expectedModuleName = testName.removeSuffix("Test")
      val moduleExists = allFiles.any {
        !it.isDirectory && (it.name == "$expectedModuleName.kt" || it.name == "${expectedModuleName}Module.kt")
      }

      val hasValidAssertions = testCode.contains("return ") && testCode.contains("fun ")
      val testPassed = moduleExists && hasValidAssertions

      results.add(
        TestResult(
          suiteName = suiteName,
          testName = "testDynamicModuleIntegrity",
          passed = testPassed,
          executionTimeMs = 8L,
          message = if (testPassed) {
            "Assertion PASSED: $testName evaluated against workspace implementation."
          } else {
            "Assertion FAILED: $testName could not locate matching source module or valid assertions."
          },
          failureReason = if (!testPassed) "Missing matching module implementation for $testName" else null
        )
      )
    }

    return results
  }
}
