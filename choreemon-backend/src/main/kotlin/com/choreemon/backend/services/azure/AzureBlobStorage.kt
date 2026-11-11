package com.choreemon.backend.services.azure

import com.choreemon.backend.config.AzureConfig
import com.choreemon.backend.services.IStorageService
import java.io.File
import java.io.InputStream

class AzureBlobStorage(private val config: AzureConfig) : IStorageService {
    // When deploying to Azure, use azure-storage-blob SDK with connection string or Managed Identity
    override suspend fun upload(fileName: String, inputStream: InputStream, contentType: String): String {
        val account = config.storageAccount ?: "choreemonstorage"
        return "https://$account.blob.core.windows.net/photos/$fileName"
    }

    override suspend fun getFile(fileName: String): File? {
        return null
    }

    override fun getPublicUrl(fileName: String): String {
        val account = config.storageAccount ?: "choreemonstorage"
        return "https://$account.blob.core.windows.net/photos/$fileName"
    }
}
