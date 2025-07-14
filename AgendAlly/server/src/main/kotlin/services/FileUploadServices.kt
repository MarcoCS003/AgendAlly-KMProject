// AgendAlly/server/src/main/kotlin/services/FileUploadService.kt
package services

import com.example.ImageInfo
import com.example.UploadResult
import io.ktor.http.content.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

/**
 * 📁 Servicio para manejo de archivos e imágenes
 */
class FileUploadService {

    companion object {
        private const val UPLOAD_DIR = "uploads"
        private const val IMAGES_DIR = "$UPLOAD_DIR/images"
        private const val MAX_FILE_SIZE = 5 * 1024 * 1024 // 5MB
        private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp")
        private val ALLOWED_MIME_TYPES = setOf(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
        )
    }

    init {
        // Crear directorios si no existen
        File(UPLOAD_DIR).mkdirs()
        File(IMAGES_DIR).mkdirs()
    }

    /**
     * 📤 Subir imagen
     */
    suspend fun uploadImage(multipart: MultiPartData): UploadResult {
        return withContext(Dispatchers.IO) {
            try {
                var fileName: String? = null
                var fileBytes: ByteArray? = null
                var originalFileName: String? = null
                var contentType: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            originalFileName = part.originalFileName
                            contentType = part.contentType?.toString()
                            fileBytes = part.streamProvider().readBytes()
                        }
                        else -> part.dispose()
                    }
                }

                // Validaciones
                if (fileBytes == null || originalFileName == null) {
                    return@withContext UploadResult.Error("No se encontró archivo para subir")
                }

                if (fileBytes!!.size > MAX_FILE_SIZE) {
                    return@withContext UploadResult.Error("Archivo muy grande. Máximo 5MB permitido")
                }

                val fileExtension = originalFileName!!.substringAfterLast(".", "").lowercase()
                if (fileExtension !in ALLOWED_EXTENSIONS) {
                    return@withContext UploadResult.Error("Tipo de archivo no permitido. Solo: ${ALLOWED_EXTENSIONS.joinToString()}")
                }

                if (contentType != null && contentType !in ALLOWED_MIME_TYPES) {
                    return@withContext UploadResult.Error("Tipo MIME no permitido")
                }

                // Generar nombre único
                val uniqueFileName = "${UUID.randomUUID()}_${System.currentTimeMillis()}.$fileExtension"
                val file = File(IMAGES_DIR, uniqueFileName)

                // Guardar archivo
                file.writeBytes(fileBytes!!)

                println("✅ Imagen guardada: $uniqueFileName (${fileBytes!!.size} bytes)")

                UploadResult.Success(
                    fileName = uniqueFileName,
                    originalName = originalFileName!!,
                    size = fileBytes!!.size.toLong(),
                    url = "/api/images/$uniqueFileName",
                    path = file.absolutePath
                )

            } catch (e: Exception) {
                println("❌ Error subiendo imagen: ${e.message}")
                UploadResult.Error("Error subiendo archivo: ${e.message}")
            }
        }
    }

    /**
     * 🗑️ Eliminar imagen
     */
    fun deleteImage(fileName: String): Boolean {
        return try {
            val file = File(IMAGES_DIR, fileName)
            if (file.exists() && file.isFile) {
                val deleted = file.delete()
                if (deleted) {
                    println("✅ Imagen eliminada: $fileName")
                } else {
                    println("❌ No se pudo eliminar: $fileName")
                }
                deleted
            } else {
                println("⚠️ Archivo no encontrado: $fileName")
                false
            }
        } catch (e: Exception) {
            println("❌ Error eliminando imagen: ${e.message}")
            false
        }
    }

    /**
     * 📋 Listar imágenes
     */
    fun listImages(): List<ImageInfo> {
        return try {
            File(IMAGES_DIR).listFiles()
                ?.filter { it.isFile && it.extension.lowercase() in ALLOWED_EXTENSIONS }
                ?.map { file ->
                    ImageInfo(
                        fileName = file.name,
                        size = file.length(),
                        url = "/api/images/${file.name}",
                        lastModified = file.lastModified()
                    )
                }
                ?.sortedByDescending { it.lastModified }
                ?: emptyList()
        } catch (e: Exception) {
            println("❌ Error listando imágenes: ${e.message}")
            emptyList()
        }
    }

    /**
     * 📁 Obtener archivo
     */
    fun getImageFile(fileName: String): File? {
        val file = File(IMAGES_DIR, fileName)
        return if (file.exists() && file.isFile) file else null
    }
}
