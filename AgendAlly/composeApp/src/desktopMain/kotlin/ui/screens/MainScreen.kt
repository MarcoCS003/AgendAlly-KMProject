package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import repository.AuthRepo
import repository.AuthResult
import ui.components.AgendAllyNavigationRail
import ui.components.NavigationScreen

// Modelo de datos del usuario
data class UserData(
    val id: String,
    val name: String,
    val email: String,
    val profilePicture: String?,
    val hasOrganization: Boolean,
    val organizationName: String?
)

@Composable
fun MainScreen() {
    var selectedScreen by remember { mutableStateOf(NavigationScreen.CONNECTIVITY_TEST) }


    // Estado del usuario
    var currentUser by remember { mutableStateOf<UserData?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Estado de login
    val isUserLoggedIn = currentUser != null

    val coroutineScope = rememberCoroutineScope()

    Row(modifier = Modifier.fillMaxSize()) {
        // Navigation Rail con estado de login
        AgendAllyNavigationRail(
            selectedScreen = selectedScreen,
            onScreenSelected = { screen ->
                when (screen) {
                    NavigationScreen.LOGIN -> {
                        if (isUserLoggedIn) {
                            // Si está logueado, hacer logout
                            currentUser = null
                            error = null
                            selectedScreen = NavigationScreen.LOGIN
                        } else {
                            // Si no está logueado, ir a login
                            selectedScreen = NavigationScreen.LOGIN
                        }
                    }
                    else -> {
                        selectedScreen = screen
                    }
                }
            },
            isUserLoggedIn = isUserLoggedIn,
            userName = currentUser?.name,
            modifier = Modifier.width(120.dp).padding(vertical = 16.dp).clip(RoundedCornerShape(15.dp))
        )

        // Contenido principal
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedScreen) {
                NavigationScreen.CALENDAR -> {
                    if (isUserLoggedIn) {
                        CalendarScreen()
                    } else {
                        LoginRequiredScreen {
                            selectedScreen = NavigationScreen.LOGIN
                        }
                    }
                }
                NavigationScreen.INSTITUTE -> {
                    if (isUserLoggedIn) {
                        InstitutePlaceholderScreen()
                    } else {
                        LoginRequiredScreen {
                            selectedScreen = NavigationScreen.LOGIN
                        }
                    }
                }
                NavigationScreen.SETTINGS -> {
                    if (isUserLoggedIn) {
                        SettingsPlaceholderScreen()
                    } else {
                        LoginRequiredScreen {
                            selectedScreen = NavigationScreen.LOGIN
                        }
                    }
                }
                NavigationScreen.LOGIN -> {
                    LoginScreen(
                        onGoogleSignIn = {
                            // 🚀 NUEVA IMPLEMENTACIÓN: Usar AuthRepository real
                            handleRealGoogleSignIn(
                                scope = coroutineScope,
                                onLoading = { isLoading = it },
                                onError = { error = it },
                                onSuccess = { user, requiresSetup ->
                                    currentUser = user

                                    if (requiresSetup) {
                                        // ⚠️ TODO: Navegar a OrganizationSetupScreen
                                        error = "✅ Login exitoso! Requiere configurar organización"
                                        // Por ahora quedarse en login mostrando el mensaje
                                    } else {
                                        // Usuario existente → ir al dashboard
                                        selectedScreen = NavigationScreen.CALENDAR
                                        error = null
                                    }
                                }
                            )
                        },
                        isLoading = isLoading,
                        error = error,
                        onRetry = {
                            // Limpiar error y reintentar
                            error = null
                        }
                    )
                }
                NavigationScreen.CONNECTIVITY_TEST -> {
                    ConnectivityTestScreen()
                }
            }
        }
    }
}

@Composable
fun LoginRequiredScreen(onLoginClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Inicia sesión para continuar",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Necesitas iniciar sesión para acceder a esta funcionalidad",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Iniciar sesión")
                }
            }
        }
    }
}

@Composable
fun InstitutePlaceholderScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            "Pantalla de Instituto - Próximamente",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun SettingsPlaceholderScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            "Pantalla de Configuración - Próximamente",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

// Función para manejar el login con Google
private fun handleRealGoogleSignIn(
    scope: kotlinx.coroutines.CoroutineScope,
    onLoading: (Boolean) -> Unit,
    onError: (String?) -> Unit,
    onSuccess: (UserData, Boolean) -> Unit
) {
    scope.launch {
        try {
            onLoading(true)
            onError(null)

            // 🔐 Llamada real al AuthRepository
            val authResult = AuthRepo.instance.signInWithGoogle()

            when (authResult) {
                is AuthResult.Success -> {
                    onLoading(false)
                    // ✅ CONVERSIÓN: models.UserData → ui.UserData
                    val uiUserData = UserData(
                        id = authResult.user.id,
                        name = authResult.user.name,
                        email = authResult.user.email,
                        profilePicture = authResult.user.profilePicture,
                        hasOrganization = authResult.user.hasOrganization,
                        organizationName = authResult.user.organizationName
                    )
                    onSuccess(uiUserData, authResult.requiresOrganizationSetup)
                }
                is AuthResult.Error -> {
                    onLoading(false)
                    onError("❌ ${authResult.message}")
                }
                is AuthResult.Loading -> {
                    // Este estado se maneja en el repository
                }
            }

        } catch (e: Exception) {
            onLoading(false)
            onError("💥 Error inesperado: ${e.message}")
        }
    }
}