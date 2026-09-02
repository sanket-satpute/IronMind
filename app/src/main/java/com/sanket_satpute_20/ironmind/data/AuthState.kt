package com.sanket_satpute_20.ironmind.data

data class AuthState(
    val isLoggedIn: Boolean = false,
    val isAnonymous: Boolean = true,
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val uid: String? = null
)
