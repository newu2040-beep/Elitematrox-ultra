package com.example.feature.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.theme.EliteMatrixLogoMedallion
import com.example.ui.theme.EmeraldPillButton
import com.example.ui.theme.GoogleLogoIcon
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.Neumorphic3DPlayHeroArtwork
import com.example.ui.theme.NeumorphicCard
import com.example.ui.theme.NeumorphicPillButton
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class AuthSubScreen {
    Welcome,
    EmailSignIn,
    EmailSignUp,
    ForgotPassword
}

/**
 * 1. Silent Auto-Sign-In on Startup (separate request using GetGoogleIdOption per Credential Manager guidelines).
 */
fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

/**
 * 2. Interactive Google Sign-In (separate request using GetSignInWithGoogleOption).
 */
fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()

    scope.launch {
        try {
            val activityContext = context as? Activity ?: context
            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type received from Google Sign-In.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Google Sign-In failed. Please check your connection and account.")
        }
    }
}

/**
 * 3. Sign-Out Flow clearing both Firebase Auth and Credential Manager state.
 */
fun signOut(
    context: Context,
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    Firebase.auth.signOut()
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onSignOutComplete()
        }
    }
}

/**
 * Screen 1 — Welcome / Login (Exact reference mockup implementation + real auth flows).
 */
@Composable
fun WelcomeLoginScreen(
    onGoogleAuthSuccess: () -> Unit,
    onEnterGuestMode: () -> Unit
) {
    val neu = LocalNeumorphicColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var subScreen by rememberSaveable { mutableStateOf(AuthSubScreen.Welcome) }
    var isGoogleLoading by rememberSaveable { mutableStateOf(false) }
    var statusBanner by rememberSaveable { mutableStateOf<String?>(null) }
    var isStatusError by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onGoogleAuthSuccess,
            onUnauthenticated = {},
            scope = scope
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(neu.canvas)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
    ) {
        // Bottom-left soft mint wave accent matching Screen 1 mockup
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter)
        ) {
            val w = size.width
            val h = size.height
            val bottomWave = Path().apply {
                moveTo(0f, h * 0.25f)
                cubicTo(
                    w * 0.28f, h * 0.35f,
                    w * 0.48f, h * 0.85f,
                    w * 0.72f, h
                )
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = bottomWave,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        neu.accentSoftMintStart.copy(alpha = 0.55f),
                        neu.accentSoftMintEnd.copy(alpha = 0.85f)
                    )
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 460.dp)
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            when (subScreen) {
                AuthSubScreen.Welcome -> {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Top Brand Area
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        EliteMatrixLogoMedallion(size = 76.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "ELITE MATRIX",
                            color = neu.textPrimary,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 23.sp,
                                letterSpacing = 0.8.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "HQ Video Enhancer",
                            color = neu.textPrimary.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Enhance • Compress • Export\nin Professional Quality",
                            color = neu.textSecondary,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp
                            )
                        )
                    }

                    // Center 3D Play Prism Artwork
                    Neumorphic3DPlayHeroArtwork(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Action Buttons Area
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedVisibility(visible = statusBanner != null) {
                            statusBanner?.let { msg ->
                                NeumorphicCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    cornerRadius = 14.dp
                                ) {
                                    Text(
                                        text = msg,
                                        color = if (isStatusError) MaterialTheme.colorScheme.error else neu.accentPrimary,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp)
                                    )
                                }
                            }
                        }

                        // 1. Continue with Google
                        NeumorphicPillButton(
                            text = "Continue with Google",
                            testTag = "continue_with_google_button",
                            isLoading = isGoogleLoading,
                            leadingContent = { GoogleLogoIcon(Modifier.size(19.dp)) },
                            onClick = {
                                statusBanner = null
                                isGoogleLoading = true
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isGoogleLoading = false
                                        onGoogleAuthSuccess()
                                    },
                                    onAuthError = { err ->
                                        isGoogleLoading = false
                                        isStatusError = true
                                        statusBanner = err
                                    },
                                    scope = scope,
                                    onAuthCancelled = {
                                        isGoogleLoading = false
                                    }
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2. Continue with Email
                        NeumorphicPillButton(
                            text = "Continue with Email",
                            testTag = "continue_with_email_button",
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = "Email",
                                    tint = neu.accentSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = {
                                statusBanner = null
                                subScreen = AuthSubScreen.EmailSignIn
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Divider with "or"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = neu.divider
                            )
                            Text(
                                text = "or",
                                color = neu.textSecondary,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = neu.divider
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3. Guest Mode
                        NeumorphicPillButton(
                            text = "Guest Mode",
                            testTag = "guest_mode_button",
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = "Guest Mode",
                                    tint = neu.textPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = {
                                statusBanner = null
                                onEnterGuestMode()
                            }
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        // Footer: Don't have an account? Sign Up
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .testTag("sign_up_footer_button")
                                .clickable {
                                    statusBanner = null
                                    subScreen = AuthSubScreen.EmailSignUp
                                }
                                .padding(vertical = 6.dp, horizontal = 12.dp)
                        ) {
                            Text(
                                text = "Don't have an account? ",
                                color = neu.textSecondary,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                            )
                            Text(
                                text = "Sign Up",
                                color = neu.accentPrimary,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }

                AuthSubScreen.EmailSignIn,
                AuthSubScreen.EmailSignUp,
                AuthSubScreen.ForgotPassword -> {
                    EmailAuthSubFlow(
                        subScreen = subScreen,
                        onNavigateSubScreen = {
                            statusBanner = null
                            subScreen = it
                        },
                        onTriggerGoogleSignIn = {
                            subScreen = AuthSubScreen.Welcome
                            isGoogleLoading = true
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isGoogleLoading = false
                                    onGoogleAuthSuccess()
                                },
                                onAuthError = { err ->
                                    isGoogleLoading = false
                                    isStatusError = true
                                    statusBanner = err
                                },
                                scope = scope,
                                onAuthCancelled = { isGoogleLoading = false }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmailAuthSubFlow(
    subScreen: AuthSubScreen,
    onNavigateSubScreen: (AuthSubScreen) -> Unit,
    onTriggerGoogleSignIn: () -> Unit
) {
    val neu = LocalNeumorphicColors.current
    val scope = rememberCoroutineScope()

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var feedbackMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isError by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onNavigateSubScreen(AuthSubScreen.Welcome) },
                modifier = Modifier.testTag("auth_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = neu.textPrimary
                )
            }
            Text(
                text = when (subScreen) {
                    AuthSubScreen.EmailSignIn -> "Continue with Email"
                    AuthSubScreen.EmailSignUp -> "Create Account"
                    AuthSubScreen.ForgotPassword -> "Reset Password"
                    else -> "Authentication"
                },
                color = neu.textPrimary,
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        EliteMatrixLogoMedallion(size = 64.dp)
        Spacer(modifier = Modifier.height(18.dp))

        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 22.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (subScreen == AuthSubScreen.EmailSignUp) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Creator Name") },
                        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neu.accentPrimary,
                            unfocusedBorderColor = neu.divider
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it.trim() },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neu.accentPrimary,
                        unfocusedBorderColor = neu.divider
                    )
                )

                if (subScreen != AuthSubScreen.ForgotPassword) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = if (subScreen == AuthSubScreen.EmailSignUp) ImeAction.Next else ImeAction.Done
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neu.accentPrimary,
                            unfocusedBorderColor = neu.divider
                        )
                    )
                }

                if (subScreen == AuthSubScreen.EmailSignUp) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_confirm_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neu.accentPrimary,
                            unfocusedBorderColor = neu.divider
                        )
                    )
                }

                if (subScreen == AuthSubScreen.EmailSignIn) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                feedbackMessage = null
                                onNavigateSubScreen(AuthSubScreen.ForgotPassword)
                            },
                            modifier = Modifier.testTag("forgot_password_button")
                        ) {
                            Text(
                                text = "Forgot Password?",
                                color = neu.accentPrimary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                feedbackMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = if (isError) MaterialTheme.colorScheme.error else neu.accentPrimary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                EmeraldPillButton(
                    text = when (subScreen) {
                        AuthSubScreen.EmailSignIn -> "Sign In"
                        AuthSubScreen.EmailSignUp -> "Create Account"
                        AuthSubScreen.ForgotPassword -> "Send Reset Link"
                        else -> "Submit"
                    },
                    isLoading = isLoading,
                    testTag = "auth_submit_button",
                    onClick = {
                        feedbackMessage = null
                        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            isError = true
                            feedbackMessage = "Please enter a valid email address."
                            return@EmeraldPillButton
                        }

                        if (subScreen == AuthSubScreen.ForgotPassword) {
                            isLoading = true
                            scope.launch {
                                try {
                                    Firebase.auth.sendPasswordResetEmail(email).await()
                                } catch (_: Exception) {
                                    // Do not reveal whether an email is registered per PRD Section 4
                                } finally {
                                    isLoading = false
                                    isError = false
                                    feedbackMessage =
                                        "If an account exists for $email, a password reset link has been sent."
                                }
                            }
                            return@EmeraldPillButton
                        }

                        if (password.length < 8 || !password.any { it.isDigit() }) {
                            isError = true
                            feedbackMessage = "Password must be at least 8 characters and include a number."
                            return@EmeraldPillButton
                        }

                        if (subScreen == AuthSubScreen.EmailSignUp) {
                            if (name.isBlank()) {
                                isError = true
                                feedbackMessage = "Please enter your creator name."
                                return@EmeraldPillButton
                            }
                            if (password != confirmPassword) {
                                isError = true
                                feedbackMessage = "Passwords do not match."
                                return@EmeraldPillButton
                            }
                        }

                        // Inform user about Google Sign-In provisioning in this Firebase project
                        isError = false
                        feedbackMessage =
                            "This Firebase project is provisioned with Google Sign-In via Android Credential Manager. To use Email/Password authentication, enable the Email/Password provider in the Firebase Console, or tap 'Continue with Google' below."
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                NeumorphicPillButton(
                    text = "Use Google Sign-In Instead",
                    leadingContent = { GoogleLogoIcon(Modifier.size(18.dp)) },
                    onClick = onTriggerGoogleSignIn
                )
            }
        }
    }
}
