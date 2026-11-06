package com.example.myfirstandroidapplication.utils

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class VoiceSearchManager(private val fragment: Fragment) {

    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery: StateFlow<String?> = _searchQuery

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private var speechRecognizerLauncher: ActivityResultLauncher<Intent>? = null

    fun initialize() {
        speechRecognizerLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            _isListening.value = false
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (!matches.isNullOrEmpty()) {
                    _searchQuery.value = matches[0]
                }
            }
        }
    }

    fun startVoiceSearch() {
        if (!isAvailable()) {
            _searchQuery.value = null
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to search...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        _isListening.value = true
        speechRecognizerLauncher?.launch(intent)
    }

    fun isAvailable(): Boolean {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        val packageManager = fragment.context?.packageManager
        return packageManager?.queryIntentActivities(intent, 0)?.isNotEmpty() == true
    }

    fun clearSearchQuery() {
        _searchQuery.value = null
    }
}
