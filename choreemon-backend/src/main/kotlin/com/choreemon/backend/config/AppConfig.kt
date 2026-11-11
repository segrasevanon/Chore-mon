package com.choreemon.backend.config

data class AppConfig(
    val env: String,
    val db: DbConfig,
    val jwt: JwtConfig,
    val storage: StorageConfig,
    val fcm: FcmConfig,
    val azure: AzureConfig,
) {
    val isAzure get() = env == "azure"

    companion object {
        fun load(): AppConfig = AppConfig(
            env = env("ENV", "local"),
            db = DbConfig(
                host     = env("DB_HOST", "localhost"),
                port     = env("DB_PORT", "1433").toInt(),
                name     = env("DB_NAME", "choreemon"),
                user     = env("DB_USER", "sa"),
                password = env("DB_PASSWORD", "Local_Dev_P@ss1"),
            ),
            jwt = JwtConfig(
                secret      = env("JWT_SECRET", "dev-secret-CHANGE-IN-PRODUCTION"),
                issuer      = env("JWT_ISSUER", "choreemon"),
                expiryHours = env("JWT_EXPIRY_HOURS", "168").toLong(),
            ),
            storage = StorageConfig(
                endpoint  = env("STORAGE_ENDPOINT", "http://localhost:9000"),
                bucket    = env("STORAGE_BUCKET", "photos"),
                accessKey = env("STORAGE_ACCESS_KEY", "minioadmin"),
                secretKey = env("STORAGE_SECRET_KEY", "minioadmin"),
                uploadDir = env("STORAGE_UPLOAD_DIR", "./uploads"),
            ),
            fcm = FcmConfig(serverKey = System.getenv("FCM_SERVER_KEY")),
            azure = AzureConfig(
                storageAccount          = System.getenv("AZURE_STORAGE_ACCOUNT"),
                storageKey              = System.getenv("AZURE_STORAGE_KEY"),
                b2cTenant               = System.getenv("AZURE_B2C_TENANT"),
                b2cClientId             = System.getenv("AZURE_B2C_CLIENT_ID"),
                notificationHubConnection = System.getenv("AZURE_NOTIFICATION_HUB_CONNECTION"),
                notificationHubName     = System.getenv("AZURE_NOTIFICATION_HUB_NAME"),
            ),
        )

        private fun env(key: String, default: String): String =
            System.getenv(key)?.takeIf { it.isNotBlank() } ?: default
    }
}

data class DbConfig(val host: String, val port: Int, val name: String, val user: String, val password: String)
data class JwtConfig(val secret: String, val issuer: String, val expiryHours: Long)
data class StorageConfig(val endpoint: String, val bucket: String, val accessKey: String, val secretKey: String, val uploadDir: String)
data class FcmConfig(val serverKey: String?)
data class AzureConfig(
    val storageAccount: String?,
    val storageKey: String?,
    val b2cTenant: String?,
    val b2cClientId: String?,
    val notificationHubConnection: String?,
    val notificationHubName: String?,
)
