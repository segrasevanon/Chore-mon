package com.choreemon.backend.services.local

import com.choreemon.backend.services.INotificationService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import org.slf4j.LoggerFactory

class FcmDirectService(private val serverKey: String?) : INotificationService {
    private val logger = LoggerFactory.getLogger(FcmDirectService::class.java)
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson()
        }
    }

    override suspend fun sendNotification(
        tokens: List<String>,
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        if (tokens.isEmpty()) return

        if (serverKey.isNullOrBlank()) {
            logger.info("[DEV FCM] Notification suppressed (no FCM_SERVER_KEY configured). Recipients: ${tokens.size}, Title: '$title', Body: '$body'")
            return
        }

        for (token in tokens) {
            try {
                val payload = mapOf(
                    "to" to token,
                    "notification" to mapOf(
                        "title" to title,
                        "body" to body
                    ),
                    "data" to data
                )
                httpClient.post("https://fcm.googleapis.com/fcm/send") {
                    header(HttpHeaders.Authorization, "key=$serverKey")
                    contentType(ContentType.Application.Json)
                    setBody(payload)
                }
            } catch (e: Exception) {
                logger.error("Failed to send FCM notification to $token", e)
            }
        }
    }
}
