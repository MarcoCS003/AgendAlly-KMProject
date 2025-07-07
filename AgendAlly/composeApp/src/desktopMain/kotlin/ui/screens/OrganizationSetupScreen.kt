// composeApp/src/commonMain/kotlin/ui/screens/OrganizationSetupScreen.kt
package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationSetupScreen(
    userEmail: String,
    onSetupComplete: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var organizationName by remember { mutableStateOf("") }
    var acronym by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf(userEmail) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Validaciones
    val isFormValid = organizationName.trim().length >= 3 &&
            acronym.trim().length >= 2 &&
            contactEmail.contains("@")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Configura tu Organización",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Para completar tu registro, necesitamos información sobre tu institución educativa",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Form
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nombre de organización
                OutlinedTextField(
                    value = organizationName,
                    onValueChange = { organizationName = it },
                    label = { Text("Nombre de la Institución") },
                    placeholder = { Text("Ej: Instituto Tecnológico de Puebla") },
                    leadingIcon = {
                        Icon(Icons.Default.School, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = organizationName.isNotEmpty() && organizationName.trim().length < 3
                )

                // Acrónimo
                OutlinedTextField(
                    value = acronym,
                    onValueChange = { if (it.length <= 10) acronym = it.uppercase() },
                    label = { Text("Acrónimo/Código") },
                    placeholder = { Text("Ej: ITP") },
                    leadingIcon = {
                        Icon(Icons.Default.Tag, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("Máximo 10 caracteres") },
                    isError = acronym.isNotEmpty() && acronym.trim().length < 2
                )

                // Email de contacto
                OutlinedTextField(
                    value = contactEmail,
                    onValueChange = { contactEmail = it },
                    label = { Text("Email de Contacto") },
                    placeholder = { Text("admin@institucion.edu.mx") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = contactEmail.isNotEmpty() && !contactEmail.contains("@")
                )

                // Error message
                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = errorMessage!!,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón Omitir
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Omitir por ahora")
                    }

                    // Botón Crear
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    isLoading = true
                                    errorMessage = null

                                    // TODO: Llamar API real
                                    kotlinx.coroutines.delay(2000) // Simular API call

                                    // Simular éxito
                                    onSetupComplete()

                                } catch (e: Exception) {
                                    errorMessage = "Error al crear organización: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isFormValid && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isLoading) "Creando..." else "Crear Organización")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info adicional
        Text(
            text = "Podrás modificar esta información más tarde en Configuración",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
