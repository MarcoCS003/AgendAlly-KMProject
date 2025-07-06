import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.storage.v2.ServiceAccount
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
            val serviceAccount: InputStream = getServiceAccountStream()
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()
            FirebaseApp.initializeApp(options)
            isInitialized = true

            println("✅ Firebase Admin SDK inicializado correctamente")
            println("🔑 Project ID: ${FirebaseApp.getInstance().options.projectId}")

        } catch (e: Exception) {
            print("Error: ${e.message}")
            throw RuntimeException("Error: ${e.message}")
        }


    }

    private fun getServiceAccountStream(): InputStream {
        val firebaseCredentials = System.getenv("FIREBASE_CREDENTIALS")
        if (!firebaseCredentials.isNullOrBlank()) {
            println("🔑 Usando credenciales de variable de entorno")
            return firebaseCredentials.byteInputStream()
        }

        // 2. Archivo local (para desarrollo)
        val localFile = "firebase-service-account.json"
        try {
            println("🔑 Buscando archivo local: $localFile")
            return FileInputStream(localFile)
        } catch (e: Exception) {
            println("⚠️ No se encontró archivo local: $localFile")
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

        throw IllegalStateException(
            """
            ❌ No se encontraron credenciales de Firebase. Configura una de estas opciones:
            
            1. Variable de entorno FIREBASE_CREDENTIALS con el JSON completo
            2. Archivo firebase-service-account.json en la raíz del proyecto
            3. Archivo firebase-service-account.json en src/main/resources/
            
            Descarga las credenciales desde:
            Firebase Console → Configuración → Cuentas de servicio → Generar nueva clave privada
            """.trimIndent()
        )
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