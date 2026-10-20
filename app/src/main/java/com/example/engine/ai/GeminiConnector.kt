package com.example.engine.ai

import com.example.BuildConfig
import com.example.data.model.ModelConnectorInfo
import com.example.data.model.ModelStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiConnector {

  private var customApiKey: String? = null
  private var selectedModelId: String = "gemini-2.5-flash"

  private val providers: Map<String, AiModelProvider> = mapOf(
    "gemini-2.5-flash" to GeminiProvider("gemini-2.5-flash", "Gemini 2.5 Flash (Recommended)", isPro = false),
    "gemini-2.5-pro" to GeminiProvider("gemini-2.5-pro", "Gemini 2.5 Pro", isPro = true),
    "gemini-1.5-flash" to GeminiProvider("gemini-1.5-flash", "Gemini 1.5 Flash", isPro = false),
    "claude-3-7-sonnet" to ClaudeAdapter(),
    "gpt-4o" to OpenAiAdapter(),
    "local-heuristic-brain" to LocalAutonomousBrain()
  )

  fun setCustomApiKey(key: String?) {
    customApiKey = key?.trim()
  }

  fun setSelectedModel(modelId: String) {
    if (providers.containsKey(modelId)) {
      selectedModelId = modelId
    }
  }

  fun getSelectedModelId(): String = selectedModelId

  fun getActiveApiKey(): String {
    return if (!customApiKey.isNullOrBlank()) {
      customApiKey!!
    } else {
      try {
        BuildConfig.GEMINI_API_KEY
      } catch (e: Exception) {
        ""
      }
    }
  }

  fun getModelRegistry(): List<ModelConnectorInfo> {
    val key = getActiveApiKey()
    return providers.values.map { p ->
      val isConfigured = p.isConfigured(key)
      val status = when {
        p is LocalAutonomousBrain -> ModelStatus.ONLINE
        isConfigured -> ModelStatus.ONLINE
        p.providerName.contains("Adapter") -> ModelStatus.CONFIGURED
        else -> ModelStatus.KEY_REQUIRED
      }
      ModelConnectorInfo(
        provider = p.providerName,
        modelId = p.modelId,
        displayName = p.displayName,
        status = status,
        isConfigured = isConfigured,
        capabilities = p.capabilities
      )
    }
  }

  suspend fun generateResponse(
    prompt: String,
    systemInstruction: String,
    contextFilesSummary: String
  ): Result<String> = withContext(Dispatchers.IO) {
    val apiKey = getActiveApiKey()
    val provider = providers[selectedModelId] ?: providers["local-heuristic-brain"]!!

    if (!provider.isConfigured(apiKey) && provider !is LocalAutonomousBrain) {
      val fallback = providers["local-heuristic-brain"]!!
      val localRes = fallback.generateResponse(prompt, systemInstruction, contextFilesSummary, "")
      return@withContext Result.success(
        "Notice: Provider '${provider.displayName}' requires active API key. Engaged local autonomous planner:\n\n" +
          localRes.getOrDefault("")
      )
    }

    val res = provider.generateResponse(prompt, systemInstruction, contextFilesSummary, apiKey)
    if (res.isSuccess) {
      res
    } else {
      // Graceful fallback to local autonomous brain on network timeout or quota
      val fallback = providers["local-heuristic-brain"]!!
      val localRes = fallback.generateResponse(prompt, systemInstruction, contextFilesSummary, "")
      Result.success(
        "Notice: Remote API call encountered [${res.exceptionOrNull()?.message}]. Seamlessly engaged local autonomous planner:\n\n" +
          localRes.getOrDefault("")
      )
    }
  }
}
