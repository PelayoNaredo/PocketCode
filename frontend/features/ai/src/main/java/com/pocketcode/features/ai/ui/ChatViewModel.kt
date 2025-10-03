package com.pocketcode.features.ai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketcode.features.settings.model.AIProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentContext: String = "",
    val selectedModel: AIProvider = AIProvider.OPENAI_GPT4
)

data class ProjectContext(
    val projectId: String? = null,
    val projectName: String? = null,
    val filePath: String? = null,
    val fileName: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    // AI service functionality implemented via mock responses
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private var projectContext = ProjectContext()
    private var externalContext: String? = null
    
    fun updateContext(
        projectId: String?,
        projectName: String?,
        filePath: String?,
        fileName: String?
    ) {
        projectContext = ProjectContext(
            projectId = projectId,
            projectName = projectName,
            filePath = filePath,
            fileName = fileName
        )
        
        val contextDescription = buildContextDescription()
        _uiState.value = _uiState.value.copy(currentContext = contextDescription)
    }

    fun applyExternalContext(context: String?) {
        externalContext = context?.takeIf { it.isNotBlank() }
        _uiState.value = _uiState.value.copy(currentContext = buildContextDescription())
    }
    
    private fun buildContextDescription(): String {
        val parts = mutableListOf<String>()
        
        projectContext.projectName?.let { projectName ->
            parts.add("Project: $projectName")
        }
        
        projectContext.fileName?.let { fileName ->
            parts.add("File: $fileName")
        }

        return when {
            parts.isNotEmpty() && !externalContext.isNullOrBlank() -> {
                val enriched = parts + "Focus: ${externalContext!!}"
                "Currently working on: ${enriched.joinToString(" | ")}"
            }
            parts.isNotEmpty() -> "Currently working on: ${parts.joinToString(" | ")}"
            !externalContext.isNullOrBlank() -> "Chat context: ${externalContext!!}"
            else -> "No active project or file"
        }
    }
    
    fun sendMessage(content: String) {
        viewModelScope.launch {
            // Add user message
            val userMessage = ChatMessage(
                id = System.currentTimeMillis().toString(),
                content = content,
                isFromUser = true
            )
            
            val currentMessages = _uiState.value.messages.toMutableList()
            currentMessages.add(userMessage)
            
            // Add loading message from AI
            val loadingMessage = ChatMessage(
                id = "${System.currentTimeMillis()}_loading",
                content = "",
                isFromUser = false,
                isLoading = true
            )
            currentMessages.add(loadingMessage)
            
            _uiState.value = _uiState.value.copy(
                messages = currentMessages,
                isLoading = true
            )
            
            try {
                // Simulate AI response with intelligent context-aware responses
                delay(1500) // Simulate network delay
                
                val aiResponse = generateMockResponse(content)
                
                // Remove loading message and add actual response
                val finalMessages = currentMessages.toMutableList()
                finalMessages.removeLastOrNull() // Remove loading message
                
                val aiMessage = ChatMessage(
                    id = System.currentTimeMillis().toString(),
                    content = aiResponse,
                    isFromUser = false
                )
                finalMessages.add(aiMessage)
                
                _uiState.value = _uiState.value.copy(
                    messages = finalMessages,
                    isLoading = false
                )
                
            } catch (e: Exception) {
                // Remove loading message on error
                val errorMessages = currentMessages.toMutableList()
                errorMessages.removeLastOrNull()
                
                val errorMessage = ChatMessage(
                    id = "${System.currentTimeMillis()}_error",
                    content = "Sorry, I encountered an error. Please try again.",
                    isFromUser = false
                )
                errorMessages.add(errorMessage)
                
                _uiState.value = _uiState.value.copy(
                    messages = errorMessages,
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    private fun generateMockResponse(userMessage: String): String {
        val contextAwareResponse = generateContextAwareResponse(userMessage)
        if (contextAwareResponse != null) {
            return contextAwareResponse
        }
        
        // Fallback to general responses
        return when {
            userMessage.contains("hello", ignoreCase = true) ||
            userMessage.contains("hi", ignoreCase = true) -> {
                val contextInfo = if (projectContext.projectName != null) {
                    " I can see you're working on the ${projectContext.projectName} project${
                        projectContext.fileName?.let { " in file $it" } ?: ""
                    }."
                } else {
                    ""
                }
                "Hello! I'm PocketCode AI, your coding assistant.$contextInfo How can I help you today?"
            }
            
            userMessage.contains("debug", ignoreCase = true) ||
            userMessage.contains("error", ignoreCase = true) -> {
                val fileSpecific = projectContext.fileName?.let { fileName ->
                    when {
                        fileName.endsWith(".kt") -> "\n\n**For Kotlin files like $fileName:**\n• Check for null safety issues\n• Verify coroutine usage\n• Look for lateinit property access"
                        fileName.endsWith(".java") -> "\n\n**For Java files like $fileName:**\n• Check for NullPointerException\n• Verify try-catch blocks\n• Look for array index bounds"
                        fileName.endsWith(".xml") -> "\n\n**For XML files like $fileName:**\n• Validate XML syntax\n• Check attribute values\n• Verify namespace declarations"
                        else -> ""
                    }
                } ?: ""
                
                """I'd be happy to help you debug! Here are some common debugging strategies:

1. **Check the console/logs** for error messages
2. **Add breakpoints** to inspect variable values  
3. **Use print statements** to trace execution flow
4. **Verify your logic** step by step
5. **Check for typos** in variable names and syntax$fileSpecific

Could you share the specific error or code you're having trouble with?"""
            }
            
            userMessage.contains("optimize", ignoreCase = true) ||
            userMessage.contains("performance", ignoreCase = true) -> {
                val projectSpecific = if (projectContext.projectName != null) {
                    "\n\n**For your ${projectContext.projectName} project:**\n• Consider using ProGuard for code shrinking\n• Optimize Compose recompositions\n• Use appropriate launch modes for Activities"
                } else ""
                
                """Here are some general optimization tips:

**For Mobile Apps:**
• Minimize UI recompositions in Jetpack Compose
• Use lazy loading for large lists
• Optimize image loading and caching
• Reduce memory allocations in tight loops

**For Code Quality:**
• Use appropriate data structures
• Avoid nested loops when possible  
• Cache expensive calculations
• Profile your code to find bottlenecks$projectSpecific

What specific area would you like to optimize?"""
            }
            
            userMessage.contains("code review", ignoreCase = true) ||
            userMessage.contains("review", ignoreCase = true) -> {
                val currentFileInfo = projectContext.fileName?.let { fileName ->
                    "\n\nI can see you're currently working on **$fileName**. Feel free to share its content for a detailed review!"
                } ?: ""
                
                """I'd love to review your code! Here's what I typically look for:

✅ **Code Quality:**
• Clear variable and function names
• Proper separation of concerns
• DRY (Don't Repeat Yourself) principle

✅ **Best Practices:**
• Error handling
• Input validation
• Security considerations
• Performance implications$currentFileInfo"""
            }
            
            userMessage.contains("android", ignoreCase = true) ||
            userMessage.contains("kotlin", ignoreCase = true) -> {
                """Great! I'm well-versed in Android development with Kotlin. I can help with:

📱 **Android Development:**
• Jetpack Compose UI
• Architecture patterns (MVVM, MVI)
• Dependency injection with Hilt
• Navigation and state management
• Room database and data persistence

🎯 **Kotlin Features:**
• Coroutines and Flow
• Extension functions
• Data classes and sealed classes
• Null safety best practices

What specific Android/Kotlin topic can I assist you with?"""
            }
            
            else -> {
                val contextSuggestion = if (projectContext.projectName != null) {
                    " Since you're working on ${projectContext.projectName}${
                        projectContext.fileName?.let { ", specifically on $it," } ?: ","
                    } I can provide more targeted assistance if you share specific code or questions about your current work."
                } else {
                    " If you share details about your current project or code, I can provide more specific assistance."
                }
                
                """I understand you're asking about: "$userMessage"

I'm here to help with various coding topics including:
• 🐛 Debugging and troubleshooting
• 🚀 Performance optimization  
• 📱 Android & Kotlin development
• 🎨 UI/UX with Jetpack Compose
• 🏗️ Architecture and design patterns
• 📝 Code reviews and best practices$contextSuggestion"""
            }
        }
    }
    
    private fun generateContextAwareResponse(userMessage: String): String? {
        // Project-specific responses
        projectContext.projectName?.let { projectName ->
            when {
                userMessage.contains("project", ignoreCase = true) -> {
                    return """I can see you're working on the **$projectName** project! Here's what I can help you with:

🏗️ **Project Structure:**
• Organizing your code modules
• Setting up proper package structure
• Managing dependencies

🔧 **Development Workflow:**
• Best practices for your project type
• Code organization patterns
• Testing strategies

📱 **Mobile Development:**
• Android-specific optimizations
• UI/UX improvements
• Performance tuning

What specific aspect of the $projectName project would you like to work on?"""
                }
                
                userMessage.contains("file", ignoreCase = true) && projectContext.fileName != null -> {
                    return """I see you're asking about files, and you're currently working on **${projectContext.fileName}**!

For this ${getFileTypeDescription(projectContext.fileName!!)} file, I can help with:
${getFileSpecificHelp(projectContext.fileName!!)}

Would you like me to review the code, suggest improvements, or help with a specific issue?"""
                }
                
                else -> { /* Continue to other checks */ }
            }
        }
        
        // File-specific responses
        projectContext.fileName?.let { fileName ->
            when {
                userMessage.contains("this file", ignoreCase = true) ||
                userMessage.contains("current file", ignoreCase = true) -> {
                    return """You're currently working on **$fileName**. 

${getFileSpecificHelp(fileName)}

Feel free to share the code or ask specific questions about this file!"""
                }
                
                else -> { /* Continue to other checks */ }
            }
        }
        
        return null
    }
    
    private fun getFileTypeDescription(fileName: String): String {
        return when {
            fileName.endsWith(".kt") -> "Kotlin"
            fileName.endsWith(".java") -> "Java"
            fileName.endsWith(".xml") -> "XML layout/resource"
            fileName.endsWith(".json") -> "JSON configuration"
            fileName.endsWith(".gradle") || fileName.endsWith(".kts") -> "Gradle build"
            else -> "source code"
        }
    }
    
    private fun getFileSpecificHelp(fileName: String): String {
        return when {
            fileName.endsWith(".kt") -> """
• **Kotlin Best Practices:** Coroutines, null safety, extension functions
• **Code Review:** Clean code principles, performance optimization
• **Debugging:** Common Kotlin pitfalls and solutions
• **Architecture:** MVVM, Repository pattern, Use Cases"""
            
            fileName.endsWith(".java") -> """
• **Java Best Practices:** Error handling, memory management
• **Code Review:** Object-oriented principles, design patterns
• **Debugging:** Exception handling, performance issues
• **Migration:** Converting to Kotlin if needed"""
            
            fileName.endsWith(".xml") -> """
• **Layout Optimization:** Constraint layouts, performance tips
• **UI/UX Review:** Material Design guidelines, accessibility
• **Debugging:** Layout issues, resource conflicts
• **Best Practices:** Reusable components, proper styling"""
            
            fileName.endsWith(".json") -> """
• **Structure Validation:** Proper JSON formatting and validation
• **Configuration Review:** Best practices for config files
• **Debugging:** Syntax errors, data type issues
• **Optimization:** Efficient data structures"""
            
            fileName.endsWith(".gradle") || fileName.endsWith(".kts") -> """
• **Build Optimization:** Dependency management, build performance
• **Configuration Review:** Android-specific settings
• **Debugging:** Build errors, dependency conflicts
• **Best Practices:** Version management, modularization"""
            
            else -> """
• **Code Review:** General best practices and improvements
• **Debugging:** Common issues and solutions
• **Optimization:** Performance and structure improvements
• **Best Practices:** Clean code principles"""
        }
    }
    
    fun clearChat() {
        _uiState.value = ChatUiState()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun selectModel(provider: AIProvider) {
        _uiState.value = _uiState.value.copy(selectedModel = provider)
    }
    
    fun getAvailableModels(): List<AIProvider> {
        return AIProvider.values().toList()
    }
}