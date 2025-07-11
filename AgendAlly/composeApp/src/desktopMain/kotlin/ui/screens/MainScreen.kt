package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import auth.DesktopAuth
import auth.TokenResponse
import kotlinx.coroutines.launch
import models.LoginRequest
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
    var selectedScreen by remember { mutableStateOf(NavigationScreen.LOGIN) }


    // Estado del usuario
    var currentUser by remember { mutableStateOf<UserData?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Estado de login
    val isUserLoggedIn = currentUser != null

    val coroutineScope = rememberCoroutineScope()


    var currentUserToken by remember { mutableStateOf<String?>(null) }

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
                            handleRealGoogleSignIn(
                                useRealOAuth = true,
                                scope = coroutineScope,
                                onLoading = { isLoading = it },
                                onError = { error = it },
                                onSuccess = { user, requiresSetup, token ->  // ✅ RECIBIR token
                                    currentUser = user
                                    currentUserToken = token  // ✅ GUARDAR token

                                    println("🔍 LOGIN SUCCESS:")
                                    println("   User: ${user.name}")
                                    println("   Email: ${user.email}")
                                    println("   RequiresSetup: $requiresSetup")
                                    println("   Token saved: ${token?.take(50)}...")

                                    if (requiresSetup) {
                                        selectedScreen = NavigationScreen.ORGANIZATION_SETUP
                                    } else {
                                        selectedScreen = NavigationScreen.CALENDAR
                                    }
                                }
                            )
                        },
                        isLoading = isLoading,
                        error = error
                    )
                }

                NavigationScreen.ORGANIZATION_SETUP -> {
                    OrganizationSetupScreen(
                        userToken = currentUserToken ?: "tempToken",
                        onSetupComplete = {
                            // Setup exitoso, ir al dashboard
                            selectedScreen = NavigationScreen.CALENDAR
                        },
                        onBackToLogin = {
                            // Volver al login
                            selectedScreen = NavigationScreen.LOGIN
                            currentUser = null
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


private fun handleRealGoogleSignIn(
    useRealOAuth: Boolean,
    scope: kotlinx.coroutines.CoroutineScope,
    onLoading: (Boolean) -> Unit,
    onError: (String?) -> Unit,
    onSuccess: (UserData, Boolean, String?) -> Unit  // ✅ AGREGAR token como parámetro
) {
    println("🚀 INICIANDO LOGIN...")
    scope.launch {
        try {
            onLoading(true)
            onError(null)

            println("🔐 Modo OAuth: $useRealOAuth")

            // 🔐 Usar DesktopAuthManager con modo seleccionado
            val authResult = if (useRealOAuth) {
                println("📱 Usando OAuth real...")
                DesktopAuth.instance.signInWithGoogleOAuth()
            } else {
                println("🧪 Usando testing...")
                DesktopAuth.instance.signInWithGoogleTesting()
            }

            println("📊 Auth result type: ${authResult::class.simpleName}")

            when (authResult) {
                is AuthResult.Success -> {
                    println("✅ LOGIN EXITOSO!")
                    println("   User ID: ${authResult.user.id}")
                    println("   Name: ${authResult.user.name}")
                    println("   Requires Setup: ${authResult.requiresOrganizationSetup}")

                    onLoading(false)
                    val uiUserData = UserData(
                        id = authResult.user.id,
                        name = authResult.user.name,
                        email = authResult.user.email,
                        profilePicture = authResult.user.profilePicture,
                        hasOrganization = authResult.user.hasOrganization,
                        organizationName = authResult.user.organizationName
                    )

                    // ✅ OBTENER EL TOKEN del authResult
                    val userToken = authResult.token  // Necesitamos agregar esto a AuthResult

                    onSuccess(uiUserData, authResult.requiresOrganizationSetup, userToken)
                }

                is AuthResult.Error -> {
                    println("❌ LOGIN ERROR: ${authResult.message}")
                    onLoading(false)
                    onError("❌ ${authResult.message}")
                }

                is AuthResult.Loading -> {
                    println("⏳ Still loading...")
                }
            }

        } catch (e: Exception) {
            println("💥 EXCEPTION: ${e.message}")
            e.printStackTrace()
            onLoading(false)
            onError("💥 Error inesperado: ${e.message}")
        }
    }
}