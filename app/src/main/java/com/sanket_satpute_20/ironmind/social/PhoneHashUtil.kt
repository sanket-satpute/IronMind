package com.sanket_satpute_20.ironmind.social

import java.security.MessageDigest

object PhoneHashUtil {

    fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
