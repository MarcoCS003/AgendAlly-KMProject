// composeApp/src/desktopMain/kotlin/ui/screens/BlogScreen.kt
package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import models.*
import network.BlogApi
import network.ChannelsApi
import ui.components.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun BlogScreen(
    userToken: String,
    userOrganizationId: Int,
    modifier: Modifier = Modifier
) {
    // Estados principales
    var events by remember { mutableStateOf<List<BlogEvent>>(emptyList()) }
    var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
    var selectedChannel by remember { mutableStateOf(ChannelOption.ALL_CHANNELS) }
    var selectedEvent by remember { mutableStateOf<BlogEvent?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Estados para diálogos
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<BlogEvent?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    // Función para cargar canales
    fun loadChannels() {
        coroutineScope.launch {
            val result = ChannelsApi.instance.getMyChannels(userToken, userOrganizationId)
            if (result.isSuccess) {
                channels = result.getOrNull() ?: emptyList()
            }
        }
    }

    // Función para cargar eventos
    fun loadEvents() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null

            val result = when {
                selectedChannel.isAll -> BlogApi.instance.getEventsByOrganization(userOrganizationId)
                else -> BlogApi.instance.getEventsByChannel(selectedChannel.id)
            }

            if (result.isSuccess) {
                events = result.getOrNull()?.events ?: emptyList()
                // Si hay eventos y no hay uno seleccionado, seleccionar el primero
                if (events.isNotEmpty() && selectedEvent == null) {
                    selectedEvent = events.first()
                }
            } else {
                errorMessage = result.exceptionOrNull()?.message ?: "Error cargando eventos"
            }

            isLoading = false
        }
    }

    // Cargar datos iniciales
    LaunchedEffect(userToken) {
        loadChannels()
        loadEvents()
    }

    // Cargar cuando cambia el canal seleccionado
    LaunchedEffect(selectedChannel) {
        selectedEvent = null // Limpiar selección
        loadEvents()
    }

    Row(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 📱 LADO IZQUIERDO - Lista de eventos
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header con selector de canal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Blog ${selectedChannel.acronym}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        ChannelSelector(
                            channels = channels,
                            selectedChannel = selectedChannel,
                            onChannelSelected = { selectedChannel = it }
                        )
                    }

                    Divider()

                    // Lista de eventos
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (errorMessage != null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorMessage!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { loadEvents() }) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    } else if (events.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No hay eventos",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Crea el primer evento para ${selectedChannel.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(events) { event ->
                                BlogEventCard(
                                    event = event,
                                    isSelected = selectedEvent?.id == event.id,
                                    onClick = { selectedEvent = event },
                                    onEdit = {
                                        eventToEdit = event
                                        showEditDialog = true
                                    },
                                    onDelete = {
                                        loadEvents()
                                        if (selectedEvent?.id == event.id) {
                                            selectedEvent = null
                                        }
                                    },
                                    userToken = userToken
                                )
                            }
                        }
                    }
                }

                // FloatingActionButton para crear evento
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Crear evento"
                    )
                }
            }
        }

        // 👁️ LADO DERECHO - Visor de evento
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            if (selectedEvent != null) {
                EventDetailViewer(
                    event = selectedEvent!!,
                    channels = channels
                )
            } else {
                // Placeholder cuando no hay evento seleccionado
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Preview,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Selecciona un evento",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Haz clic en un evento para ver los detalles completos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Diálogos
    if (showCreateDialog) {
        CreateEventDialog(
            channels = channels,
            userToken = userToken,
            userOrganizationId = userOrganizationId,
            onSuccess = { message ->
                loadEvents()
                showCreateDialog = false
                successMessage = message
                showSuccessDialog = true
            },
            onDismiss = {
                showCreateDialog = false
            }
        )
    }

    if (showEditDialog && eventToEdit != null) {
        EditEventDialog(
            event = eventToEdit!!,
            channels = channels,
            userToken = userToken,
            onSuccess = { message ->
                loadEvents()
                showEditDialog = false
                eventToEdit = null
                successMessage = message
                showSuccessDialog = true
            },
            onDismiss = {
                showEditDialog = false
                eventToEdit = null
            }
        )
    }

    if (showSuccessDialog) {
        SuccessDialog(
            message = successMessage,
            onDismiss = {
                showSuccessDialog = false
                successMessage = ""
            }
        )
    }
}