package com.pocketcode.features.ai.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pocketcode.core.ui.theme.PocketTheme
import com.pocketcode.domain.ai.model.CodeGenerationResponse
import com.pocketcode.features.ai.viewmodel.AiAssistantUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AIAssistantPanelTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun generatedCodeCard_emitsCopyAndSaveCallbacks() {
        var copyTriggered = false
        var saveTriggered = false
        val response = CodeGenerationResponse(
            generatedCode = "fun hello() = println(\"hola\")",
            explanation = "Demo snippet",
            suggestions = listOf("Invoca hello() desde main")
        )

        composeRule.setContent {
            PocketTheme {
                AIAssistantPanel(
                    uiState = AiAssistantUiState(lastGeneratedCode = response),
                    chatHistory = emptyList(),
                    onSendPrompt = {},
                    onGenerateCode = {},
                    onClearError = {},
                    onCopyGeneratedCode = { copyTriggered = true },
                    onSaveGeneratedCode = { saveTriggered = true }
                )
            }
        }

        composeRule.onNodeWithText("Copiar").performClick()
        composeRule.onNodeWithText("Guardar").performClick()

        assertTrue(copyTriggered)
        assertTrue(saveTriggered)
    }
}
