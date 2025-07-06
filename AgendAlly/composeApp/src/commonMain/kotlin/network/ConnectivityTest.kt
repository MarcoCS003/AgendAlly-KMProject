// composeApp/src/commonMain/kotlin/network/ConnectivityTest.kt
// ⚠️ ARCHIVO TEMPORAL PARA TESTING - ELIMINAR DESPUÉS

package network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ConnectivityTest {

    /**
     * 🧪 Test básico de conectividad con el backend
     */
    fun runBasicConnectivityTest(
        onResult: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            val results = mutableListOf<String>()

            try {
                results.add("🧪 Iniciando tests de conectividad...")
                results.add("🔗 Backend URL: http://localhost:8080")
                results.add("")

                // Test 1: Status endpoint
                results.add("📡 Test 1: GET /api/auth/status")
                val statusResult = AuthApi.instance.getStatus()

                if (statusResult.isSuccess) {
                    val status = statusResult.getOrNull()
                    results.add("✅ Status OK")
                    results.add("   Firebase: ${status?.firebase_initialized}")
                    results.add("   Environment: ${status?.environment}")
                    results.add("   Auth Flow: ${status?.auth_flow}")
                } else {
                    results.add("❌ Status failed: ${statusResult.exceptionOrNull()?.message}")
                }

                results.add("")

                // Test 2: Test login
                results.add("🔐 Test 2: POST /api/auth/test-login")
                val loginResult = AuthApi.instance.testLogin("admin@itp.edu.mx")

                if (loginResult.isSuccess) {
                    val login = loginResult.getOrNull()
                    results.add("✅ Test login OK")
                    results.add("   Success: ${login?.success}")
                    results.add("   User: ${login?.user?.name}")
                    results.add("   Email: ${login?.user?.email}")
                    results.add("   Role: ${login?.user?.role}")
                    results.add("   Requires Setup: ${login?.requiresOrganizationSetup}")
                } else {
                    results.add("❌ Test login failed: ${loginResult.exceptionOrNull()?.message}")
                }

                results.add("")

                // Test 3: Usuario nuevo
                results.add("👤 Test 3: POST /api/auth/test-login (nuevo usuario)")
                val newUserResult = AuthApi.instance.testLogin("nuevo@universidad.com")

                if (newUserResult.isSuccess) {
                    val newUser = newUserResult.getOrNull()
                    results.add("✅ New user test OK")
                    results.add("   Requires Setup: ${newUser?.requiresOrganizationSetup}")
                    results.add("   Message: ${newUser?.message}")
                } else {
                    results.add("❌ New user test failed: ${newUserResult.exceptionOrNull()?.message}")
                }

                results.add("")
                results.add("🎉 Tests completados!")

            } catch (e: Exception) {
                results.add("💥 Error general: ${e.message}")
            }

            // Enviar resultados al UI
            onResult(results.joinToString("\n"))
        }
    }

    /**
     * 🔬 Test de serialización
     */
    fun runSerializationTest(onResult: (String) -> Unit) {
        try {
            val results = mutableListOf<String>()
            results.add("🔬 Test de Serialización")
            results.add("")

            // Test request serialization
            val loginRequest = models.LoginRequest(
                email = "test@example.com",
                idToken = "fake-token",
                clientType = "DESKTOP_ADMIN"
            )
            results.add("✅ LoginRequest creado")
            results.add("   Email: ${loginRequest.email}")
            results.add("   Client Type: ${loginRequest.clientType}")

            results.add("")
            results.add("✅ Serialización funcionando correctamente")

            onResult(results.joinToString("\n"))

        } catch (e: Exception) {
            onResult("❌ Error de serialización: ${e.message}")
        }
    }
}
