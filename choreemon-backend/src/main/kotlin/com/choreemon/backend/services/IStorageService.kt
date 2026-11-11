package com.choreemon.backend.services

import java.io.File
import java.io.InputStream

interface IStorageService {
    suspend fun upload(fileName: String, inputStream: InputStream, contentType: String): String
    suspend fun getFile(fileName: String): File?
    fun getPublicUrl(fileName: String): String
}
