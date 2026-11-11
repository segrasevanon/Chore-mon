package com.choreemon.backend.services

interface INotificationService {
    suspend fun sendNotification(tokens: List<String>, title: String, body: String, data: Map<String, String> = emptyMap())
}
