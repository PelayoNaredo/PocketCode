package com.pocketcode.core.utils.extensions

import java.text.SimpleDateFormat
import java.util.*

/**
 * String extensions for common operations
 */
fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}

fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.truncate(maxLength: Int, suffix: String = "..."): String {
    return if (length <= maxLength) this else "${take(maxLength)}$suffix"
}

/**
 * Date extensions
 */
fun Date.toFormattedString(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(this)
}

fun Long.toDate(): Date = Date(this)

/**
 * Collection extensions
 */
fun <T> List<T>.second(): T {
    if (size < 2) throw NoSuchElementException("List has less than 2 elements")
    return this[1]
}

fun <T> List<T>.secondOrNull(): T? {
    return if (size >= 2) this[1] else null
}