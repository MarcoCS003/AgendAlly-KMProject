// AgendAlly/composeApp/src/desktopMain/kotlin/ui/screens/AddChannelScreen.kt
package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import network.ChannelsApi  // ✅ IMPORTAR API

@Composable
fun AddChannelScreen(
    userToken: String,
    userOrganizationId: Int,  // ✅ AGREGAR PARÁMETRO
    onNavigateBack: () -> Unit,
    onChannelCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados del formulario
    var channelName by remember { mutableStateOf("") }
    var channelAcronym by remember { mutableStateOf("") }
    var channelEmail by remember { mutableStateOf("") }
    var channelPhone by remember { mutableStateOf("") }

    // Estados de UI
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Estados de validación
    var nameError by remember { mutableStateOf<String?>(null) }
    var acronymError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Función de validación
    fun validateForm(): Boolean {
        var isValid = true

        nameError = when {
            channelName.isBlank() -> { isValid = false; "El nombre es obligatorio" }
            channelName.length < 3 -> { isValid = false; "Mínimo 3 caracteres" }
            else -> null
        }

        acronymError = when {
            channelAcronym.isBlank() -> { isValid = false; "El acrónimo es obligatorio" }
            channelAcronym.length < 2 -> { isValid = false; "Mínimo 2 caracteres" }
            else -> null
        }

        emailError = when {
            channelEmail.isNotBlank() && !channelEmail.contains("@") -> {
                isValid = false; "Email inválido"
            }
            else -> null
        }

        return isValid
    }

    // ✅ FUNCIÓN REAL PARA GUARDAR CANAL
    fun saveChannel() {
        if (!validateForm()) return

        coroutineScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val result = ChannelsApi.instance.createChannel(
                    name = channelName,
                    acronym = channelAcronym,
                    description = "", // Podrías agregar un campo de descripción después
                    email = channelEmail.ifBlank { null },
                    phone = channelPhone.ifBlank { null },
                    organizationId = userOrganizationId,  // ✅ USAR PARÁMETRO
                    authToken = userToken
                )

                if (result.isSuccess) {
                    onChannelCreated()
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Error creando canal"
                }

            } catch (e: Exception) {
                errorMessage = e.message ?: "Error desconocido"
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        // Header con botón de regreso
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver"
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Agregar Canal",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Formulario
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Nombre completo
                OutlinedTextField(
                    value = channelName,
                    onValueChange = {
                        channelName = it
                        nameError = null
                    },
                    label = { Text("Nombre completo") },
                    placeholder = { Text("Nombre de la carrera / facultad") },
                    leadingIcon = {
                        Icon(Icons.Default.School, contentDescription = null)
                    },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                // Nombre corto / Acrónimo
                OutlinedTextField(
                    value = channelAcronym,
                    onValueChange = {
                        channelAcronym = it.uppercase().filter { char -> char.isLetterOrDigit() }
                        acronymError = null
                    },
                    label = { Text("Nombre corto") },
                    placeholder = { Text("acrónimo o nombre corto") },
                    leadingIcon = {
                        Icon(Icons.Default.Tag, contentDescription = null)
                    },
                    isError = acronymError != null,
                    supportingText = acronymError?.let { { Text(it) } },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                // Email
                OutlinedTextField(
                    value = channelEmail,
                    onValueChange = {
                        channelEmail = it
                        emailError = null
                    },
                    label = { Text("correo electrónico") },
                    placeholder = { Text("correo de electrónico de contacto") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = emailError != null,
                    supportingText = emailError?.let { { Text(it) } },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                // Teléfono
                OutlinedTextField(
                    value = channelPhone,
                    onValueChange = { channelPhone = it },
                    label = { Text("Teléfono") },
                    placeholder = { Text("teléfono de contacto") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Mostrar error si existe
        errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Botón de guardar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { saveChannel() },
                enabled = !isLoading && channelName.isNotBlank() && channelAcronym.isNotBlank(),
                modifier = Modifier.width(180.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Guardar Cambios")
            }
        }
    }
}