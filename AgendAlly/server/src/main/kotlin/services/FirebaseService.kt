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
    fun forceReset() {
        println("🔥 Forzando reset de FirebaseService...")
        isInitialized = false
        try {
            val existingApps = FirebaseApp.getApps()
            existingApps.forEach { app ->
                println("   - Eliminando app: ${app.name}")
                app.delete()
            }
        } catch (e: Exception) {
            println("⚠️ Error eliminando apps: ${e.message}")
        }
    }


    fun initialize() {

        if (isInitialized) {
            println("Firebase ya está inicializado.")
            return
        }

        // 🔥 FORZAR RESET AL INICIO
        forceReset()
        try {
            println("🔍 ===== DEBUG FIREBASE SERVICE INIT =====")

            // 🔥 FORZAR ELIMINACIÓN DE FIREBASE APPS EXISTENTES
            println("🔥 Verificando apps existentes...")
            val existingApps = FirebaseApp.getApps()
            if (existingApps.isNotEmpty()) {
                println("⚠️ Encontradas ${existingApps.size} apps existentes, eliminando...")
                existingApps.forEach { app ->
                    println("   - Eliminando app: ${app.name} (Project: ${app.options.projectId})")
                    app.delete()
                }
                println("✅ Apps eliminadas")
            }

            val serviceAccountStream = getServiceAccountStream()

            // 🔍 Leer y verificar el contenido del service account
            val serviceAccountContent = serviceAccountStream.readBytes()
            val jsonString = String(serviceAccountContent)
            println("📄 Service Account JSON (primeros 200 chars):")
            println(jsonString.take(200))

            // Verificar que contiene project_id correcto
            if (jsonString.contains("agendally-6226b")) {
                println("✅ Service account contiene project_id: agendally-6226b")
            } else {
                println("❌ Service account NO contiene agendally-6226b")
            }

            // Recrear stream desde el contenido
            val credentials = GoogleCredentials.fromStream(serviceAccountContent.inputStream())
            println("🔑 Credentials cargadas:")
            println("   - Quota Project ID: ${credentials.quotaProjectId}")

            val options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId("agendally-6226b")  // ← FORZAR EL PROJECT ID
                .build()

            println("🔧 Firebase Options:")
            println("   - Project ID: ${options.projectId}")
            println("   - Service Account ID: ${options.serviceAccountId}")

            // 🔥 INICIALIZAR NUEVA APP
            println("🔥 Inicializando nueva FirebaseApp...")
            FirebaseApp.initializeApp(options)
            println("✅ FirebaseApp inicializada")

            val app = FirebaseApp.getInstance()
            isInitialized = true

            println("✅ Firebase inicializado correctamente")
            println("🔑 Project ID final: ${app.options.projectId}")

            // Verificar FirebaseAuth
            val auth = FirebaseAuth.getInstance(app)
            println("🔐 FirebaseAuth disponible: ${auth != null}")

            println("🔍 ===== FIN DEBUG FIREBASE SERVICE INIT =====")

        } catch (e: Exception) {
            println("❌ ===== ERROR FIREBASE SERVICE INIT =====")
            println("❌ Error: ${e.message}")
            println("❌ Tipo: ${e.javaClass.simpleName}")
            e.printStackTrace()
            println("❌ ===== FIN ERROR =====")
            throw e
        }
    }
    private fun configureClientIds() {
        // Configurar CLIENT_IDs autorizados para validación
        val authorizedClientIds = listOf(
            "agendally-6226b", // Project ID
            "648744731730-kulq054dv32bgdbg9bo8d5ga831q9dps.apps.googleusercontent.com" // OAuth CLIENT_ID
        )

        // Configurar Firebase para aceptar ambos audiences
        System.setProperty("FIREBASE_AUTHORIZED_CLIENT_IDS", authorizedClientIds.joinToString(","))
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