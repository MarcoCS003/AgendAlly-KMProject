package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import models.*
import network.BlogApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CreateEventDialog(
    channels: List<Channel>,
    userToken: String,
    userOrganizationId: Int,
    onSuccess: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Estados del formulario con valores por defecto
    val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    var title by remember { mutableStateOf("") }
    var shortDescription by remember { mutableStateOf("") }
    var longDescription by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(today) }
    var endDate by remember { mutableStateOf(today) }
    var selectedChannelId by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    fun validateAndShowConfirmation() {
        when {
            title.isBlank() -> errorMessage = "El título es obligatorio"
            shortDescription.isBlank() -> errorMessage = "La descripción corta es obligatoria"
            location.isBlank() -> errorMessage = "La ubicación es obligatoria"
            selectedChannelId == null -> errorMessage = "Debe seleccionar un canal"
            else -> {
                errorMessage = null
                showConfirmation = true
            }
        }
    }

    fun createEvent() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null

            val request = CreateEventRequest(
                title = title,
                shortDescription = shortDescription,
                longDescription = longDescription,
                location = location,
                startDate = startDate,
                endDate = endDate,
                organizationId = userOrganizationId,
                channelId = selectedChannelId,
                category = "INSTITUTIONAL"
            )

            println("🔍 Sending request: $request") // 🆕 AGREGAR LOG

            val result = BlogApi.instance.createEvent(request, userToken)

            println("🔍 API Result: ${result.isSuccess}") // 🆕 AGREGAR LOG
            if (result.isFailure) {
                println("🔍 Error: ${result.exceptionOrNull()?.message}") // 🆕 AGREGAR LOG
            }

            if (result.isSuccess) {
                onSuccess("Evento '$title' creado exitosamente")
            } else {
                errorMessage = result.exceptionOrNull()?.message ?: "Error creando evento"
                showConfirmation = false
            }

            isLoading = false
        }
    }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading
        )
    ) {
        Card(
            modifier = Modifier
                .width(600.dp)
                .heightIn(max = 700.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Añadir Evento a Blog",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    if (!isLoading) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (!showConfirmation) {
                    // Formulario principal
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Título
                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                errorMessage = null
                            },
                            label = { Text("Añadir Título") },
                            placeholder = { Text("Título del evento") },
                            enabled = !isLoading,
                            isError = title.isBlank() && errorMessage != null,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Descripción corta
                        OutlinedTextField(
                            value = shortDescription,
                            onValueChange = {
                                if (it.length <= 500) {
                                    shortDescription = it
                                    errorMessage = null
                                }
                            },
                            label = { Text("Texto corto") },
                            placeholder = { Text("Añade texto máximo 500 caracteres") },
                            supportingText = { Text("${shortDescription.length}/500") },
                            enabled = !isLoading,
                            isError = shortDescription.isBlank() && errorMessage != null,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Descripción completa
                        OutlinedTextField(
                            value = longDescription,
                            onValueChange = { longDescription = it },
                            label = { Text("Texto Completo") },
                            placeholder = { Text("Añade descripción detallada del evento...") },
                            enabled = !isLoading,
                            minLines = 4,
                            maxLines = 8,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Fechas
                        Text(
                            text = "Fecha",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = { startDate = it },
                                label = { Text("Fecha inicio") },
                                placeholder = { Text("YYYY-MM-DD") },
                                enabled = !isLoading,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = endDate,
                                onValueChange = { endDate = it },
                                label = { Text("Fecha fin") },
                                placeholder = { Text("YYYY-MM-DD") },
                                enabled = !isLoading,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Ubicación
                        OutlinedTextField(
                            value = location,
                            onValueChange = {
                                if (it.length <= 500) {
                                    location = it
                                    errorMessage = null
                                }
                            },
                            label = { Text("Ubicación") },
                            placeholder = { Text("Añade texto máximo 500 caracteres") },
                            supportingText = { Text("${location.length}/500") },
                            enabled = !isLoading,
                            isError = location.isBlank() && errorMessage != null,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Selector de canal
                        SimpleChannelSelector(
                            channels = channels,
                            selectedChannelId = selectedChannelId,
                            onChannelSelected = {
                                selectedChannelId = it
                                errorMessage = null
                            },
                            includeAllOption = false,
                            label = "Blog",
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Mostrar error
                        errorMessage?.let { error ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = error,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Pantalla de confirmación
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Confirmar publicación",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Evento: $title",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                val selectedChannel = channels.find { it.id == selectedChannelId }
                                Text(
                                    text = "Canal: ${selectedChannel?.name ?: "General"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = "Ubicación: $location",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = "Fecha: $startDate",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "¿Estás seguro que deseas publicar este evento?",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // Mostrar error de creación
                        errorMessage?.let { error ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = error,
                                    modifier = Modifier.padding(12.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    if (!showConfirmation) {
                        OutlinedButton(
                            onClick = onDismiss,
                            enabled = !isLoading
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = { validateAndShowConfirmation() },
                            enabled = !isLoading
                        ) {
                            Text("Continuar")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showConfirmation = false },
                            enabled = !isLoading
                        ) {
                            Text("Volver")
                        }

                        Button(
                            onClick = { createEvent() },
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }
}