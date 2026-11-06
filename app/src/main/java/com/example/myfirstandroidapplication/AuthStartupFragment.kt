package com.example.myfirstandroidapplication

import android.app.Activity
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myfirstandroidapplication.adapters.ReaderTheme
import com.example.myfirstandroidapplication.data.AuthManager
import com.example.myfirstandroidapplication.data.PreferencesManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class AuthStartupFragment : Fragment(R.layout.fragment_auth_startup) {

    private lateinit var authStartupRoot: View
    private lateinit var cardAuthOptions: View
    private lateinit var ivAppIcon: ImageView
    private lateinit var tvWelcomeTitle: TextView
    private lateinit var tvWelcomeSubtitle: TextView
    private lateinit var btnGoogleSignIn: MaterialButton
    private lateinit var tvGoogleHint: TextView
    private lateinit var dividerAuth: View
    private lateinit var btnAnonymous: MaterialButton
    private lateinit var tvAnonymousHint: TextView
    private lateinit var progressAuth: ProgressBar

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var preferencesManager: PreferencesManager
    private var currentTheme: ReaderTheme = ReaderTheme.SEPIA

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
                    setLoading(false)
                    Toast.makeText(requireContext(), "Google sign in failed: Missing ID Token", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                setLoading(false)
                Toast.makeText(requireContext(), "Google sign in failed (code ${e.statusCode}): ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        } else {
            setLoading(false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferencesManager = PreferencesManager(requireContext())
        setupGoogleSignInClient()
        initViews(view)
        setupListeners()

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
        authStartupRoot = view.findViewById(R.id.authStartupRoot)
        cardAuthOptions = view.findViewById(R.id.cardAuthOptions)
        ivAppIcon = view.findViewById(R.id.ivAuthAppIcon)
        tvWelcomeTitle = view.findViewById(R.id.tvAuthWelcomeTitle)
        tvWelcomeSubtitle = view.findViewById(R.id.tvAuthWelcomeSubtitle)
        btnGoogleSignIn = view.findViewById(R.id.btnStartupGoogleSignIn)
        tvGoogleHint = view.findViewById(R.id.tvGoogleSignInHint)
        dividerAuth = view.findViewById(R.id.dividerAuth)
        btnAnonymous = view.findViewById(R.id.btnStartupAnonymous)
        tvAnonymousHint = view.findViewById(R.id.tvAnonymousHint)
        progressAuth = view.findViewById(R.id.progressStartupAuth)
    }

    private fun setupListeners() {
        btnGoogleSignIn.setOnClickListener {
            setLoading(true)
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        btnAnonymous.setOnClickListener {
            continueAnonymously()
        }
    }

    private fun setLoading(loading: Boolean) {
        progressAuth.visibility = if (loading) View.VISIBLE else View.GONE
        btnGoogleSignIn.isEnabled = !loading
        btnAnonymous.isEnabled = !loading
    }

    private fun handleGoogleSignInSuccess(idToken: String) {
        setLoading(true)
        lifecycleScope.launch {
            val result = AuthManager.signInWithGoogleCredential(requireContext(), idToken)
            setLoading(false)
            result.onSuccess { displayName ->
                Toast.makeText(requireContext(), "Signed in as $displayName", Toast.LENGTH_SHORT).show()
                preferencesManager.setAuthChoiceCompleted(true)
                preferencesManager.syncFromCloud()
                (activity as? MainActivity)?.onAuthCompleted()
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Sign in failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun continueAnonymously() {
        setLoading(true)
        lifecycleScope.launch {
            AuthManager.ensureUserId(requireContext())
            preferencesManager.setAuthChoiceCompleted(true)
            setLoading(false)
            Toast.makeText(requireContext(), "Continuing as Guest", Toast.LENGTH_SHORT).show()
            (activity as? MainActivity)?.onAuthCompleted()
        }
    }

    fun applyTheme(theme: ReaderTheme) {
        currentTheme = theme
        val ctx = context ?: return
        if (!::authStartupRoot.isInitialized) return

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

        authStartupRoot.setBackgroundColor(ContextCompat.getColor(ctx, bgRes))
        cardAuthOptions.setBackgroundColor(cardBg)
        tvWelcomeTitle.setTextColor(textColor)
        tvWelcomeSubtitle.setTextColor(subtextColor)
        tvGoogleHint.setTextColor(subtextColor)
        tvAnonymousHint.setTextColor(subtextColor)
        btnAnonymous.setTextColor(textColor)
        btnAnonymous.strokeColor = ColorStateList.valueOf(textColor)
    }
}
