package com.example.engine.planner

import com.example.data.model.ExecutionPhase
import com.example.data.model.RiskLevel
import com.example.data.model.StepStatus
import com.example.data.model.TaskClassification
import com.example.data.model.TaskExecutionState
import com.example.data.model.TaskPlan
import com.example.data.model.TaskStep
import java.util.UUID

class AutonomousPlanner {

  fun classifyPrompt(prompt: String): TaskClassification {
    val lower = prompt.lowercase().trim()
    return when {
      lower.contains("improve") || lower.contains("self-building") || lower.contains("upgrade yourself") ->
        TaskClassification.SELF_IMPROVEMENT

      lower.contains("test") || lower.contains("check unit") || lower.contains("run test") ->
        TaskClassification.TESTING_REQUEST

      lower.contains("inspect") || lower.contains("analyze") || lower.contains("find broken") || lower.contains("list file") ->
        TaskClassification.PROJECT_ANALYSIS

      lower.contains("speech") || lower.contains("voice") || lower.contains("microphone") || lower.contains("wake-word") || lower.contains("wake word") ->
        TaskClassification.VOICE_CAPABILITY_REQUEST

      lower.contains("fix") || lower.contains("bug") || lower.contains("repair") || lower.contains("error") ->
        TaskClassification.BUG_FIX

      lower.contains("model") || lower.contains("gemini") || lower.contains("ai connector") || lower.contains("openai") || lower.contains("claude") ->
        TaskClassification.AI_MODEL_INTEGRATION

      lower.contains("add ") || lower.contains("build ") || lower.contains("create new") || lower.contains("implement") ->
        TaskClassification.NEW_FEATURE_REQUEST

      lower.contains("edit ") || lower.contains("modify ") || lower.contains("update ") || lower.contains("replace") || lower.contains("rename") ->
        TaskClassification.CODE_MODIFICATION

      lower.contains("create project") || lower.contains("initialize") || lower.contains("new workspace") ->
        TaskClassification.PROJECT_CREATION

      lower.contains("config") || lower.contains("setting") || lower.contains("permission") ->
        TaskClassification.SYSTEM_CONFIGURATION

      lower.contains("hack") || lower.contains("root") || lower.contains("bypass security") || lower.contains("delete entire phone") ->
        TaskClassification.UNSUPPORTED_OR_BLOCKED

      lower.startsWith("why") || lower.startsWith("what is") || lower.startsWith("how do") || lower.startsWith("explain") ->
        TaskClassification.GENERAL_QUESTION

      else -> TaskClassification.NEW_FEATURE_REQUEST
    }
  }

  fun createPlan(prompt: String, availableFiles: List<String>): TaskPlan {
    val classification = classifyPrompt(prompt)
    val taskId = "task_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"

    if (classification == TaskClassification.UNSUPPORTED_OR_BLOCKED) {
      return TaskPlan(
        id = taskId,
        userPrompt = prompt,
        classification = classification,
        summary = "Blocked Request: Operation violates safety policy or exceeds mobile sandbox isolation boundaries.",
        targetFiles = emptyList(),
        riskLevel = RiskLevel.CRITICAL,
        steps = listOf(
          TaskStep(
            id = "${taskId}_step_1",
            phase = ExecutionPhase.UNDERSTAND,
            description = "Identify platform restriction violation",
            toolName = null,
            status = StepStatus.FAILED,
            error = "Security Policy: Android runtime prohibits uncontrolled root access or host system wiping."
          )
        ),
        requiresApproval = true,
        approvalExplanation = "Host system root access, uncontrolled permission elevation, or destructive device wiping is prohibited.",
        factualState = TaskExecutionState.BLOCKED
      )
    }

    if (classification == TaskClassification.GENERAL_QUESTION) {
      return TaskPlan(
        id = taskId,
        userPrompt = prompt,
        classification = classification,
        summary = "Analytical query: Answering user inquiry regarding system capabilities and architecture.",
        targetFiles = emptyList(),
        riskLevel = RiskLevel.LOW,
        steps = listOf(
          TaskStep(
            id = "${taskId}_step_1",
            phase = ExecutionPhase.UNDERSTAND,
            description = "Analyze user query and verify against project knowledge",
            toolName = "search_code",
            status = StepStatus.PENDING
          ),
          TaskStep(
            id = "${taskId}_step_2",
            phase = ExecutionPhase.REPORT,
            description = "Synthesize comprehensive architectural explanation",
            toolName = null,
            status = StepStatus.PENDING
          )
        ),
        factualState = TaskExecutionState.PLANNED
      )
    }

    val (targetFile, risk, reqApproval) = when (classification) {
      TaskClassification.VOICE_CAPABILITY_REQUEST -> Triple("src/main/kotlin/SpeechModule.kt", RiskLevel.MEDIUM, false)
      TaskClassification.BUG_FIX -> Triple("src/main/kotlin/AssistantCore.kt", RiskLevel.MEDIUM, false)
      TaskClassification.TESTING_REQUEST -> Triple("tests/AssistantTest.kt", RiskLevel.LOW, false)
      TaskClassification.AI_MODEL_INTEGRATION -> Triple("src/main/kotlin/modules/ModelConnectorModule.kt", RiskLevel.MEDIUM, false)
      TaskClassification.PROJECT_ANALYSIS -> Triple("src/main/kotlin/AssistantCore.kt", RiskLevel.LOW, false)
      TaskClassification.SELF_IMPROVEMENT -> Triple("src/main/kotlin/modules/SelfImprovementModule.kt", RiskLevel.MEDIUM, false)
      TaskClassification.CODE_MODIFICATION -> Triple(
        availableFiles.firstOrNull { it.endsWith(".kt") } ?: "src/main/kotlin/AssistantCore.kt",
        RiskLevel.MEDIUM,
        false
      )
      TaskClassification.SYSTEM_CONFIGURATION -> {
        val isDestructive = prompt.lowercase().contains("delete") || prompt.lowercase().contains("wipe")
        Triple("jarvis.config.json", if (isDestructive) RiskLevel.CRITICAL else RiskLevel.HIGH, isDestructive)
      }
      else -> {
        val lower = prompt.lowercase()
        val path = when {
          lower.contains("memory") || lower.contains("vector") -> "src/main/kotlin/modules/ProjectMemoryModule.kt"
          lower.contains("diagnostic") || lower.contains("monitor") -> "src/main/kotlin/modules/DiagnosticsMonitorModule.kt"
          lower.contains("settings") -> "src/main/kotlin/modules/SettingsModule.kt"
          lower.contains("browser") -> "src/main/kotlin/modules/BrowserAutomationModule.kt"
          else -> {
            val candidateName = prompt.filter { it.isLetterOrDigit() }.take(16).replaceFirstChar {
              if (it.isLowerCase()) it.titlecase() else it.toString()
            }.ifBlank { "CustomCapability" }
            "src/main/kotlin/modules/${candidateName}Module.kt"
          }
        }
        Triple(path, RiskLevel.MEDIUM, false)
      }
    }

    val steps = mutableListOf<TaskStep>()

    // Phase 1: Understand
    steps.add(
      TaskStep(
        id = "${taskId}_s1",
        phase = ExecutionPhase.UNDERSTAND,
        description = "Inspect workspace structure and read interfaces of $targetFile",
        toolName = "read_file"
      )
    )

    // Phase 2: Plan
    steps.add(
      TaskStep(
        id = "${taskId}_s2",
        phase = ExecutionPhase.PLAN,
        description = "Formulate safe changes, define dependencies, and verify interfaces",
        toolName = "search_code"
      )
    )

    // Phase 3: Prepare
    steps.add(
      TaskStep(
        id = "${taskId}_s3",
        phase = ExecutionPhase.PREPARE,
        description = "Create recoverable workspace snapshot checkpoint",
        toolName = "create_checkpoint"
      )
    )

    // Phase 4: Implement
    val implementTool = if (classification == TaskClassification.NEW_FEATURE_REQUEST || classification == TaskClassification.SELF_IMPROVEMENT || !availableFiles.contains(targetFile)) {
      "create_file"
    } else {
      "edit_file"
    }
    steps.add(
      TaskStep(
        id = "${taskId}_s4",
        phase = ExecutionPhase.IMPLEMENT,
        description = "Apply synthesized code modifications to $targetFile",
        toolName = implementTool
      )
    )

    // Phase 5: Review
    steps.add(
      TaskStep(
        id = "${taskId}_s5",
        phase = ExecutionPhase.REVIEW,
        description = "Review changes for syntax consistency and bracket balance",
        toolName = "run_syntax_check"
      )
    )

    // Phase 6: Test
    steps.add(
      TaskStep(
        id = "${taskId}_s6",
        phase = ExecutionPhase.TEST,
        description = "Execute workspace unit test suite and verify assertions",
        toolName = "run_unit_tests"
      )
    )

    // Phase 7: Repair (Conditional Bounded Auto-Repair)
    steps.add(
      TaskStep(
        id = "${taskId}_s7",
        phase = ExecutionPhase.REPAIR,
        description = "Inspect errors if test fails and apply targeted repair",
        toolName = "edit_file"
      )
    )

    // Phase 8: Verify
    steps.add(
      TaskStep(
        id = "${taskId}_s8",
        phase = ExecutionPhase.VERIFY,
        description = "Rerun test suites to verify repair resolution",
        toolName = "run_unit_tests"
      )
    )

    // Phase 9: Report
    steps.add(
      TaskStep(
        id = "${taskId}_s9",
        phase = ExecutionPhase.REPORT,
        description = "Present factual summary of applied modifications and test results",
        toolName = null
      )
    )

    // Phase 10: Release
    steps.add(
      TaskStep(
        id = "${taskId}_s10",
        phase = ExecutionPhase.RELEASE,
        description = "Record successful modification in persistent project knowledge memory",
        toolName = null
      )
    )

    return TaskPlan(
      id = taskId,
      userPrompt = prompt,
      classification = classification,
      summary = "Autonomous 10-Phase Self-Improvement Plan for: $prompt",
      targetFiles = listOf(targetFile),
      riskLevel = risk,
      steps = steps,
      requiresApproval = reqApproval,
      approvalExplanation = if (reqApproval) "Sensitive operation requires explicit operator authorization." else null,
      factualState = TaskExecutionState.PLANNED
    )
  }
}
