package com.choreemon.backend.services.local

import com.choreemon.backend.services.IStorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class LocalFileStorage(private val baseDir: File) : IStorageService {
    init {
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
    }

    override suspend fun upload(fileName: String, inputStream: InputStream, contentType: String): String =
        withContext(Dispatchers.IO) {
            val target = File(baseDir, fileName)
            Files.copy(inputStream, target.toPath(), StandardCopyOption.REPLACE_EXISTING)
            getPublicUrl(fileName)
        }

    override suspend fun getFile(fileName: String): File? = withContext(Dispatchers.IO) {
        val file = File(baseDir, fileName)
        if (file.exists() && file.isFile) file else null
    }

    override fun getPublicUrl(fileName: String): String {
        return "/api/photos/$fileName"
    }
}
