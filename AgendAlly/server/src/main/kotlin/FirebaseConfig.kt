import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import java.io.FileInputStream
import java.io.InputStream


object FirebaseConfig {
    private var isInitialized = false

    fun initialize(){
        if (isInitialized){
            println("Firebase is already initialized.")
            return
        }

        try {
            val existingApp = FirebaseApp.getInstance()
            if (existingApp != null) {
                println("✅ Firebase ya estaba inicializado")
                println("🔑 Project ID: ${existingApp.options.projectId}")
                println("🔑 Service Account Email: hola")
                isInitialized = true
                return
            }
        } catch (e: IllegalStateException) {
            // No existe, continuar con inicialización
            println("🔄 Inicializando Firebase por primera vez...")
        }

        try {
            val serviceAccount: InputStream = getServiceAccountStream()

            // ✅ VERIFICAR EL CONTENIDO DEL ARCHIVO
            val credentials = GoogleCredentials.fromStream(serviceAccount)
            println("🔍 Credentials obtenidas: ${credentials.javaClass.simpleName}")

            val options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build()

            FirebaseApp.initializeApp(options)
            isInitialized = true

            val app = FirebaseApp.getInstance()
            println("✅ Firebase Admin SDK inicializado correctamente")
            println("🔑 Project ID: ${app.options.projectId}")
            println("🔑 Application Name: ${app.name}")

        } catch (e: Exception) {
            println("❌ Error inicializando Firebase: ${e.message}")
            e.printStackTrace()
            throw RuntimeException("Error: ${e.message}")
        }
    }

    private fun getServiceAccountStream(): InputStream {
        val firebaseCredentials = System.getenv("FIREBASE_CREDENTIALS")
        if (!firebaseCredentials.isNullOrBlank()) {
            println("🔑 Usando credenciales de variable de entorno")
            return firebaseCredentials.byteInputStream()
        }

        // 2. Archivo local (para desarrollo) - MÚLTIPLES UBICACIONES
        val possiblePaths = listOf(
            "firebase-service-account.json",
            "../firebase-service-account.json",
            "server/firebase-service-account.json",
            "src/main/resources/firebase-service-account.json"
        )

        for (path in possiblePaths) {
            try {
                println("🔑 Probando ruta: $path")
                return FileInputStream(path)
            } catch (e: Exception) {
                println("⚠️ No encontrado en: $path")
            }
        }

        // 3. Recursos del proyecto
        try {
            println("🔑 Buscando en recursos del proyecto")
            val resourceStream = this::class.java.classLoader.getResourceAsStream("firebase-service-account.json")
            if (resourceStream != null) {
                return resourceStream
            }
        } catch (e: Exception) {
            println("⚠️ No se encontró en recursos: ${e.message}")
        }

        throw IllegalStateException("❌ No se encontraron credenciales de Firebase")
    }


    fun getAuth(): FirebaseAuth {
        if (!isInitialized) {
            initialize()
        }
        return FirebaseAuth.getInstance()
    }
    fun isReady(): Boolean = isInitialized

    // Para testing y desarrollo
    fun reset() {
        isInitialized = false
        try {
            FirebaseApp.getInstance().delete()
        } catch (e: Exception) {
            // Ignorar si no existe
        }
    }

}