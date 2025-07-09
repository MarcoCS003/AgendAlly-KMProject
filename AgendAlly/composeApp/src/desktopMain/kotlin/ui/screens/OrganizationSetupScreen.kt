// composeApp/src/commonMain/kotlin/ui/screens/OrganizationSetupScreen.kt
package ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import repository.AuthRepo
import repository.AuthResult
import utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationSetupScreen(
    onSetupComplete: () -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados del formulario - INFORMACIÓN BÁSICA
    var organizationName by remember { mutableStateOf("") }
    var acronym by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Estados del formulario - NÚMEROS
    var studentNumber by remember { mutableStateOf("") }
    var teacherNumber by remember { mutableStateOf("") }

    // Estados del formulario - REDES SOCIALES (OPCIONALES)
    var website by remember { mutableStateOf("") }
    var facebook by remember { mutableStateOf("") }
    var instagram by remember { mutableStateOf("") }
    var twitter by remember { mutableStateOf("") }
    var youtube by remember { mutableStateOf("") }
    var linkedin by remember { mutableStateOf("") }

    // Estados de UI
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentStep by remember { mutableStateOf(0) } // 0: Básico, 1: Números, 2: Redes

    // Estados de validación
    var nameError by remember { mutableStateOf<String?>(null) }
    var acronymError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val authRepository = AuthRepo.instance
    val scrollState = rememberScrollState()

    // Función de validación
    fun validateStep1(): Boolean {
        var isValid = true

        nameError = when {
            organizationName.isBlank() -> { isValid = false; "El nombre es obligatorio" }
            organizationName.length < 3 -> { isValid = false; "Mínimo 3 caracteres" }
            else -> null
        }

        acronymError = when {
            acronym.isBlank() -> { isValid = false; "El acrónimo es obligatorio" }
            acronym.length > 10 -> { isValid = false; "Máximo 10 caracteres" }
            !acronym.matches(Regex("^[A-Z0-9]+$")) -> { isValid = false; "Solo mayúsculas y números" }
            else -> null
        }

        return isValid
    }

    // Función de validación completa - SOLO PARA ENVÍO FINAL
    fun validateAllForm(): Boolean {
        var isValid = validateStep1()

        addressError = when {
            address.isBlank() -> { isValid = false; "La dirección es obligatoria" }
            address.length < 10 -> { isValid = false; "Dirección muy corta" }
            else -> null
        }

        emailError = when {
            email.isBlank() -> { isValid = false; "El email es obligatorio" }
            !email.matches(Regex(Constants.Validation.EMAIL_PATTERN)) -> { isValid = false; "Email inválido" }
            else -> null
        }

        phoneError = when {
            phone.isBlank() -> { isValid = false; "El teléfono es obligatorio" }
            phone.length < 10 -> { isValid = false; "Teléfono debe tener al menos 10 dígitos" }
            else -> null
        }

        return isValid
    }

    // Función para enviar formulario
    fun submitForm() {
        if (!validateAllForm()) return

        coroutineScope.launch {
            isLoading = true
            errorMessage = null

            val result = authRepository.setupOrganization(
                name = organizationName.trim(),
                acronym = acronym.trim().uppercase(),
                description = description.trim(),
                address = address.trim(),
                email = email.trim(),
                phone = phone.trim(),
                studentNumber = studentNumber.toIntOrNull() ?: 0,
                teacherNumber = teacherNumber.toIntOrNull() ?: 0,
                website = website.trim().takeIf { it.isNotBlank() },
                facebook = facebook.trim().takeIf { it.isNotBlank() },
                instagram = instagram.trim().takeIf { it.isNotBlank() },
                twitter = twitter.trim().takeIf { it.isNotBlank() },
                youtube = youtube.trim().takeIf { it.isNotBlank() },
                linkedin = linkedin.trim().takeIf { it.isNotBlank() }
            )

            isLoading = false

            when (result) {
                is AuthResult.Success -> onSetupComplete()
                is AuthResult.Error -> errorMessage = result.message
                else -> errorMessage = "Error inesperado"
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Configurar Organización",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Proporciona la información completa de tu organización",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Indicador de pasos
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { step ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .run {
                                if (step <= currentStep) {
                                    this.then(
                                        Modifier.background(
                                            MaterialTheme.colorScheme.primary,
                                            androidx.compose.foundation.shape.CircleShape
                                        )
                                    )
                                } else {
                                    this.then(
                                        Modifier.background(
                                            MaterialTheme.colorScheme.outline,
                                            androidx.compose.foundation.shape.CircleShape
                                        )
                                    )
                                }
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Formulario por pasos
            when (currentStep) {
                0 -> {
                    // PASO 1: INFORMACIÓN BÁSICA
                    Text(
                        text = "Información Básica",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = organizationName,
                            onValueChange = { organizationName = it; nameError = null },
                            label = { Text("Nombre de la Organización *") },
                            placeholder = { Text("Instituto Tecnológico de Puebla") },
                            leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                            isError = nameError != null,
                            supportingText = nameError?.let { { Text(it) } },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = acronym,
                            onValueChange = {
                                acronym = it.uppercase().filter { char -> char.isLetterOrDigit() }
                                acronymError = null
                            },
                            label = { Text("Acrónimo *") },
                            placeholder = { Text("ITP") },
                            leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                            isError = acronymError != null,
                            supportingText = acronymError?.let { { Text(it) } },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Descripción") },
                            placeholder = { Text("Institución educativa de excelencia...") },
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                            enabled = !isLoading,
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                1 -> {
                    // PASO 2: CONTACTO Y UBICACIÓN
                    Text(
                        text = "Contacto y Ubicación",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it; addressError = null },
                            label = { Text("Dirección *") },
                            placeholder = { Text("Av. Tecnológico 420, Puebla, México") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            isError = addressError != null,
                            supportingText = addressError?.let { { Text(it) } },
                            enabled = !isLoading,
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; emailError = null },
                            label = { Text("Email Institucional *") },
                            placeholder = { Text("contacto@itp.edu.mx") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            isError = emailError != null,
                            supportingText = emailError?.let { { Text(it) } },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it; phoneError = null },
                            label = { Text("Teléfono *") },
                            placeholder = { Text("+52 222 123 4567") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            isError = phoneError != null,
                            supportingText = phoneError?.let { { Text(it) } },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = studentNumber,
                                onValueChange = { studentNumber = it.filter { char -> char.isDigit() } },
                                label = { Text("Estudiantes") },
                                placeholder = { Text("5000") },
                                leadingIcon = { Icon(Icons.Default.People, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                enabled = !isLoading,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = teacherNumber,
                                onValueChange = { teacherNumber = it.filter { char -> char.isDigit() } },
                                label = { Text("Profesores") },
                                placeholder = { Text("200") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                enabled = !isLoading,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                2 -> {
                    // PASO 3: REDES SOCIALES Y WEB
                    Text(
                        text = "Presencia Digital (Opcional)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = website,
                            onValueChange = { website = it },
                            label = { Text("Sitio Web") },
                            placeholder = { Text("https://itp.edu.mx") },
                            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = facebook,
                            onValueChange = { facebook = it },
                            label = { Text("Facebook") },
                            placeholder = { Text("https://facebook.com/ITP") },
                            leadingIcon = { Icon(Icons.Default.Facebook, contentDescription = null) },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = instagram,
                            onValueChange = { instagram = it },
                            label = { Text("Instagram") },
                            placeholder = { Text("@itp_oficial") },
                            leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = twitter,
                                onValueChange = { twitter = it },
                                label = { Text("Twitter/X") },
                                placeholder = { Text("@ITP_Oficial") },
                                enabled = !isLoading,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = youtube,
                                onValueChange = { youtube = it },
                                label = { Text("YouTube") },
                                placeholder = { Text("ITP Oficial") },
                                enabled = !isLoading,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = linkedin,
                            onValueChange = { linkedin = it },
                            label = { Text("LinkedIn") },
                            placeholder = { Text("instituto-tecnologico-puebla") },
                            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Error message
            errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botones de navegación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (currentStep) {
                    0 -> {
                        OutlinedButton(
                            onClick = onBackToLogin,
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancelar")
                        }

                        Button(
                            onClick = {
                                if (validateStep1()) {
                                    currentStep = 1
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Siguiente")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }

                    1 -> {
                        OutlinedButton(
                            onClick = { currentStep = 0 },
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Anterior")
                        }

                        Button(
                            onClick = { currentStep = 2 },
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Siguiente")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }

                    2 -> {
                        OutlinedButton(
                            onClick = { currentStep = 1 },
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Anterior")
                        }

                        Button(
                            onClick = { submitForm() },
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isLoading) "Creando..." else "Crear")
                        }
                    }
                }
            }
        }
    }
}