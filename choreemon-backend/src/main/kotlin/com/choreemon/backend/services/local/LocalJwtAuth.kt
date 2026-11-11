package com.choreemon.backend.services.local

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import com.choreemon.backend.config.JwtConfig
import com.choreemon.backend.services.IAuthService
import java.util.Date

class LocalJwtAuth(private val config: JwtConfig) : IAuthService {
    private val algorithm = Algorithm.HMAC256(config.secret)

    override fun verifier(): JWTVerifier =
        JWT.require(algorithm)
            .withIssuer(config.issuer)
            .build()

    override fun generateToken(userId: String, householdId: String, isParent: Boolean): String {
        val now = System.currentTimeMillis()
        val expiresAt = Date(now + config.expiryHours * 3600 * 1000)
        return JWT.create()
            .withIssuer(config.issuer)
            .withClaim("userId", userId)
            .withClaim("householdId", householdId)
            .withClaim("isParent", isParent)
            .withIssuedAt(Date(now))
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }

    override fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    override fun verifyPassword(password: String, hash: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hash).verified
    }
}
