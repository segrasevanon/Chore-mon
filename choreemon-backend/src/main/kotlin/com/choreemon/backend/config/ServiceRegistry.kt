package com.choreemon.backend.config

import com.choreemon.backend.repositories.*
import com.choreemon.backend.services.IAuthService
import com.choreemon.backend.services.INotificationService
import com.choreemon.backend.services.IStorageService
import com.choreemon.backend.services.azure.AzureB2CAuth
import com.choreemon.backend.services.azure.AzureBlobStorage
import com.choreemon.backend.services.local.FcmDirectService
import com.choreemon.backend.services.local.LocalFileStorage
import com.choreemon.backend.services.local.LocalJwtAuth
import java.io.File

/**
 * Dependency injection root.
 *
 * Switch ENV=azure to swap every service implementation with its Azure counterpart.
 * All route code talks only to the interfaces — nothing changes in routes.
 */
class ServiceRegistry(
    val auth: IAuthService,
    val storage: IStorageService,
    val notifications: INotificationService,
    // Repositories (data access — always SQL, same code for local and Azure SQL)
    val households: HouseholdRepository,
    val members: MemberRepository,
    val chores: ChoreRepository,
    val completions: CompletionRepository,
    val creatures: CreatureRepository,
) {
    companion object {
        fun create(config: AppConfig): ServiceRegistry {
            val auth: IAuthService
            val storage: IStorageService

            if (config.isAzure) {
                // ─── Azure implementations ──────────────────────────────────
                auth    = AzureB2CAuth(config.azure)
                storage = AzureBlobStorage(config.azure)
            } else {
                // ─── Local implementations (no Azure account needed) ─────────
                auth    = LocalJwtAuth(config.jwt)
                storage = LocalFileStorage(File(config.storage.uploadDir))
            }

            // FCM works the same in both environments (direct FCM call or log-only in dev)
            val notifications = FcmDirectService(config.fcm.serverKey)

            return ServiceRegistry(
                auth          = auth,
                storage       = storage,
                notifications = notifications,
                households    = HouseholdRepository(),
                members       = MemberRepository(),
                chores        = ChoreRepository(),
                completions   = CompletionRepository(),
                creatures     = CreatureRepository(),
            )
        }
    }
}
