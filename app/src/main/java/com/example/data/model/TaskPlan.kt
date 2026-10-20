package com.example.data.model

enum class TaskClassification(val displayName: String, val description: String) {
  GENERAL_QUESTION("General Question", "General inquiry or conceptual clarification"),
  PROJECT_ANALYSIS("Project Analysis", "Inspect project architecture, files, and dependencies"),
  NEW_FEATURE_REQUEST("New Feature Request", "Plan, create and integrate new capabilities"),
  CODE_MODIFICATION("Code Modification", "Edit existing code while preserving functionality"),
  BUG_FIX("Bug Fix", "Analyze errors, formulate repair, and verify resolution"),
  SELF_IMPROVEMENT("Self-Improvement", "Autonomous inspection, self-extension, and validation"),
  AI_MODEL_INTEGRATION("AI Model Integration", "Configure and connect an AI provider/adapter"),
  VOICE_CAPABILITY_REQUEST("Voice Capability", "Configure voice input, TTS, or wake-word detection"),
  PROJECT_CREATION("Project Creation", "Initialize new modules or project workspaces"),
  TESTING_REQUEST("Testing Request", "Execute unit test suites and verify system checks"),
  SYSTEM_CONFIGURATION("System Configuration", "Adjust environment, models, or permission policies"),
  UNSUPPORTED_OR_BLOCKED("Blocked / Unsupported", "Requires unavailable platform runtime capabilities")
}

enum class ExecutionPhase(val stepNumber: Int, val title: String) {
  UNDERSTAND(1, "Phase 1: Understand"),
  PLAN(2, "Phase 2: Plan"),
  PREPARE(3, "Phase 3: Prepare Checkpoint"),
  IMPLEMENT(4, "Phase 4: Implement Changes"),
  REVIEW(5, "Phase 5: Review Code"),
  TEST(6, "Phase 6: Run Tests"),
  REPAIR(7, "Phase 7: Bounded Repair"),
  VERIFY(8, "Phase 8: Verify Outcome"),
  REPORT(9, "Phase 9: Report Result"),
  RELEASE(10, "Phase 10: Promote & Release")
}

enum class StepStatus {
  PENDING,
  IN_PROGRESS,
  SUCCESS,
  FAILED,
  SKIPPED
}

enum class TaskExecutionState(val label: String, val colorHex: Long) {
  PLANNED("Planned", 0xFF8EADC9),
  IN_PROGRESS("In progress", 0xFF00E5FF),
  IMPLEMENTED("Implemented", 0xFF1A8CFF),
  TESTS_PASSED("Tests passed", 0xFF00E676),
  PARTIALLY_SUCCESSFUL("Partially successful", 0xFFFFB300),
  BLOCKED("Blocked", 0xFFFF9100),
  FAILED("Failed", 0xFFFF3366)
}

enum class RiskLevel(val label: String) {
  LOW("Low - Safe Workspace Read/Analysis"),
  MEDIUM("Medium - Routine Reversible Code Changes"),
  HIGH("High - Sensitive Configuration / Dependency Changes"),
  CRITICAL("Critical - Destructive / External / Security Modification")
}

enum class AutonomyMode(val title: String, val description: String) {
  HIGH_AUTONOMY(
    "High Autonomy Mode",
    "Autonomously inspects, plans, creates files, runs tests, and applies bounded repairs. Approval requested ONLY for sensitive or destructive actions."
  ),
  GUIDED_MODE(
    "Operator Guided Mode",
    "Requires explicit operator confirmation before applying file modifications."
  )
}

data class TaskStep(
  val id: String,
  val phase: ExecutionPhase,
  val description: String,
  val toolName: String?,
  var status: StepStatus = StepStatus.PENDING,
  var output: String? = null,
  var error: String? = null
)

data class ExecutionEvidence(
  val filesInspected: List<String> = emptyList(),
  val filesModified: List<String> = emptyList(),
  val filesCreated: List<String> = emptyList(),
  val diffAdditions: Int = 0,
  val diffDeletions: Int = 0,
  val syntaxErrorsFound: Int = 0,
  val testsExecuted: Int = 0,
  val testsPassed: Int = 0,
  val testsFailed: Int = 0,
  val repairAttemptsUsed: Int = 0,
  val checkpointId: String? = null,
  val rollbackTriggered: Boolean = false,
  val executionLogs: List<String> = emptyList()
)

data class TaskPlan(
  val id: String,
  val userPrompt: String,
  val classification: TaskClassification,
  val summary: String,
  val targetFiles: List<String> = emptyList(),
  val riskLevel: RiskLevel = RiskLevel.LOW,
  val steps: List<TaskStep> = emptyList(),
  val requiresApproval: Boolean = false,
  val approvalExplanation: String? = null,
  var factualState: TaskExecutionState = TaskExecutionState.PLANNED,
  var evidence: ExecutionEvidence = ExecutionEvidence()
)

enum class ExecutionStatus(val label: String) {
  IDLE("ONLINE — STANDBY"),
  ANALYZING("ANALYZING WORKSPACE"),
  PLANNING("SYNTHESIZING TASK PLAN"),
  EXECUTING("HIGH AUTONOMY EXECUTION"),
  TESTING("RUNNING VERIFICATION SUITE"),
  REPAIRING("PERFORMING BOUNDED AUTO-REPAIR"),
  AWAITING_APPROVAL("AWAITING OPERATOR APPROVAL"),
  COMPLETE("TASK COMPLETE"),
  FAILED("ANOMALY DETECTED")
}
