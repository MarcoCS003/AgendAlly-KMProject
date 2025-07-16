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
import models.Channel
import network.ChannelsApi

@Composable
fun EditChannelScreen(
    channel: Channel,
    userToken: String,
    onNavigateBack: () -> Unit,
    onChannelUpdated: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados del formulario - inicializados con datos del canal
    var channelName by remember { mutableStateOf(channel.name) }
    var channelAcronym by remember { mutableStateOf(channel.acronym) }
    var channelEmail by remember { mutableStateOf(channel.email ?: "") }
    var channelPhone by remember { mutableStateOf(channel.phone ?: "") }
    var channelDescription by remember { mutableStateOf(channel.description) }

    // Estados de UI
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Estados de validación
    var nameError by remember { mutableStateOf<String?>(null) }
    var acronymError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Determinar si es el canal General (no editable)
    val isGeneralChannel = channel.acronym == "GEN"

    // Función de validación
    fun validateForm(): Boolean {
        var isValid = true

        if (!isGeneralChannel) {
            nameError = when {
                channelName.isBlank() -> {
                    isValid = false; "El nombre es obligatorio"
                }

                channelName.length < 3 -> {
                    isValid = false; "Mínimo 3 caracteres"
                }

                else -> null
            }

            acronymError = when {
                channelAcronym.isBlank() -> {
                    isValid = false; "El acrónimo es obligatorio"
                }

                channelAcronym.length < 2 -> {
                    isValid = false; "Mínimo 2 caracteres"
                }

                else -> null
            }
        }

        emailError = when {
            channelEmail.isNotBlank() && !channelEmail.contains("@") -> {
                isValid = false; "Email inválido"
            }

            else -> null
        }

        return isValid
    }

    // Función para actualizar canal
    fun updateChannel() {
        if (!validateForm()) return

        coroutineScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val result = ChannelsApi.instance.updateChannel(
                    channelId = channel.id,
                    name = channelName,
                    acronym = channelAcronym,
                    description = channelDescription,
                    email = channelEmail.ifBlank { null },
                    phone = channelPhone.ifBlank { null },
                    authToken = userToken
                )

                if (result.isSuccess) {
                    onChannelUpdated()
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Error actualizando canal"
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
                    text = if (isGeneralChannel) "Ver Canal General" else "Editar Canal",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))


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
                    enabled = !isLoading && !isGeneralChannel,
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // Acrónimo
                OutlinedTextField(
                    value = channelAcronym,
                    onValueChange = {

                            channelAcronym = it.uppercase()
                            acronymError = null

                    },
                    label = { Text("Acrónimo") },
                    placeholder = { Text("TICS, ING, etc.") },
                    leadingIcon = {
                        Icon(Icons.Default.Tag, contentDescription = null)
                    },
                    enabled = !isLoading && !isGeneralChannel,
                    isError = acronymError != null,
                    supportingText = acronymError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // Descripción
                OutlinedTextField(
                    value = channelDescription,
                    onValueChange = {
                        channelDescription = it
                    },
                    label = { Text("Descripción") },
                    placeholder = { Text("Descripción del canal") },
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = null)
                    },
                    enabled = !isLoading,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Email
                OutlinedTextField(
                    value = channelEmail,
                    onValueChange = {
                        channelEmail = it
                        emailError = null
                    },
                    label = { Text("Email de contacto") },
                    placeholder = { Text("contacto@ejemplo.com") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = !isLoading,
                    isError = emailError != null,
                    supportingText = emailError?.let { { Text(it) } },
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

        // Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
        ) {
            // Botón Cancelar
            OutlinedButton(
                onClick = onNavigateBack,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }

            // Botón Guardar cambios
            Button(
                onClick = { updateChannel() },
                enabled = !isLoading && (
                        channelName != channel.name ||
                                channelAcronym != channel.acronym ||
                                channelDescription != channel.description ||
                                channelEmail != (channel.email ?: "") ||
                                channelPhone != (channel.phone ?: "")
                        ),
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
                Text(if (isGeneralChannel) "Guardar Contacto" else "Guardar Cambios")
            }
        }
    }
}