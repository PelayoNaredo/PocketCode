package com.pocketcode.features.editor.domain.model

/**
 * Supported programming languages for syntax highlighting
 */
enum class CodeLanguage(
    val displayName: String,
    val extensions: List<String>,
    val mimeType: String
) {
    KOTLIN(
        displayName = "Kotlin",
        extensions = listOf("kt", "kts"),
        mimeType = "text/x-kotlin"
    ),
    JAVA(
        displayName = "Java", 
        extensions = listOf("java"),
        mimeType = "text/x-java"
    ),
    JAVASCRIPT(
        displayName = "JavaScript",
        extensions = listOf("js", "mjs"),
        mimeType = "text/javascript"
    ),
    TYPESCRIPT(
        displayName = "TypeScript",
        extensions = listOf("ts", "tsx"),
        mimeType = "text/typescript"
    ),
    PYTHON(
        displayName = "Python",
        extensions = listOf("py", "pyw", "pyc"),
        mimeType = "text/x-python"
    ),
    DART(
        displayName = "Dart",
        extensions = listOf("dart"),
        mimeType = "application/dart"
    ),
    XML(
        displayName = "XML",
        extensions = listOf("xml"),
        mimeType = "text/xml"
    ),
    HTML(
        displayName = "HTML",
        extensions = listOf("html", "htm"),
        mimeType = "text/html"
    ),
    CSS(
        displayName = "CSS",
        extensions = listOf("css"),
        mimeType = "text/css"
    ),
    JSON(
        displayName = "JSON",
        extensions = listOf("json"),
        mimeType = "application/json"
    ),
    YAML(
        displayName = "YAML",
        extensions = listOf("yml", "yaml"),
        mimeType = "text/yaml"
    ),
    MARKDOWN(
        displayName = "Markdown",
        extensions = listOf("md", "markdown"),
        mimeType = "text/markdown"
    ),
    SQL(
        displayName = "SQL",
        extensions = listOf("sql"),
        mimeType = "text/x-sql"
    ),
    GRADLE(
        displayName = "Gradle",
        extensions = listOf("gradle"),
        mimeType = "text/x-gradle"
    ),
    PLAIN_TEXT(
        displayName = "Plain Text",
        extensions = listOf("txt"),
        mimeType = "text/plain"
    );

    companion object {
        /**
         * Get language from file extension
         */
        fun fromExtension(extension: String): CodeLanguage {
            return values().find { language ->
                language.extensions.contains(extension.lowercase())
            } ?: PLAIN_TEXT
        }
        
        /**
         * Get language from filename
         */
        fun fromFileName(fileName: String): CodeLanguage {
            val extension = fileName.substringAfterLast('.', "")
            return fromExtension(extension)
        }
        
        /**
         * Get all supported extensions
         */
        fun getAllExtensions(): List<String> {
            return values().flatMap { it.extensions }.distinct()
        }
        
        /**
         * Check if extension is supported
         */
        fun isSupported(extension: String): Boolean {
            return getAllExtensions().contains(extension.lowercase())
        }
    }
}