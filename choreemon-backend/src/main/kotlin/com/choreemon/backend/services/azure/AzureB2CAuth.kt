package com.choreemon.backend.services.azure

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import com.choreemon.backend.config.AzureConfig
import com.choreemon.backend.services.IAuthService

class AzureB2CAuth(private val config: AzureConfig) : IAuthService {
    // When migrating to Azure AD B2C, JWT verification uses JWKS from Azure B2C
    // e.g., https://<tenant>.b2clogin.com/<tenant>.onmicrosoft.com/<policy>/discovery/v2.0/keys
    override fun verifier(): JWTVerifier {
        // Fallback or JWKS verifier
        return JWT.require(Algorithm.none()).build()
    }

    override fun generateToken(userId: String, householdId: String, isParent: Boolean): String {
        // In Azure B2C, tokens are issued by B2C identity flows.
        return ""
    }

    override fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    override fun verifyPassword(password: String, hash: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hash).verified
    }
}
