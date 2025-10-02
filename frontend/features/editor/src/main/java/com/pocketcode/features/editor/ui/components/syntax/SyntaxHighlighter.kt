package com.pocketcode.features.editor.ui.components.syntax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.pocketcode.features.editor.domain.model.CodeLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Visual tokens used by the syntax highlighter.
 */
enum class TokenType {
    KEYWORD,
    STRING,
    COMMENT,
    NUMBER,
    OPERATOR,
    FUNCTION,
    TYPE,
    VARIABLE,
    CONSTANT,
    ANNOTATION,
    PUNCTUATION,
    WHITESPACE,
    UNKNOWN
}

/**
 * Theme definition for the highlighter.
 */
data class SyntaxTheme(
    val name: String,
    val background: Color,
    val foreground: Color,
    val keyword: Color,
    val string: Color,
    val comment: Color,
    val number: Color,
    val operator: Color,
    val function: Color,
    val type: Color,
    val variable: Color,
    val constant: Color,
    val annotation: Color,
    val error: Color,
    val warning: Color,
    val currentLine: Color
) {
    companion object {
        val VSCodeDark = SyntaxTheme(
            name = "VS Code Dark",
            background = Color(0xFF1E1E1E),
            foreground = Color(0xFFD4D4D4),
            keyword = Color(0xFF569CD6),
            string = Color(0xFFCE9178),
            comment = Color(0xFF6A9955),
            number = Color(0xFFB5CEA8),
            operator = Color(0xFFD4D4D4),
            function = Color(0xFFDCDCAA),
            type = Color(0xFF4EC9B0),
            variable = Color(0xFF9CDCFE),
            constant = Color(0xFF4FC1FF),
            annotation = Color(0xFFD7BA7D),
            error = Color(0xFFF44747),
            warning = Color(0xFFFFCC02),
            currentLine = Color(0xFF2A2D2E)
        )

        val VSCodeLight = SyntaxTheme(
            name = "VS Code Light",
            background = Color(0xFFFFFFFF),
            foreground = Color(0xFF000000),
            keyword = Color(0xFF0000FF),
            string = Color(0xFF163E00),
            comment = Color(0xFF008000),
            number = Color(0xFF09885A),
            operator = Color(0xFF000000),
            function = Color(0xFF795E26),
            type = Color(0xFF267F99),
            variable = Color(0xFF001080),
            constant = Color(0xFF0070C1),
            annotation = Color(0xFF0070C1),
            error = Color(0xFFE51400),
            warning = Color(0xFFBF8803),
            currentLine = Color(0xFFF3F3F3)
        )

        val Dracula = SyntaxTheme(
            name = "Dracula",
            background = Color(0xFF282A36),
            foreground = Color(0xFFF8F8F2),
            keyword = Color(0xFFFF79C6),
            string = Color(0xFFF1FA8C),
            comment = Color(0xFF6272A4),
            number = Color(0xFFBD93F9),
            operator = Color(0xFFFFB86C),
            function = Color(0xFF50FA7B),
            type = Color(0xFF8BE9FD),
            variable = Color(0xFFF8F8F2),
            constant = Color(0xFFBD93F9),
            annotation = Color(0xFFFFB86C),
            error = Color(0xFFFF5555),
            warning = Color(0xFFF1FA8C),
            currentLine = Color(0xFF44475A)
        )

        val Monokai = SyntaxTheme(
            name = "Monokai",
            background = Color(0xFF272822),
            foreground = Color(0xFFF8F8F2),
            keyword = Color(0xFFF92672),
            string = Color(0xFFE6DB74),
            comment = Color(0xFF75715E),
            number = Color(0xFFAE81FF),
            operator = Color(0xFFF92672),
            function = Color(0xFFA6E22E),
            type = Color(0xFF66D9EF),
            variable = Color(0xFFF8F8F2),
            constant = Color(0xFFAE81FF),
            annotation = Color(0xFFA6E22E),
            error = Color(0xFFF92672),
            warning = Color(0xFFE6DB74),
            currentLine = Color(0xFF3E3D32)
        )
    }
}

/**
 * Token data used while highlighting.
 */
data class SyntaxToken(
    val text: String,
    val start: Int,
    val end: Int,
    val type: TokenType
)

/**
 * Main entry point for syntax highlighting.
 */
class SyntaxHighlighter(
    private var theme: SyntaxTheme = SyntaxTheme.VSCodeDark,
    private val cacheSize: Int = 100
) {

    private val highlightCache = mutableMapOf<String, AnnotatedString>()
    private val cacheKeys = ArrayDeque<String>()

    suspend fun highlight(text: String, language: CodeLanguage): AnnotatedString =
        withContext(Dispatchers.Default) {
            highlightInternal(text, language)
        }

    fun highlightSync(text: String, language: CodeLanguage): AnnotatedString =
        highlightInternal(text, language)

    fun setTheme(newTheme: SyntaxTheme) {
        if (theme != newTheme) {
            theme = newTheme
            clearCache()
        }
    }

    fun clearCache() {
        highlightCache.clear()
        cacheKeys.clear()
    }

    private fun highlightInternal(text: String, language: CodeLanguage): AnnotatedString {
        val cacheKey = "${language.name}_${text.hashCode()}_${theme.name}"
        highlightCache[cacheKey]?.let { cached ->
            cacheKeys.remove(cacheKey)
            cacheKeys.addFirst(cacheKey)
            return cached
        }

        val tokens = tokenize(text, language)
            val highlighted = buildAnnotatedString {
                append(text)
                tokens.forEach { token ->
                    addStyle(getStyleForToken(token.type), token.start, token.end)
                }
            }

            cacheHighlighted(cacheKey, highlighted)
            return highlighted
    }

    private fun cacheHighlighted(key: String, highlighted: AnnotatedString) {
        if (highlightCache.size >= cacheSize) {
            cacheKeys.removeLastOrNull()?.let { highlightCache.remove(it) }
        }
        highlightCache[key] = highlighted
        cacheKeys.addFirst(key)
    }

    private fun getStyleForToken(type: TokenType): SpanStyle = when (type) {
        TokenType.KEYWORD -> SpanStyle(theme.keyword, fontWeight = FontWeight.Bold)
        TokenType.STRING -> SpanStyle(theme.string)
        TokenType.COMMENT -> SpanStyle(theme.comment, fontStyle = FontStyle.Italic)
        TokenType.NUMBER -> SpanStyle(theme.number)
        TokenType.OPERATOR -> SpanStyle(theme.operator)
        TokenType.FUNCTION -> SpanStyle(theme.function, fontWeight = FontWeight.Medium)
        TokenType.TYPE -> SpanStyle(theme.type, fontWeight = FontWeight.Medium)
        TokenType.VARIABLE -> SpanStyle(theme.variable)
        TokenType.CONSTANT -> SpanStyle(theme.constant, fontWeight = FontWeight.Bold)
        TokenType.ANNOTATION -> SpanStyle(theme.annotation, textDecoration = TextDecoration.Underline)
        TokenType.PUNCTUATION -> SpanStyle(theme.operator)
        else -> SpanStyle(theme.foreground)
    }

    private fun tokenize(text: String, language: CodeLanguage): List<SyntaxToken> = when (language) {
        CodeLanguage.KOTLIN -> tokenizeKotlin(text)
        CodeLanguage.JAVA -> tokenizeJava(text)
        CodeLanguage.JAVASCRIPT -> tokenizeJavaScript(text)
        CodeLanguage.TYPESCRIPT -> tokenizeTypeScript(text)
        CodeLanguage.PYTHON -> tokenizePython(text)
        CodeLanguage.DART -> tokenizeDart(text)
        CodeLanguage.XML -> tokenizeXml(text)
        CodeLanguage.HTML -> tokenizeHtml(text)
        CodeLanguage.CSS -> tokenizeCss(text)
        CodeLanguage.JSON -> tokenizeJson(text)
        CodeLanguage.YAML -> tokenizeYaml(text)
        CodeLanguage.MARKDOWN -> tokenizeMarkdown(text)
        CodeLanguage.SQL -> tokenizeSql(text)
        CodeLanguage.GRADLE -> tokenizeGradle(text)
        else -> tokenizePlainText(text)
    }
}

private fun applyRegexPatterns(
    text: String,
    patterns: List<Pair<Regex, TokenType>>,
    tokens: MutableList<SyntaxToken>
) {
    patterns.forEach { (regex, type) ->
        regex.findAll(text).forEach { match ->
            tokens.add(
                SyntaxToken(
                    match.value,
                    match.range.first,
                    match.range.last + 1,
                    type
                )
            )
        }
    }
}

private fun addKeywordTokens(
    text: String,
    keywords: Set<String>,
    tokens: MutableList<SyntaxToken>,
    ignoreCase: Boolean = false
) {
    if (keywords.isEmpty()) return
    val escaped = keywords.map { Regex.escape(it) }
    val pattern = Regex(
        pattern = "\\b(${escaped.joinToString("|")})\\b",
        options = if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet()
    )
    pattern.findAll(text).forEach { match ->
        tokens.add(
            SyntaxToken(
                match.value,
                match.range.first,
                match.range.last + 1,
                TokenType.KEYWORD
            )
        )
    }
}

private fun tokenizeKotlin(text: String): List<SyntaxToken> {
    val tokens = mutableListOf<SyntaxToken>()
    val keywords = setOf(
        "abstract", "actual", "annotation", "as", "break", "by", "catch", "class",
        "companion", "const", "constructor", "continue", "crossinline", "data",
        "delegate", "do", "dynamic", "else", "enum", "expect", "external", "false",
        "final", "finally", "for", "fun", "get", "if", "import", "in", "infix",
        "init", "inline", "inner", "interface", "internal", "is", "lateinit",
        "noinline", "null", "object", "open", "operator", "out", "override",
        "package", "private", "protected", "public", "reified", "return", "sealed",
        "set", "super", "suspend", "tailrec", "this", "throw", "true", "try",
        "typealias", "typeof", "val", "var", "vararg", "when", "where", "while"
    )
    val patterns = listOf(
        Regex("""//.*$""", RegexOption.MULTILINE) to TokenType.COMMENT,
        Regex("""/\*[\s\S]*?\*/""") to TokenType.COMMENT,
        Regex("\"(?:[^\"\\]|\\.)*\"") to TokenType.STRING,
        Regex("""\"\"\"[\s\S]*?\"\"\"""") to TokenType.STRING,
        Regex("""\b\d+\.?\d*[fFlL]?\b""") to TokenType.NUMBER,
        Regex("""@\w+""") to TokenType.ANNOTATION,
        Regex("""\b[a-zA-Z_]\w*\b""") to TokenType.VARIABLE
    )
    applyRegexPatterns(text, patterns, tokens)
    addKeywordTokens(text, keywords, tokens)
    return tokens.sortedBy { it.start }
}

private fun tokenizeJava(text: String): List<SyntaxToken> = tokenizeKotlin(text)

private fun tokenizeJavaScript(text: String): List<SyntaxToken> {
    val tokens = mutableListOf<SyntaxToken>()
    val keywords = setOf(
        "await", "break", "case", "catch", "class", "const", "continue", "debugger",
        "default", "delete", "do", "else", "enum", "export", "extends", "false",
        "finally", "for", "function", "if", "import", "in", "instanceof", "let",
        "new", "null", "return", "super", "switch", "this", "throw", "true", "try",
        "typeof", "var", "void", "while", "with", "yield"
    )
    val patterns = listOf(
        Regex("""//.*$""", RegexOption.MULTILINE) to TokenType.COMMENT,
        Regex("""/\*[\s\S]*?\*/""") to TokenType.COMMENT,
        Regex("""`(?:[^`\\]|\\.)*`""") to TokenType.STRING,
        Regex("\"(?:[^\"\\]|\\.)*\"") to TokenType.STRING,
        Regex("""'(?:[^'\\]|\\.)*'""") to TokenType.STRING,
        Regex("""\b\d+\.?\d*(?:[eE][+-]?\d+)?\b""") to TokenType.NUMBER,
        Regex("""\b[a-zA-Z_$][\w$]*\b""") to TokenType.VARIABLE
    )
    applyRegexPatterns(text, patterns, tokens)
    addKeywordTokens(text, keywords, tokens, ignoreCase = true)
    return tokens.sortedBy { it.start }
}

private fun tokenizeTypeScript(text: String): List<SyntaxToken> = tokenizeJavaScript(text)

private fun tokenizePython(text: String): List<SyntaxToken> {
    val tokens = mutableListOf<SyntaxToken>()
    val keywords = setOf(
        "and", "as", "assert", "async", "await", "break", "class", "continue",
        "def", "del", "elif", "else", "except", "False", "finally", "for", "from",
        "global", "if", "import", "in", "is", "lambda", "None", "nonlocal", "not",
        "or", "pass", "raise", "return", "True", "try", "while", "with", "yield"
    )
    val patterns = listOf(
        Regex("""#.*$""", RegexOption.MULTILINE) to TokenType.COMMENT,
        Regex("""\"\"\"[\s\S]*?\"\"\"""") to TokenType.STRING,
        Regex("""'''[\s\S]*?'''""") to TokenType.STRING,
        Regex("\"(?:[^\"\\]|\\.)*\"") to TokenType.STRING,
        Regex("""'(?:[^'\\]|\\.)*'""") to TokenType.STRING,
        Regex("""\b\d+\.?\d*\b""") to TokenType.NUMBER,
        Regex("""@\w+""") to TokenType.ANNOTATION,
        Regex("""\b[a-zA-Z_]\w*\b""") to TokenType.VARIABLE
    )
    applyRegexPatterns(text, patterns, tokens)
    addKeywordTokens(text, keywords, tokens)
    return tokens.sortedBy { it.start }
}

private fun tokenizeDart(text: String): List<SyntaxToken> = tokenizeJava(text)

private fun tokenizeXml(text: String): List<SyntaxToken> {
    val tokens = mutableListOf<SyntaxToken>()
    val patterns = listOf(
        Regex("""<!--[\s\S]*?-->""") to TokenType.COMMENT,
        Regex("""</?[a-zA-Z_][\w:.-]*""") to TokenType.KEYWORD,
        Regex("""\b[a-zA-Z_][\w:.-]*(?=\s*=)""") to TokenType.VARIABLE,
        Regex("\"(?:[^\"\\]|\\.)*\"") to TokenType.STRING,
        Regex("""'(?:[^'\\]|\\.)*'""") to TokenType.STRING
    )
    applyRegexPatterns(text, patterns, tokens)
    return tokens.sortedBy { it.start }
}

private fun tokenizeHtml(text: String): List<SyntaxToken> = tokenizeXml(text)

private fun tokenizeCss(text: String): List<SyntaxToken> {
    val tokens = mutableListOf<SyntaxToken>()
    val patterns = listOf(
        Regex("""/\*[\s\S]*?\*/""") to TokenType.COMMENT,
        Regex("""[.#]?[a-zA-Z_][\w-]*(?=\s*\{)""") to TokenType.FUNCTION,
        Regex("""\b[a-zA-Z-]+(?=\s*:)""") to TokenType.VARIABLE,
        Regex(""":\s*[^;{}]+""") to TokenType.STRING,
        Regex("""#[0-9a-fA-F]{3,6}\b""") to TokenType.NUMBER,
        Regex("""\d+(?:px|em|rem|%|vh|vw|pt|pc|in|cm|mm)""") to TokenType.NUMBER
    )
    applyRegexPatterns(text, patterns, tokens)
    return tokens.sortedBy { it.start }
}

private fun tokenizeJson(text: String): List<SyntaxToken> {
    val tokens = mutableListOf<SyntaxToken>()
    val patterns = listOf(
        Regex("\"(?:[^\"\\]|\\.)*\"") to TokenType.STRING,
        Regex("""-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?""") to TokenType.NUMBER,
        Regex("""\b(?:true|false|null)\b""") to TokenType.KEYWORD
    )
    applyRegexPatterns(text, patterns, tokens)
    return tokens.sortedBy { it.start }
}

private fun tokenizeYaml(text: String): List<SyntaxToken> {
    val tokens = mutableListOf<SyntaxToken>()
    val patterns = listOf(
        Regex("""#.*$""", RegexOption.MULTILINE) to TokenType.COMMENT,
        Regex("""^\s*[a-zA-Z_][\w\s-]*(?=:)""", RegexOption.MULTILINE) to TokenType.VARIABLE,
        Regex("\"(?:[^\"\\]|\\.)*\"") to TokenType.STRING,
        Regex("""'(?:[^'\\]|\\.)*'""") to TokenType.STRING,
        Regex("""\b\d+\.?\d*\b""") to TokenType.NUMBER,
        Regex("""\b(?:true|false|yes|no|on|off|null)\b""") to TokenType.KEYWORD
    )
    applyRegexPatterns(text, patterns, tokens)
    return tokens.sortedBy { it.start }
}

private fun tokenizeMarkdown(text: String): List<SyntaxToken> {
    val tokens = mutableListOf<SyntaxToken>()
    val patterns = listOf(
        Regex("""^#{1,6}\s+.*$""", RegexOption.MULTILINE) to TokenType.KEYWORD,
        Regex("""\*\*.+?\*\*""") to TokenType.CONSTANT,
        Regex("""\*[^*]+\*""") to TokenType.VARIABLE,
        Regex("""```[\s\S]*?```""") to TokenType.STRING,
        Regex("""`[^`]+`""") to TokenType.STRING,
        Regex("""\[.*?\]\(.*?\)""") to TokenType.FUNCTION
    )
    applyRegexPatterns(text, patterns, tokens)
    return tokens.sortedBy { it.start }
}

private fun tokenizeSql(text: String): List<SyntaxToken> {
    val tokens = mutableListOf<SyntaxToken>()
    val keywords = setOf(
        "SELECT", "FROM", "WHERE", "INSERT", "UPDATE", "DELETE", "CREATE", "DROP",
        "ALTER", "TABLE", "INDEX", "VIEW", "DATABASE", "SCHEMA", "GRANT", "REVOKE",
        "COMMIT", "ROLLBACK", "TRANSACTION", "BEGIN", "END", "IF", "ELSE", "CASE",
        "WHEN", "THEN", "AND", "OR", "NOT", "NULL", "TRUE", "FALSE", "DISTINCT",
        "ORDER", "BY", "GROUP", "HAVING", "JOIN", "INNER", "LEFT", "RIGHT", "FULL",
        "OUTER", "ON", "AS", "INTO", "VALUES", "SET", "PRIMARY", "KEY", "FOREIGN",
        "REFERENCES", "CONSTRAINT", "CHECK", "DEFAULT" 
    )
    val patterns = listOf(
        Regex("""--.*$""", RegexOption.MULTILINE) to TokenType.COMMENT,
        Regex("""/\*[\s\S]*?\*/""") to TokenType.COMMENT,
        Regex("""'(?:[^'\\]|\\.)*'""") to TokenType.STRING,
        Regex("""\b\d+\.?\d*\b""") to TokenType.NUMBER,
        Regex("""\b[a-zA-Z_]\w*\b""") to TokenType.VARIABLE
    )
    applyRegexPatterns(text, patterns, tokens)
    addKeywordTokens(text, keywords, tokens, ignoreCase = true)
    return tokens.sortedBy { it.start }
}

private fun tokenizeGradle(text: String): List<SyntaxToken> = tokenizeKotlin(text)

private fun tokenizePlainText(text: String): List<SyntaxToken> = emptyList()

private fun Char.isUppercaseLetter(): Boolean = java.lang.Character.isUpperCase(this)

private fun String.isAllUppercase(): Boolean {
    if (isEmpty()) return false
    var hasLetter = false
    for (ch in this) {
        if (ch.isLetter()) {
            hasLetter = true
            if (!ch.isUpperCase()) return false
        }
    }
    return hasLetter
}