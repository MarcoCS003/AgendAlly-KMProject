package services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.auth.oauth2.GoogleCredentials
import java.io.FileInputStream
import java.io.InputStream

object FirebaseService {
    private var isInitialized = false

    fun initialize() {
        if (isInitialized) return

        try {
            val serviceAccountStream = getServiceAccountStream()

            val credentials = GoogleCredentials.fromStream(serviceAccountStream)
            val options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build()

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
            }

            val app = FirebaseApp.getInstance()
            isInitialized = true

            println("✅ Firebase inicializado correctamente")
            println("🔑 Project ID: ${app.options.projectId}")

        } catch (e: Exception) {
            println("❌ Error inicializando Firebase: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    private fun getServiceAccountStream(): InputStream {
        // 1. Recursos del proyecto
        try {
            println("🔑 Buscando en resources...")
            val resourceStream = object {}.javaClass.classLoader
                .getResourceAsStream("firebase-service-account.json")
            if (resourceStream != null) {
                println("✅ Encontrado en resources")
                return resourceStream
            }
        } catch (e: Exception) {
            println("⚠️ Error en resources: ${e.message}")
        }

        // 2. Variable de entorno
        val firebaseCredentials = System.getenv("FIREBASE_CREDENTIALS")
        if (!firebaseCredentials.isNullOrBlank()) {
            println("🔑 Usando variable de entorno")
            return firebaseCredentials.byteInputStream()
        }

        // 3. Archivos locales
        val paths = listOf(
            "firebase-service-account.json",
            "server/firebase-service-account.json",
            "../firebase-service-account.json"
        )

        for (path in paths) {
            try {
                println("🔑 Probando: $path")
                val file = java.io.File(path)
                if (file.exists()) {
                    val content = file.readText()
                    println("📄 Tamaño del archivo: ${content.length} chars")
                    println("📄 Primeros 100 chars: ${content.take(100)}")
                    return FileInputStream(path)
                }
            } catch (e: Exception) {
                println("⚠️ No encontrado: $path")
            }
        }

        throw IllegalStateException("❌ No se encontró firebase-service-account.json")
    }

    fun verifyIdToken(idToken: String): FirebaseToken? {
        return try {
            if (!isInitialized) {
                initialize()
            }

            val decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken)
            println("✅ Token verificado para usuario: ${decodedToken.email}")
            decodedToken

        } catch (e: Exception) {
            println("❌ Error verificando token: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    fun getAuth(): FirebaseAuth {
        if (!isInitialized) {
            initialize()
        }
        return FirebaseAuth.getInstance()
    }
    fun isReady(): Boolean = isInitialized
}