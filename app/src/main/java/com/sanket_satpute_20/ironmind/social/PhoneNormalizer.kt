package com.sanket_satpute_20.ironmind.social

object PhoneNormalizer {

    fun normalize(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        if (digits.isBlank()) return ""
        return when {
            digits.length == 10 -> "+91$digits"
            digits.startsWith("91") && digits.length == 12 -> "+$digits"
            raw.trim().startsWith("+") -> "+$digits"
            else -> "+$digits"
        }
    }
}
