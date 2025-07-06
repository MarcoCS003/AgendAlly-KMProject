package services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.auth.oauth2.GoogleCredentials
import java.io.FileInputStream

object FirebaseService {
    private var isInitialized = false

    fun initialize() {
        if (isInitialized) return

        try {
            // Buscar el archivo de credenciales
            val serviceAccountStream = object {}.javaClass.classLoader
                .getResourceAsStream("firebase-service-account.json")

            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                .build()

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
            }

            isInitialized = true
            println("✅ Firebase inicializado correctamente")

        } catch (e: Exception) {
            println("❌ Error inicializando Firebase: ${e.message}")
            throw e
        }
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
            null
        }
    }

    fun isReady(): Boolean = isInitialized
}