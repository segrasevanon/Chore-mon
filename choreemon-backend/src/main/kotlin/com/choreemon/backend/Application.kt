package com.choreemon.backend

import com.choreemon.backend.config.AppConfig
import com.choreemon.backend.config.ServiceRegistry
import com.choreemon.backend.db.DatabaseFactory
import com.choreemon.backend.routes.*
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.event.Level

fun Application.module() {
    val config = AppConfig.load()
    val registry = ServiceRegistry.create(config)
    DatabaseFactory.init(config)

    install(CallLogging) { level = Level.INFO }

    install(ContentNegotiation) {
        jackson {
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            registerModule(JavaTimeModule())
        }
    }

    install(CORS) {
        anyHost()  // Tighten to your portal domain in production
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
    }

    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to (cause.message ?: "Bad request")))
        }
        exception<SecurityException> { call, cause ->
            call.respond(HttpStatusCode.Forbidden, mapOf("error" to (cause.message ?: "Forbidden")))
        }
        exception<NoSuchElementException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, mapOf("error" to (cause.message ?: "Not found")))
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled error", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Internal server error")
            )
        }
    }

    install(Authentication) {
        jwt("jwt") {
            realm = "choreemon"
            verifier(registry.auth.verifier())
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                val householdId = credential.payload.getClaim("householdId").asString()
                if (userId != null && householdId != null) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Token is missing, invalid, or expired")
                )
            }
        }
    }

    routing {
        route("/api") {
            // Public
            authRoutes(registry)

            // Protected — JWT required
            authenticate("jwt") {
                householdsRoutes(registry)
                membersRoutes(registry)
                choresRoutes(registry)
                completionsRoutes(registry)
                creaturesRoutes(registry)
                leaderboardRoutes(registry)
                societiesRoutes(registry)
                photosRoutes(registry)
            }
        }

        // Health check (used by Docker and Azure App Service)
        get("/health") { call.respond(mapOf("status" to "ok")) }
    }
}
