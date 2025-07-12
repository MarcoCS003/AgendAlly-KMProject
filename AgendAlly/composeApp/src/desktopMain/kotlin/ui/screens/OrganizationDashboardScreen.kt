// AgendAlly/composeApp/src/desktopMain/kotlin/ui/screens/OrganizationDashboardScreen.kt
package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import models.Organization
import network.AuthApi

@Composable
fun OrganizationDashboardScreen(
    userToken: String,
    modifier: Modifier = Modifier
) {
    // Estados principales
    var organization by remember { mutableStateOf<Organization?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isEditMode by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Estados del formulario
    var editName by remember { mutableStateOf("") }
    var editAcronym by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editAddress by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editWebsite by remember { mutableStateOf("") }
    var editFacebook by remember { mutableStateOf("") }
    var editInstagram by remember { mutableStateOf("") }
    var editTwitter by remember { mutableStateOf("") }
    var editYoutube by remember { mutableStateOf("") }
    var editLinkedin by remember { mutableStateOf("") }

    // Estados para diálogos de redes sociales
    var showSocialDialog by remember { mutableStateOf(false) }
    var socialDialogType by remember { mutableStateOf("") }
    var socialDialogTitle by remember { mutableStateOf("") }
    var socialDialogValue by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Cargar organización al iniciar
    LaunchedEffect(userToken) {
        println("🔍 DEBUG - Cargando organización...")
        println("   Token: ${userToken.take(50)}...")

        isLoading = true
        errorMessage = null

        val result = AuthApi.instance.getMyOrganization(userToken)

        if (result.isSuccess) {
            organization = result.getOrNull()

            // 🔍 DEBUG: Mostrar datos recibidos
            organization?.let { org ->
                println("✅ Organización cargada:")
                println("   ID: ${org.organizationID}")
                println("   Nombre: ${org.name}")
                println("   Acrónimo: ${org.acronym}")
                println("   Email: ${org.email}")

                // Inicializar campos...
            }
        } else {
            println("❌ Error cargando organización: ${result.exceptionOrNull()?.message}")
            errorMessage = result.exceptionOrNull()?.message ?: "Error desconocido"
        }

        isLoading = false
    }

    // Función para cargar organización
    fun loadOrganization() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null

            val result = AuthApi.instance.getMyOrganization(userToken)

            if (result.isSuccess) {
                organization = result.getOrNull()
                organization?.let { org ->
                    // Inicializar campos de edición
                    editName = org.name
                    editAcronym = org.acronym
                    editDescription = org.description
                    editAddress = org.address
                    editEmail = org.email
                    editPhone = org.phone
                    editWebsite = org.webSite ?: ""
                    editFacebook = org.facebook ?: ""
                    editInstagram = org.instagram ?: ""
                    editTwitter = org.twitter ?: ""
                    editYoutube = org.youtube ?: ""
                    editLinkedin = org.linkedin ?: ""
                }
            } else {
                errorMessage = result.exceptionOrNull()?.message ?: "Error desconocido"
            }

            isLoading = false
        }
    }

    // Función para guardar cambios
    fun saveChanges() {
        coroutineScope.launch {
            isSaving = true
            errorMessage = null

            val result = AuthApi.instance.updateMyOrganization(
                name = editName,
                acronym = editAcronym,
                description = editDescription,
                address = editAddress,
                email = editEmail,
                phone = editPhone,
                website = editWebsite.ifBlank { null },
                facebook = editFacebook.ifBlank { null },
                instagram = editInstagram.ifBlank { null },
                twitter = editTwitter.ifBlank { null },
                youtube = editYoutube.ifBlank { null },
                linkedin = editLinkedin.ifBlank { null },
                authToken = userToken
            )

            if (result.isSuccess) {
                organization = result.getOrNull()
                isEditMode = false
            } else {
                errorMessage = result.exceptionOrNull()?.message ?: "Error guardando cambios"
            }

            isSaving = false
        }
    }

    // Función para abrir diálogo de red social
    fun openSocialDialog(type: String, title: String, currentValue: String) {
        socialDialogType = type
        socialDialogTitle = title
        socialDialogValue = currentValue
        showSocialDialog = true
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState)
        ) {
            // Header con título y logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Perfil de la Organización",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                // Logo de la organización
                Card(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        if (organization?.logoUrl != null) {
                            // TODO: Cargar imagen del logo
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = "Logo por defecto",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                // Estado de carga
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (organization != null) {
                // Contenido principal
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

                    // Información básica
                    InformationSection(
                        title = "Información Básica",
                        isEditMode = isEditMode,
                        content = {
                            if (isEditMode) {
                                // Modo edición
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        OutlinedTextField(
                                            value = editName,
                                            onValueChange = { editName = it },
                                            label = { Text("Nombre") },
                                            modifier = Modifier.weight(2f)
                                        )
                                        OutlinedTextField(
                                            value = editAcronym,
                                            onValueChange = { editAcronym = it.uppercase() },
                                            label = { Text("Nombre Corto") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    OutlinedTextField(
                                        value = editDescription,
                                        onValueChange = { editDescription = it },
                                        label = { Text("Descripción") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 2
                                    )

                                    OutlinedTextField(
                                        value = editAddress,
                                        onValueChange = { editAddress = it },
                                        label = { Text("Dirección") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 2
                                    )

                                    OutlinedTextField(
                                        value = editWebsite,
                                        onValueChange = { editWebsite = it },
                                        label = { Text("Sitio Web") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = editPhone,
                                        onValueChange = { editPhone = it },
                                        label = { Text("Teléfono") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = editEmail,
                                        onValueChange = { editEmail = it },
                                        label = { Text("Email de contacto") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            } else {
                                // Modo vista
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        InfoField(
                                            label = "Nombre",
                                            value = organization!!.name,
                                            modifier = Modifier.weight(2f)
                                        )
                                        InfoField(
                                            label = "Nombre Corto",
                                            value = organization!!.acronym,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    if (organization!!.description.isNotBlank()) {
                                        InfoField(
                                            label = "Descripción",
                                            value = organization!!.description
                                        )
                                    }

                                    InfoField(
                                        label = "Dirección",
                                        value = organization!!.address
                                    )

                                    if (organization!!.webSite?.isNotBlank() == true) {
                                        InfoField(
                                            label = "Sitio Web",
                                            value = organization!!.webSite!!,
                                            isUrl = true
                                        )
                                    }

                                    InfoField(
                                        label = "Teléfono",
                                        value = organization!!.phone
                                    )
                                }
                            }
                        }
                    )

                    // Redes sociales
                    InformationSection(
                        title = "Vincular redes",
                        isEditMode = isEditMode,
                        content = {
                            SocialMediaSection(
                                organization = organization!!,
                                isEditMode = isEditMode,
                                onSocialClick = { type, title, value ->
                                    if (isEditMode) {
                                        openSocialDialog(type, title, value)
                                    } else {
                                        // TODO: Abrir URL en navegador
                                    }
                                }
                            )
                        }
                    )
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

        // Floating Action Button
        if (!isEditMode) {
            FloatingActionButton(
                onClick = {
                    // Inicializar campos de edición antes de entrar al modo edición
                    organization?.let { org ->
                        editName = org.name
                        editAcronym = org.acronym
                        editDescription = org.description
                        editAddress = org.address
                        editEmail = org.email
                        editPhone = org.phone
                        editWebsite = org.webSite ?: ""
                        editFacebook = org.facebook ?: ""
                        editInstagram = org.instagram ?: ""
                        editTwitter = org.twitter ?: ""
                        editYoutube = org.youtube ?: ""
                        editLinkedin = org.linkedin ?: ""
                    }
                    isEditMode = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar"
                )
            }
        } else {
            // Botones de guardar/cancelar en modo edición
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        isEditMode = false
                        loadOrganization() // Recargar datos originales
                    },
                    enabled = !isSaving
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cancelar")
                }

                Button(
                    onClick = { saveChanges() },
                    enabled = !isSaving && editName.isNotBlank() && editAcronym.isNotBlank()
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar Cambios")
                }
            }
        }
    }

    // Diálogo para editar redes sociales
    if (showSocialDialog) {
        SocialMediaDialog(
            title = socialDialogTitle,
            value = socialDialogValue,
            onValueChange = { socialDialogValue = it },
            onSave = {
                // Actualizar el campo correspondiente
                when (socialDialogType) {
                    "facebook" -> editFacebook = socialDialogValue
                    "instagram" -> editInstagram = socialDialogValue
                    "twitter" -> editTwitter = socialDialogValue
                    "youtube" -> editYoutube = socialDialogValue
                    "linkedin" -> editLinkedin = socialDialogValue
                }
                showSocialDialog = false
            },
            onDismiss = { showSocialDialog = false }
        )
    }
}

@Composable
private fun InformationSection(
    title: String,
    isEditMode: Boolean,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

@Composable
private fun InfoField(
    label: String,
    value: String,
    isUrl: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (isUrl) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { /* TODO: Abrir URL */ }
                    .padding(vertical = 4.dp)
            )
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SocialMediaSection(
    organization: Organization,
    isEditMode: Boolean,
    onSocialClick: (type: String, title: String, value: String) -> Unit
) {
    val socialMediaList = listOf(
        Triple("facebook", Icons.Default.Facebook, organization.facebook),
        Triple("instagram", Icons.Default.CameraAlt, organization.instagram),
        Triple("twitter", Icons.Default.CheckBox, organization.twitter),
        Triple("youtube", Icons.Default.VideoLibrary, organization.youtube)
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        socialMediaList.forEach { (type, icon, url) ->
            SocialMediaIcon(
                icon = icon,
                isLinked = !url.isNullOrBlank(),
                isEditMode = isEditMode,
                onClick = {
                    onSocialClick(type, type.capitalize(), url ?: "")
                }
            )
        }
    }
}

@Composable
private fun SocialMediaIcon(
    icon: ImageVector,
    isLinked: Boolean,
    isEditMode: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isLinked) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    val iconColor = if (isLinked) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() }
            .border(
                width = if (isEditMode) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun SocialMediaDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Editar $title",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text("Colocar url") },
                    placeholder = { Text("https://...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = when (title.lowercase()) {
                                "facebook" -> Icons.Default.Facebook
                                "instagram" -> Icons.Default.CameraAlt
                                "twitter" -> Icons.Default.CheckBox
                                "youtube" -> Icons.Default.VideoLibrary
                                "linkedin" -> Icons.Default.Business
                                else -> Icons.Default.Link
                            },
                            contentDescription = null
                        )
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(onClick = onSave) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

// ================================
// EXTENSIONES ÚTILES
// ================================

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}
