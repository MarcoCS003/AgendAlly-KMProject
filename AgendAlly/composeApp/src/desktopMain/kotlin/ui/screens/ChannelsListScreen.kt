package ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import models.Channel
import network.ChannelsApi

@Composable
fun ChannelsListScreen(
    userToken: String,
    onNavigateToAddChannel: () -> Unit,
    onNavigateBack: () -> Unit,
    onEditChannel: (Channel) -> Unit,
    modifier: Modifier = Modifier,
    userOrganizationId: Int
) {
    // Estados principales
    var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    // Estados para eliminar canal
    var showDeleteDialog by remember { mutableStateOf(false) }
    var channelToDelete by remember { mutableStateOf<Channel?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Cargar canales al iniciar
    LaunchedEffect(userToken) {
        isLoading = true
        errorMessage = null

        val result = ChannelsApi.instance.getMyChannels(userToken, userOrganizationId)

        if (result.isSuccess) {
            channels = result.getOrNull() ?: emptyList()
        } else {
            errorMessage = result.exceptionOrNull()?.message ?: "Error cargando canales"
        }

        isLoading = false
    }

    // Función para cargar canales
    fun loadChannels() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null

            val result = ChannelsApi.instance.getMyChannels(userToken, userOrganizationId)

            if (result.isSuccess) {
                channels = result.getOrNull() ?: emptyList()
            } else {
                errorMessage = result.exceptionOrNull()?.message ?: "Error cargando canales"
            }

            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
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
                    text = "Canales",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            // Estado de carga
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Grid de canales
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 200.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Canales existentes
                items(channels) { channel ->
                    ChannelCard(
                        channel = channel,
                        onEdit = {
                            onEditChannel(channel)
                        },
                        onDelete = {
                            if (channel.acronym != "GEN") {
                                channelToDelete = channel
                                showDeleteDialog = true
                            }
                        }
                    )
                }

                // Botón "Agregar" siempre visible
                item {
                    AddChannelCard(
                        onClick = onNavigateToAddChannel
                    )
                }
            }
        }

        // Mostrar error si existe
        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
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
        }
    }

    if (showDeleteDialog && channelToDelete != null) {
        DeleteChannelDialog(
            channel = channelToDelete!!,
            userToken = userToken,
            onConfirm = {
                // Recargar lista después de eliminar
                loadChannels()
                showDeleteDialog = false
                channelToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                channelToDelete = null
            }
        )
    }
}

@Composable
private fun ChannelCard(
    channel: Channel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Acrónimo del canal
                Text(
                    text = channel.acronym,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Nombre completo
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Email si existe
                if (!channel.email.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = channel.email!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Botones de acción en la esquina superior derecha
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Botón de configuración
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configurar canal",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Botón de eliminar (solo si NO es el canal General)
                if (channel.acronym != "GEN") {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar canal",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddChannelCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ícono de agregar con círculo punteado
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar canal",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Agregar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Composable
fun DeleteChannelDialog(
    channel: Channel,
    userToken: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var isDeleting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Verificar si es el canal General (no se puede eliminar)
    val isGeneralChannel = channel.acronym == "GEN"

    fun deleteChannel() {
        if (isGeneralChannel) return

        coroutineScope.launch {
            isDeleting = true
            errorMessage = null

            try {
                val result = ChannelsApi.instance.deleteChannel(
                    channelId = channel.id,
                    authToken = userToken
                )

                if (result.isSuccess) {
                    onConfirm()
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Error eliminando canal"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error inesperado"
            } finally {
                isDeleting = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        icon = {
            Icon(
                imageVector = if (isGeneralChannel) Icons.Default.Warning else Icons.Default.Delete,
                contentDescription = null,
                tint = if (isGeneralChannel) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = if (isGeneralChannel) "Canal Protegido" else "Eliminar Canal",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isGeneralChannel) {
                    // Mensaje para canal General
                    Text(
                        text = "El canal \"${channel.name}\" es un canal especial del sistema y no puede ser eliminado.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Los canales General son necesarios para el funcionamiento del sistema.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                } else {
                    // Mensaje para canales normales
                    Text(
                        text = "¿Estás seguro que deseas eliminar el canal \"${channel.name}\"?",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Esta acción:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Column(
                        modifier = Modifier.padding(start = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "• Ocultará el canal de la lista",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "• Desuscribirá a todos los usuarios",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "• Los eventos del canal quedarán sin categorizar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Esta acción no se puede deshacer",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Mostrar error si existe
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
        },
        confirmButton = {
            if (isGeneralChannel) {
                // Solo botón de "Entendido" para canal General
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Entendido")
                }
            } else {
                // Botón de confirmar eliminación para canales normales
                Button(
                    onClick = { deleteChannel() },
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isDeleting) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onError
                            )
                            Text("Eliminando...")
                        }
                    } else {
                        Text("Eliminar")
                    }
                }
            }
        },
        dismissButton = {
            if (!isGeneralChannel) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isDeleting
                ) {
                    Text("Cancelar")
                }
            }
        }
    )
}