package com.example.myfirstandroidapplication

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myfirstandroidapplication.adapters.ReaderTheme
import com.example.myfirstandroidapplication.data.AuthManager
import com.example.myfirstandroidapplication.data.ExportManager
import com.example.myfirstandroidapplication.data.PreferencesManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var preferencesManager: PreferencesManager
    private var currentTheme: ReaderTheme = ReaderTheme.SEPIA

    private var settingsScrollView: ScrollView? = null
    private lateinit var cardAccountProfile: View
    private lateinit var tvSectionAccount: TextView
    private lateinit var tvAccountStatus: TextView
    private lateinit var tvAccountEmail: TextView
    private lateinit var btnGoogleSignIn: MaterialButton
    private lateinit var btnGoogleSignOut: MaterialButton

    private lateinit var switchAutoTheme: Switch
    private lateinit var spinnerThemeMode: Spinner
    private lateinit var tvCurrentTheme: TextView
    private lateinit var switchCloudSync: Switch
    private lateinit var tvSyncStatus: TextView
    private lateinit var btnExportJSON: Button
    private lateinit var btnExportCSV: Button
    private lateinit var btnExportMarkdown: Button

    private lateinit var tvSectionTheme: TextView
    private lateinit var tvLabelAutoTheme: TextView
    private lateinit var tvLabelManualTheme: TextView
    private lateinit var tvSectionCloud: TextView
    private lateinit var tvLabelCloudSync: TextView
    private lateinit var tvSectionExport: TextView
    private lateinit var tvLabelExportSubtext: TextView

    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (!idToken.isNullOrEmpty()) {
                    handleGoogleSignInSuccess(idToken)
                } else {
                    Toast.makeText(requireContext(), "Google sign in failed: Missing ID Token", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Google sign in failed (code ${e.statusCode}): ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())
        setupGoogleSignInClient()
        initViews(view)
        setupSpinner()
        setupListeners()
        loadSettings()
        updateAccountUI()

        (activity as? MainActivity)?.let {
            currentTheme = it.currentTheme
        }
        applyTheme(currentTheme)
    }

    private fun setupGoogleSignInClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun initViews(view: View) {
        settingsScrollView = view.findViewById(R.id.settingsScrollView)
        cardAccountProfile = view.findViewById(R.id.cardAccountProfile)
        tvSectionAccount = view.findViewById(R.id.tvSectionAccount)
        tvAccountStatus = view.findViewById(R.id.tvAccountStatus)
        tvAccountEmail = view.findViewById(R.id.tvAccountEmail)
        btnGoogleSignIn = view.findViewById(R.id.btnGoogleSignIn)
        btnGoogleSignOut = view.findViewById(R.id.btnGoogleSignOut)

        switchAutoTheme = view.findViewById(R.id.switchAutoTheme)
        spinnerThemeMode = view.findViewById(R.id.spinnerThemeMode)
        tvCurrentTheme = view.findViewById(R.id.tvCurrentTheme)
        switchCloudSync = view.findViewById(R.id.switchCloudSync)
        tvSyncStatus = view.findViewById(R.id.tvSyncStatus)
        btnExportJSON = view.findViewById(R.id.btnExportJSON)
        btnExportCSV = view.findViewById(R.id.btnExportCSV)
        btnExportMarkdown = view.findViewById(R.id.btnExportMarkdown)

        tvSectionTheme = view.findViewById(R.id.tvSectionTheme)
        tvLabelAutoTheme = view.findViewById(R.id.tvLabelAutoTheme)
        tvLabelManualTheme = view.findViewById(R.id.tvLabelManualTheme)
        tvSectionCloud = view.findViewById(R.id.tvSectionCloud)
        tvLabelCloudSync = view.findViewById(R.id.tvLabelCloudSync)
        tvSectionExport = view.findViewById(R.id.tvSectionExport)
        tvLabelExportSubtext = view.findViewById(R.id.tvLabelExportSubtext)
    }

    private fun setupSpinner() {
        val themes = arrayOf("Light Mode", "Sepia Mode", "Night Mode")
        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, themes) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                (v as? TextView)?.setTextColor(getThemeTextColor())
                return v
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getDropDownView(position, convertView, parent)
                val ctx = context
                val cardBg = when (currentTheme) {
                    ReaderTheme.SEPIA -> ContextCompat.getColor(ctx, R.color.kindle_sepia_card)
                    ReaderTheme.LIGHT -> ContextCompat.getColor(ctx, R.color.kindle_light_card)
                    ReaderTheme.DARK -> ContextCompat.getColor(ctx, R.color.kindle_dark_card)
                }
                v.setBackgroundColor(cardBg)
                (v as? TextView)?.setTextColor(getThemeTextColor())
                return v
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerThemeMode.adapter = adapter
        spinnerThemeMode.setSelection(1)
    }

    private fun getThemeTextColor(): Int {
        val ctx = context ?: return 0xFF000000.toInt()
        return when (currentTheme) {
            ReaderTheme.SEPIA -> ContextCompat.getColor(ctx, R.color.kindle_sepia_text)
            ReaderTheme.LIGHT -> ContextCompat.getColor(ctx, R.color.kindle_light_text)
            ReaderTheme.DARK -> ContextCompat.getColor(ctx, R.color.kindle_dark_text)
        }
    }

    private fun setupListeners() {
        btnGoogleSignIn.setOnClickListener {
            launchGoogleSignIn()
        }

        btnGoogleSignOut.setOnClickListener {
            signOutGoogle()
        }

        switchAutoTheme.setOnCheckedChangeListener { _, isChecked ->
            if (view != null && switchAutoTheme.isPressed) {
                lifecycleScope.launch {
                    preferencesManager.setAutoThemeEnabled(isChecked)
                    updateThemeStatus()
                }
            }
        }

        spinnerThemeMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedTheme = when (position) {
                    0 -> ReaderTheme.LIGHT
                    1 -> ReaderTheme.SEPIA
                    2 -> ReaderTheme.DARK
                    else -> ReaderTheme.SEPIA
                }
                if (selectedTheme != currentTheme) {
                    currentTheme = selectedTheme
                    (activity as? MainActivity)?.applyThemeFromSettings(selectedTheme)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        switchCloudSync.setOnCheckedChangeListener { _, isChecked ->
            if (view != null && switchCloudSync.isPressed) {
                lifecycleScope.launch {
                    preferencesManager.setCloudSyncEnabled(isChecked)
                    updateSyncStatus()
                    if (isChecked) {
                        Toast.makeText(requireContext(), "Syncing with cloud...", Toast.LENGTH_SHORT).show()
                        preferencesManager.syncFromCloud()
                        preferencesManager.syncToCloud()
                        Toast.makeText(requireContext(), "Cloud sync complete!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnExportJSON.setOnClickListener { exportData("json") }
        btnExportCSV.setOnClickListener { exportData("csv") }
        btnExportMarkdown.setOnClickListener { exportData("markdown") }
    }

    private fun loadSettings() {
        lifecycleScope.launch {
            try {
                preferencesManager.autoThemeEnabled.collect { enabled ->
                    if (::switchAutoTheme.isInitialized) {
                        switchAutoTheme.isChecked = enabled
                        updateThemeStatus()
                    }
                }
            } catch (e: Exception) {}
        }

        lifecycleScope.launch {
            try {
                preferencesManager.cloudSyncEnabled.collect { enabled ->
                    if (::switchCloudSync.isInitialized) {
                        switchCloudSync.isChecked = enabled
                        updateSyncStatus()
                    }
                }
            } catch (e: Exception) {}
        }
    }

    private fun updateThemeStatus() {
        if (!::tvCurrentTheme.isInitialized || !::switchAutoTheme.isInitialized) return
        val themeName = when (currentTheme) {
            ReaderTheme.LIGHT -> "Light Mode"
            ReaderTheme.SEPIA -> "Sepia Mode"
            ReaderTheme.DARK -> "Night Mode"
        }
        tvCurrentTheme.text = if (switchAutoTheme.isChecked) {
            "Auto (Currently: $themeName)"
        } else {
            themeName
        }
    }

    private fun updateSyncStatus() {
        if (!::tvSyncStatus.isInitialized || !::switchCloudSync.isInitialized) return
        tvSyncStatus.text = if (switchCloudSync.isChecked) {
            "Sync Enabled"
        } else {
            "Sync Disabled"
        }
    }

    private fun launchGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleGoogleSignInSuccess(idToken: String) {
        btnGoogleSignIn.isEnabled = false
        btnGoogleSignIn.text = "Signing in..."
        lifecycleScope.launch {
            val result = AuthManager.signInWithGoogleCredential(requireContext(), idToken)
            btnGoogleSignIn.isEnabled = true
            btnGoogleSignIn.text = "Sign in with Google"

            result.onSuccess { displayName ->
                Toast.makeText(requireContext(), "Signed in as $displayName", Toast.LENGTH_SHORT).show()
                updateAccountUI()
                // Sync data automatically upon signing in
                preferencesManager.syncFromCloud()
                preferencesManager.syncToCloud()
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Sign in failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun signOutGoogle() {
        lifecycleScope.launch {
            googleSignInClient.signOut()
            AuthManager.signOut(requireContext())
            preferencesManager.setAuthChoiceCompleted(false)
            Toast.makeText(requireContext(), "Signed out", Toast.LENGTH_SHORT).show()
            updateAccountUI()
        }
    }

    private fun updateAccountUI() {
        if (!::tvAccountStatus.isInitialized) return
        val isSigned = AuthManager.isGoogleSignedIn()
        val user = AuthManager.getCurrentGoogleUser()

        if (isSigned && user != null) {
            val name = user.displayName?.takeIf { it.isNotBlank() } ?: "User"
            val email = user.email ?: ""
            tvAccountStatus.text = "Signed in as $name"
            tvAccountEmail.text = if (email.isNotEmpty()) email else "Google Account connected"
            btnGoogleSignIn.visibility = View.GONE
            btnGoogleSignOut.visibility = View.VISIBLE
        } else {
            tvAccountStatus.text = "Not signed in (Anonymous)"
            tvAccountEmail.text = "Sign in to display your real name on quizzes and sync your progress across devices."
            btnGoogleSignIn.visibility = View.VISIBLE
            btnGoogleSignOut.visibility = View.GONE
        }
    }

    private fun exportData(format: String) {
        lifecycleScope.launch {
            try {
                Toast.makeText(requireContext(), "Generating $format export...", Toast.LENGTH_SHORT).show()
                val exportManager = ExportManager(requireContext())
                exportManager.exportData(format)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Export failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun applyTheme(theme: ReaderTheme) {
        currentTheme = theme
        updateThemeStatus()

        val ctx = context ?: return
        val root = settingsScrollView ?: return

        val bgRes = when (theme) {
            ReaderTheme.SEPIA -> R.color.kindle_sepia_bg
            ReaderTheme.LIGHT -> R.color.kindle_light_bg
            ReaderTheme.DARK -> R.color.kindle_dark_bg
        }

        val textColor = when (theme) {
            ReaderTheme.SEPIA -> ContextCompat.getColor(ctx, R.color.kindle_sepia_text)
            ReaderTheme.LIGHT -> ContextCompat.getColor(ctx, R.color.kindle_light_text)
            ReaderTheme.DARK -> ContextCompat.getColor(ctx, R.color.kindle_dark_text)
        }

        val subtextColor = when (theme) {
            ReaderTheme.SEPIA -> ContextCompat.getColor(ctx, R.color.kindle_sepia_subtext)
            ReaderTheme.LIGHT -> ContextCompat.getColor(ctx, R.color.kindle_light_subtext)
            ReaderTheme.DARK -> ContextCompat.getColor(ctx, R.color.kindle_dark_subtext)
        }

        val cardBg = when (theme) {
            ReaderTheme.SEPIA -> ContextCompat.getColor(ctx, R.color.kindle_sepia_card)
            ReaderTheme.LIGHT -> ContextCompat.getColor(ctx, R.color.kindle_light_card)
            ReaderTheme.DARK -> ContextCompat.getColor(ctx, R.color.kindle_dark_card)
        }

        root.setBackgroundColor(ContextCompat.getColor(ctx, bgRes))

        if (::cardAccountProfile.isInitialized) {
            cardAccountProfile.setBackgroundColor(cardBg)
            tvSectionAccount.setTextColor(textColor)
            tvAccountStatus.setTextColor(textColor)
            tvAccountEmail.setTextColor(subtextColor)
            btnGoogleSignOut.setTextColor(textColor)
            btnGoogleSignOut.strokeColor = android.content.res.ColorStateList.valueOf(textColor)
        }

        tvSectionTheme.setTextColor(textColor)
        tvLabelAutoTheme.setTextColor(textColor)
        tvLabelManualTheme.setTextColor(textColor)
        tvSectionCloud.setTextColor(textColor)
        tvLabelCloudSync.setTextColor(textColor)
        tvSectionExport.setTextColor(textColor)

        tvCurrentTheme.setTextColor(subtextColor)
        tvSyncStatus.setTextColor(subtextColor)
        tvLabelExportSubtext.setTextColor(subtextColor)

        // Update spinner selection index if needed
        val selectionIndex = when (theme) {
            ReaderTheme.LIGHT -> 0
            ReaderTheme.SEPIA -> 1
            ReaderTheme.DARK -> 2
        }
        if (::spinnerThemeMode.isInitialized && spinnerThemeMode.selectedItemPosition != selectionIndex) {
            spinnerThemeMode.setSelection(selectionIndex)
        }
    }
}