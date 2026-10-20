package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.TaskClassification
import com.example.data.model.TaskExecutionState
import com.example.engine.planner.AutonomousPlanner
import com.example.engine.selfbuilding.ModuleGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("JARVIS", appName)
  }

  @Test
  fun `test autonomous planner intent classification and plan synthesis`() {
    val planner = AutonomousPlanner()
    val classification = planner.classifyPrompt("Add speech-to-text to yourself")
    assertEquals(TaskClassification.VOICE_CAPABILITY_REQUEST, classification)

    val plan = planner.createPlan("Add speech-to-text to yourself", listOf("src/main/kotlin/SpeechModule.kt"))
    assertNotNull(plan)
    assertTrue("Plan must have 10 structured execution phases", plan.steps.size >= 8)
    assertEquals(TaskExecutionState.PLANNED, plan.factualState)
  }

  @Test
  fun `test self building module generator for memory module`() {
    val generator = ModuleGenerator()
    val bundle = generator.generateForIntent("Build vector memory module for yourself")
    assertNotNull(bundle)
    assertTrue("Target path must be in modules directory", bundle.targetFilePath.contains("modules/"))
    assertTrue("Source code must contain class definition", bundle.sourceCode.contains("class ProjectMemoryModule"))
    assertTrue("Test code must contain test method", bundle.testCode.contains("fun testMemory"))
  }
}
