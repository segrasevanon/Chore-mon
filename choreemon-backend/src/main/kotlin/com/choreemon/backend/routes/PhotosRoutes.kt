package com.choreemon.backend.routes

import com.choreemon.backend.config.ServiceRegistry
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.photosRoutes(registry: ServiceRegistry) {
    route("/photos") {
        post("/upload") {
            val multipart = call.receiveMultipart()
            var uploadedUrl: String? = null

            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    val ext = part.originalFileName?.substringAfterLast('.', "jpg") ?: "jpg"
                    val fileName = "${UUID.randomUUID()}.$ext"
                    val contentType = part.contentType?.toString() ?: "image/jpeg"
                    part.streamProvider().use { stream ->
                        uploadedUrl = registry.storage.upload(fileName, stream, contentType)
                    }
                }
                part.dispose()
            }

            if (uploadedUrl != null) {
                call.respond(HttpStatusCode.Created, mapOf("url" to uploadedUrl))
            } else {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No file provided"))
            }
        }

        get("/{filename}") {
            val filename = call.parameters["filename"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val file = registry.storage.getFile(filename)
            if (file != null && file.exists()) {
                call.respondFile(file)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
