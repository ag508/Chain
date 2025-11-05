package com.chain.app.domain.model.auth

/**
 * Represents the current authentication state of the app.
 */
sealed class AuthState {
    /**
     * User is authenticated and can access the app.
     */
    data class Authenticated(
        val userId: String,
        val phoneNumber: String,
        val displayName: String,
        val publicKey: String
    ) : AuthState()

    /**
     * User is not authenticated and needs to log in.
     */
    object Unauthenticated : AuthState()

    /**
     * Checking authentication status (app startup).
     */
    object Checking : AuthState()

    /**
     * Session expired, user needs to re-authenticate.
     */
    object SessionExpired : AuthState()
}

/**
 * Simplified boolean check for authentication.
 */
val AuthState.isAuthenticated: Boolean
    get() = this is AuthState.Authenticated

/**
 * Get user ID if authenticated, null otherwise.
 */
val AuthState.userId: String?
    get() = (this as? AuthState.Authenticated)?.userId

/**
 * Get display name if authenticated, null otherwise.
 */
val AuthState.displayName: String?
    get() = (this as? AuthState.Authenticated)?.displayName
