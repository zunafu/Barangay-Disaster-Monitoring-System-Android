package com.example.disastermanagement.utils

/**
 * Password validation utility for checking password strength
 */
object PasswordValidator {

    /**
     * Validates password strength requirements
     * Requirements: At least 6 characters, 1 uppercase, 1 lowercase, 1 number
     */
    fun validatePassword(password: String): PasswordValidationResult {
        val hasMinLength = password.length >= 6
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasNumber = password.any { it.isDigit() }

        val isValid = hasMinLength && hasUpperCase && hasLowerCase && hasNumber
        val strength = calculateStrength(hasMinLength, hasUpperCase, hasLowerCase, hasNumber, password.length)

        return PasswordValidationResult(
            isValid = isValid,
            hasMinLength = hasMinLength,
            hasUpperCase = hasUpperCase,
            hasLowerCase = hasLowerCase,
            hasNumber = hasNumber,
            strength = strength
        )
    }

    /**
     * Calculate password strength (0-100%)
     */
    private fun calculateStrength(
        hasMinLength: Boolean,
        hasUpperCase: Boolean,
        hasLowerCase: Boolean,
        hasNumber: Boolean,
        length: Int
    ): PasswordStrength {
        var score = 0

        // Base score from requirements
        if (hasMinLength) score += 15
        if (hasUpperCase) score += 20
        if (hasLowerCase) score += 20
        if (hasNumber) score += 20

        // Bonus for length
        if (length >= 12) score += 15
        else if (length >= 10) score += 10
        else if (length >= 8) score += 5

        return when {
            score < 35 -> PasswordStrength.WEAK
            score < 65 -> PasswordStrength.FAIR
            score < 85 -> PasswordStrength.GOOD
            else -> PasswordStrength.STRONG
        }
    }
}

data class PasswordValidationResult(
    val isValid: Boolean,
    val hasMinLength: Boolean,
    val hasUpperCase: Boolean,
    val hasLowerCase: Boolean,
    val hasNumber: Boolean,
    val strength: PasswordStrength
)

enum class PasswordStrength(val displayName: String, val percentage: Int) {
    WEAK("Weak", 25),
    FAIR("Fair", 50),
    GOOD("Good", 75),
    STRONG("Strong", 100)
}

