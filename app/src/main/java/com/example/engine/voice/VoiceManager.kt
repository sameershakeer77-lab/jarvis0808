package com.example.engine.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceManager(private val context: Context) : RecognitionListener, TextToSpeech.OnInitListener {

  private var speechRecognizer: SpeechRecognizer? = null
  private var tts: TextToSpeech? = null

  private val _isListening = MutableStateFlow(false)
  val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  private val _liveTranscript = MutableStateFlow("")
  val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

  private val _soundLevel = MutableStateFlow(0f)
  val soundLevel: StateFlow<Float> = _soundLevel.asStateFlow()

  private val _voiceError = MutableStateFlow<String?>(null)
  val voiceError: StateFlow<String?> = _voiceError.asStateFlow()

  var onTranscriptFinalized: ((String) -> Unit)? = null

  private var isTtsInitialized = false

  init {
    initSpeechRecognizer()
    initTts()
  }

  private fun initSpeechRecognizer() {
    try {
      if (SpeechRecognizer.isRecognitionAvailable(context)) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
          setRecognitionListener(this@VoiceManager)
        }
      } else {
        _voiceError.value = "Speech recognition service unavailable on this device configuration."
      }
    } catch (e: Exception) {
      _voiceError.value = "Speech recognition initialization notice: ${e.message}"
    }
  }

  private fun initTts() {
    try {
      tts = TextToSpeech(context, this)
    } catch (e: Exception) {
      _voiceError.value = "Text-to-Speech initialization notice: ${e.message}"
    }
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      isTtsInitialized = true
      tts?.language = Locale.US
      tts?.setPitch(0.95f) // slightly deeper, sophisticated assistant timbre
      tts?.setSpeechRate(1.05f)
    }
  }

  fun startListening() {
    _voiceError.value = null
    _liveTranscript.value = ""

    if (speechRecognizer == null) {
      initSpeechRecognizer()
    }

    if (speechRecognizer == null) {
      _voiceError.value = "Speech recognition is not available on this device or emulator."
      return
    }

    try {
      val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toLanguageTag())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
      }
      speechRecognizer?.startListening(intent)
      _isListening.value = true
    } catch (e: Exception) {
      _isListening.value = false
      _voiceError.value = "Could not activate microphone: ${e.message}"
    }
  }

  fun stopListening() {
    try {
      speechRecognizer?.stopListening()
    } catch (_: Exception) {}
    _isListening.value = false
    _soundLevel.value = 0f
  }

  fun cancelListening() {
    try {
      speechRecognizer?.cancel()
    } catch (_: Exception) {}
    _isListening.value = false
    _liveTranscript.value = ""
    _soundLevel.value = 0f
  }

  fun speak(text: String) {
    if (!isTtsInitialized || tts == null) return
    val clean = text.replace(Regex("[*#_`\\[\\]]"), " ")
    tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "jarvis_tts_${System.currentTimeMillis()}")
    _isSpeaking.value = true
  }

  fun stopSpeaking() {
    tts?.stop()
    _isSpeaking.value = false
  }

  fun destroy() {
    try {
      speechRecognizer?.destroy()
      tts?.stop()
      tts?.shutdown()
    } catch (_: Exception) {}
  }

  // RecognitionListener callbacks
  override fun onReadyForSpeech(params: Bundle?) {
    _isListening.value = true
  }

  override fun onBeginningOfSpeech() {}

  override fun onRmsChanged(rmsdB: Float) {
    _soundLevel.value = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
  }

  override fun onBufferReceived(buffer: ByteArray?) {}

  override fun onEndOfSpeech() {
    _isListening.value = false
    _soundLevel.value = 0f
  }

  override fun onError(error: Int) {
    _isListening.value = false
    _soundLevel.value = 0f
    val errorDescription = when (error) {
      SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
      SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error"
      SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "RECORD_AUDIO permission required"
      SpeechRecognizer.ERROR_NETWORK -> "Network communication error"
      SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network operation timed out"
      SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try speaking closer to microphone."
      SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy"
      SpeechRecognizer.ERROR_SERVER -> "Server recognition error"
      SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
      else -> "Speech recognition error code: $error"
    }
    _voiceError.value = errorDescription
  }

  override fun onResults(results: Bundle?) {
    _isListening.value = false
    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
    if (!matches.isNullOrEmpty()) {
      val text = matches[0]
      _liveTranscript.value = text
      onTranscriptFinalized?.invoke(text)
    }
  }

  override fun onPartialResults(partialResults: Bundle?) {
    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
    if (!matches.isNullOrEmpty()) {
      _liveTranscript.value = matches[0]
    }
  }

  override fun onEvent(eventType: Int, params: Bundle?) {}
}
