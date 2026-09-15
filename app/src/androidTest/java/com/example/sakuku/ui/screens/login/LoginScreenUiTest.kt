package com.example.sakuku.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.sakuku.ui.theme.SakukuTheme
import org.junit.Rule
import org.junit.Test

// Nge-test LoginScreenContent (versi stateless, internal) langsung - BUKAN LoginScreen yang
// stateful (butuh Hilt + hiltViewModel(), gak bisa dites tanpa DI graph beneran nyala). Pola ini
// niru LoginScreenPreview yang udah ada di LoginScreen.kt: kasih state + callback lokal, gak
// perlu MainActivity/navigasi/network sama sekali - jadi test-nya cepat & gak gantung backend.
class LoginScreenUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setLoginContent(
        initialState: LoginUiState = LoginUiState(identifier = "", password = ""),
        onLoginClick: () -> Unit = {}
    ) {
        composeRule.setContent {
            SakukuTheme {
                var uiState by remember { mutableStateOf(initialState) }
                LoginScreenContent(
                    uiState = uiState,
                    onIdentifierChange = { uiState = uiState.copy(identifier = it, errorMessage = null) },
                    onPasswordChange = { uiState = uiState.copy(password = it, errorMessage = null) },
                    onRememberMeChange = { uiState = uiState.copy(rememberMe = it) },
                    onLoginClick = onLoginClick,
                    onNavigateToRegister = {},
                    onBack = {}
                )
            }
        }
    }

    @Test
    fun displaysTitleAndFields() {
        setLoginContent()

        composeRule.onNodeWithText("Masuk", substring = false, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("login_email_field").assertIsDisplayed()
        composeRule.onNodeWithTag("login_password_field").assertIsDisplayed()
        composeRule.onNodeWithTag("login_submit_button").assertIsDisplayed()
    }

    @Test
    fun typingUpdatesEmailAndPasswordFields() {
        setLoginContent()

        composeRule.onNodeWithTag("login_email_field").performTextInput("novita.sari@mail.com")
        composeRule.onNodeWithTag("login_password_field").performTextInput("Password123!")

        composeRule.onNodeWithText("novita.sari@mail.com").assertIsDisplayed()
    }

    @Test
    fun submitButtonDisabledWhileLoading() {
        setLoginContent(initialState = LoginUiState(identifier = "a@b.com", password = "x", isLoading = true))

        composeRule.onNodeWithTag("login_submit_button").assertIsNotEnabled()
    }

    @Test
    fun submitButtonEnabledWhenNotLoading() {
        setLoginContent(initialState = LoginUiState(identifier = "a@b.com", password = "x", isLoading = false))

        composeRule.onNodeWithTag("login_submit_button").assertIsEnabled()
    }

    @Test
    fun clickingSubmitInvokesCallback() {
        var clicked = false
        setLoginContent(onLoginClick = { clicked = true })

        composeRule.onNodeWithTag("login_submit_button").performClick()

        assert(clicked) { "onLoginClick harusnya kepanggil pas tombol Masuk di-klik" }
    }

    @Test
    fun displaysErrorMessageWhenPresent() {
        setLoginContent(
            initialState = LoginUiState(
                identifier = "",
                password = "",
                errorMessage = "Email/No HP dan password wajib diisi"
            )
        )

        composeRule.onNodeWithText("Email/No HP dan password wajib diisi").assertIsDisplayed()
    }
}
