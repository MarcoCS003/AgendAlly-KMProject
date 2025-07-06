// composeApp/src/desktopMain/kotlin/ui/screens/ConnectivityTestScreen.kt
// ⚠️ PANTALLA TEMPORAL PARA TESTING - ELIMINAR DESPUÉS

package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import network.ConnectivityTest

@Composable
fun ConnectivityTestScreen() {
    var testResults by remember { mutableStateOf("🔄 Haz clic en 'Ejecutar Tests' para comenzar...") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Header
        Text(
            text = "🔬 Test de Conectividad Backend",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Verifica la conexión entre la app desktop y el backend Ktor",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botones de test
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    isLoading = true
                    testResults = "🔄 Ejecutando tests de conectividad..."

                    ConnectivityTest.runBasicConnectivityTest { results ->
                        testResults = results
                        isLoading = false
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Ejecutar Tests de API")
            }

            Button(
                onClick = {
                    ConnectivityTest.runSerializationTest { results ->
                        testResults = results
                    }
                },
                enabled = !isLoading
            ) {
                Text("Test Serialización")
            }

            OutlinedButton(
                onClick = {
                    testResults = "🔄 Haz clic en 'Ejecutar Tests' para comenzar..."
                }
            ) {
                Text("Limpiar")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Resultados
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            SelectionContainer {
                Text(
                    text = testResults,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Preview para desarrollo
 */
@Composable
fun ConnectivityTestScreenPreview() {
    MaterialTheme {
        ConnectivityTestScreen()
    }
}