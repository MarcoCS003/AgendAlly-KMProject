// composeApp/src/desktopMain/kotlin/ui/screens/LoginScreen.kt
package ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import agendally.composeapp.generated.resources.Res
import agendally.composeapp.generated.resources.compose_multiplatform
import androidx.compose.material.icons.filled.SingleBed
import ui.components.GoogleSignInButton
import utils.Constants

@Composable
fun LoginScreen(
    onGoogleSignIn: (useRealOAuth: Boolean) -> Unit = {},
    isLoading: Boolean = false,
    error: String? = null,
    onRetry: () -> Unit = {},
    showTestingOptions: Boolean = Constants.Development.IS_DEBUG
) {
    var useRealOAuth by remember { mutableStateOf(!Constants.Development.IS_DEBUG) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(48.dp)
                .widthIn(max = 600.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // Logo y título
                Image(
                    painter = painterResource(Res.drawable.compose_multiplatform),
                    contentDescription = "AgendAlly Logo",
                    modifier = Modifier.size(120.dp)
                )

                Text(
                    text = "AgendAlly",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Sistema de Gestión Académica",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Estado de error
                if (error != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (error.startsWith("✅")) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (error.startsWith("✅")) "Información" else "Error de autenticación",
                                fontWeight = FontWeight.Medium,
                                color = if (error.startsWith("✅")) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onErrorContainer
                                }
                            )
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (error.startsWith("✅")) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onErrorContainer
                                }
                            )

                            // Botón retry solo para errores reales
                            if (!error.startsWith("✅")) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = onRetry) {
                                        Text("Reintentar")
                                    }
                                }
                            }
                        }
                    }
                }

                // ⚠️ NUEVO: Selector de modo de autenticación
                if (showTestingOptions) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "🔧 Modo de Autenticación",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Modo Testing
                                FilterChip(
                                    onClick = { useRealOAuth = false },
                                    label = { Text("🧪 Testing") },
                                    selected = !useRealOAuth,
                                    leadingIcon = if (!useRealOAuth) {
                                        { Icon(Icons.Default.BugReport, null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    modifier = Modifier.weight(1f)
                                )

                                // Modo OAuth Real
                                FilterChip(
                                    onClick = { useRealOAuth = true },
                                    label = { Text("🔐 OAuth Real") },
                                    selected = useRealOAuth,
                                    leadingIcon = if (useRealOAuth) {
                                        { Icon(Icons.Default.SingleBed, null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Descripción del modo actual
                            Text(
                                text = if (useRealOAuth) {
                                    "🌐 Ventana de Google OAuth real"
                                } else {
                                    "📧 Email: ${Constants.Development.TEST_ADMIN_EMAIL}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Botón de login principal
                GoogleSignInButton(
                    onClick = { onGoogleSignIn(useRealOAuth) },
                    isLoading = isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    text = if (useRealOAuth) "Sign in with Google OAuth" else "Sign in (Testing)"
                )

                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = if (useRealOAuth) {
                                "Abriendo ventana de Google..."
                            } else {
                                "Conectando con servidor..."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Información adicional
                Text(
                    text = "Backend: ${Constants.Network.BASE_URL}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}