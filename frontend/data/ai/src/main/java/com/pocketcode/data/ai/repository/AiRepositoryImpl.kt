package com.pocketcode.data.ai.repository

import com.pocketcode.core.api.model.DomainResult
import com.pocketcode.domain.ai.model.AiMessage
import com.pocketcode.domain.ai.model.CodeGenerationRequest
import com.pocketcode.domain.ai.model.CodeGenerationResponse
import com.pocketcode.domain.ai.model.MessageRole
import com.pocketcode.domain.ai.repository.AiRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock implementation of AiRepository for MVP
 * This will be replaced with actual API calls in Phase 2
 */
@Singleton
class AiRepositoryImpl @Inject constructor() : AiRepository {
    
    private val _chatHistory = MutableStateFlow<List<AiMessage>>(emptyList())
    
    override suspend fun sendMessage(message: String): DomainResult<AiMessage> {
        return try {
            // Simulate API delay
            delay(1000)
            
            // Generate a mock response
            val response = generateMockResponse(message)
            val aiMessage = AiMessage(
                id = UUID.randomUUID().toString(),
                content = response,
                role = MessageRole.ASSISTANT
            )
            
            DomainResult.Success(aiMessage)
        } catch (e: Exception) {
            DomainResult.Error(e)
        }
    }
    
    override suspend fun generateCode(request: CodeGenerationRequest): DomainResult<CodeGenerationResponse> {
        return try {
            // Simulate API delay
            delay(1500)
            
            val generatedCode = when {
                request.prompt.contains("activity", ignoreCase = true) -> generateMockActivity()
                request.prompt.contains("function", ignoreCase = true) -> generateMockFunction()
                request.prompt.contains("button", ignoreCase = true) -> generateMockButton()
                else -> "// Generated code for: ${request.prompt}\nfun generatedFunction() {\n    // Implementation based on prompt requirements\n}"
            }
            
            val response = CodeGenerationResponse(
                generatedCode = generatedCode,
                explanation = "This is a mock implementation. The code was generated based on your prompt: '${request.prompt}'",
                suggestions = listOf(
                    "Consider adding error handling",
                    "Add proper documentation",
                    "Include unit tests"
                )
            )
            
            DomainResult.Success(response)
        } catch (e: Exception) {
            DomainResult.Error(e)
        }
    }
    
    override fun getChatHistory(): Flow<List<AiMessage>> {
        return _chatHistory.asStateFlow()
    }
    
    override suspend fun clearChatHistory(): DomainResult<Unit> {
        return try {
            _chatHistory.value = emptyList()
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            DomainResult.Error(e)
        }
    }
    
    override suspend fun saveMessage(message: AiMessage): DomainResult<Unit> {
        return try {
            val currentHistory = _chatHistory.value.toMutableList()
            currentHistory.add(message)
            _chatHistory.value = currentHistory
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            DomainResult.Error(e)
        }
    }
    
    private fun generateMockResponse(message: String): String {
        return when {
            message.contains("hello", ignoreCase = true) -> 
                "Hello! I'm your AI assistant. I can help you with Android development, code generation, and answering questions about your project."
            
            message.contains("help", ignoreCase = true) -> 
                "I can help you with:\n• Generating Kotlin/Java code\n• Creating Android components\n• Debugging issues\n• Best practices\n• Architecture questions"
            
            message.contains("error", ignoreCase = true) -> 
                "I'd be happy to help debug the error. Can you share the error message or describe what's not working?"
            
            message.contains("compose", ignoreCase = true) -> 
                "Jetpack Compose is great! I can help you create composables, layouts, state management, and more. What specific Compose feature are you working with?"
            
            else -> 
                "I understand you're asking about: '$message'. While this is a mock response, in the full version I'll provide detailed, contextual help for your Android development needs."
        }
    }
    
    private fun generateMockActivity(): String {
        return """
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PocketCodeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to PocketCode!",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
        """.trimIndent()
    }
    
    private fun generateMockFunction(): String {
        return """
fun processUserInput(input: String): String {
    return when {
        input.isBlank() -> "Please enter some text"
        input.length < 3 -> "Input too short"
        else -> "Processed: ${'$'}input"
    }
}

suspend fun fetchDataFromApi(): Result<String> {
    return try {
        delay(1000) // Simulate network call
        Result.success("Data retrieved successfully")
    } catch (e: Exception) {
        Result.failure(e)
    }
}
        """.trimIndent()
    }
    
    private fun generateMockButton(): String {
        return """
@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
        """.trimIndent()
    }
}