package com.pocketcode.core.utils

/**
 * This is an example of a utility file in the `:core:utils` module.
 * It provides common, reusable logic that is not tied to any specific feature or layer.
 *
 * Responsibilities:
 * - Define pure functions that perform a single, clear task.
 * - For example, an `EmailValidator` could check if a given string is a valid email format.
 *
 * This helps to avoid code duplication and keeps business logic in other layers clean.
 *
 * Interacts with:
 * - Any module that needs it. For instance, a `settings` or `login` feature might
 *   use this `Validator` to check user input.
 */
object Validator {
    fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.length > 5
    }
}
