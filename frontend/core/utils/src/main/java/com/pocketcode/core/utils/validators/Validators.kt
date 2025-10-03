package com.pocketcode.core.utils.validators

/**
 * Validation utilities for common input validation
 */
object Validators {
    
    fun isValidProjectName(name: String): Boolean {
        return name.isNotBlank() && 
               name.length >= 3 && 
               name.length <= 50 &&
               name.matches(Regex("^[a-zA-Z][a-zA-Z0-9_-]*$"))
    }
    
    fun isValidFileName(name: String): Boolean {
        return name.isNotBlank() &&
               name.length <= 255 &&
               !name.contains(Regex("[<>:\"/\\|?*]")) &&
               !name.equals(".", ignoreCase = true) &&
               !name.equals("..", ignoreCase = true)
    }
    
    fun isValidPackageName(packageName: String): Boolean {
        if (packageName.isBlank()) return false
        
        val parts = packageName.split(".")
        if (parts.size < 2) return false
        
        return parts.all { part ->
            part.isNotEmpty() &&
            part.first().isLetter() &&
            part.all { char -> char.isLetterOrDigit() || char == '_' }
        }
    }
    
    fun isValidAndroidVersionCode(versionCode: String): Boolean {
        return versionCode.toIntOrNull()?.let { it > 0 } ?: false
    }
    
    fun isValidAndroidVersionName(versionName: String): Boolean {
        return versionName.isNotBlank() &&
               versionName.matches(Regex("^\\d+(\\.\\d+)*(-[a-zA-Z0-9]+)*$"))
    }
}