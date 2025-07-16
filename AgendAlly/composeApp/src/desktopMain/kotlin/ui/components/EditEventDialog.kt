// composeApp/src/desktopMain/kotlin/ui/components/EditEventDialog.kt
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

@Composable
fun EditEventDialog(
    event: BlogEvent,
    channels: List<Channel>,
    userToken: String,
    onSuccess: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Estados del formulario inicializados con datos del evento
    var title by remember { mutableStateOf(event.title) }
    var shortDescription by remember { mutableStateOf(event.shortDescription) }
    var longDescription by remember { mutableStateOf(event.longDescription) }
    var location by remember { mutableStateOf(event.location) }
    var startDate by remember { mutableStateOf(event.startDate ?: "") }
    var endDate by remember { mutableStateOf(event.endDate ?: "") }
    var selectedChannelId by remember { mutableStateOf(event.channelId) }
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

    fun updateEvent() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null

            val request = UpdateEventRequest(
                title = title,
                shortDescription = shortDescription,
                longDescription = longDescription,
                location = location,
                startDate = startDate.ifBlank { null },
                endDate = endDate.ifBlank { null },
                channelId = selectedChannelId,
                category = event.category
            )

            val result = BlogApi.instance.updateEvent(event.id, request, userToken)

            if (result.isSuccess) {
                onSuccess("Evento '$title' actualizado exitosamente")
            } else {
                errorMessage = result.exceptionOrNull()?.message ?: "Error actualizando evento"
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
                        text = "Editar Evento",
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
                    // Formulario de edición
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
                            label = { Text("Título del evento") },
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
                            label = { Text("Descripción corta") },
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
                            label = { Text("Descripción completa") },
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
                            label = "Canal",
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
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Confirmar cambios",
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

                                if (startDate.isNotBlank()) {
                                    Text(
                                        text = "Fecha: $startDate",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Text(
                            text = "¿Estás seguro que deseas guardar los cambios?",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // Mostrar error de actualización
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
                            onClick = { updateEvent() },
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
                            Text("Guardar Cambios")
                        }
                    }
                }
            }
        }
    }
}