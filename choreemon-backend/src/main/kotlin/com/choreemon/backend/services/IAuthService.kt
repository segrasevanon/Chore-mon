package com.choreemon.backend.services

import com.auth0.jwt.interfaces.JWTVerifier

interface IAuthService {
    fun verifier(): JWTVerifier
    fun generateToken(userId: String, householdId: String, isParent: Boolean): String
    fun hashPassword(password: String): String
    fun verifyPassword(password: String, hash: String): Boolean
}
