package com.example.myfirstandroidapplication.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class TextToSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    private var currentText: String = ""
    private var currentStartPosition: Int = 0

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                _isInitialized.value = false
            } else {
                _isInitialized.value = true
                setupUtteranceListener()
            }
        } else {
            _isInitialized.value = false
        }
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }
        })
    }

    fun speak(text: String) {
        if (!_isInitialized.value) return

        currentText = text
        currentStartPosition = 0

        tts?.let {
            it.stop()
            it.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_utterance")
        }
    }

    fun speakFromPosition(text: String, position: Int) {
        if (!_isInitialized.value) return

        currentText = text
        currentStartPosition = position

        tts?.let {
            it.stop()
            it.speak(text.substring(position), TextToSpeech.QUEUE_FLUSH, null, "tts_utterance")
        }
    }

    fun pause() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun resume() {
        if (currentText.isNotEmpty() && currentStartPosition < currentText.length) {
            speakFromPosition(currentText, currentStartPosition)
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        currentText = ""
        currentStartPosition = 0
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }

    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch)
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
