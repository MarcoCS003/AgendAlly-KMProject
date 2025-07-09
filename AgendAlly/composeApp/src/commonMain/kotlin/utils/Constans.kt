package utils

object Constants {

    // ============== NETWORK ==============

    object Network {
        const val BASE_URL = "http://localhost:8080"
        const val TIMEOUT_MILLIS = 30_000L
        const val CONNECT_TIMEOUT_MILLIS = 15_000L
        const val READ_TIMEOUT_MILLIS = 30_000L
    }

    // ============== AUTH ==============

    object Auth {
        const val TOKEN_KEY = "auth_token"
        const val USER_KEY = "user_data"
        const val REFRESH_TOKEN_KEY = "refresh_token"
        const val TOKEN_EXPIRY_KEY = "token_expiry"

        // Tiempo de vida del token en milisegundos (24 horas)
        const val TOKEN_LIFETIME_MS = 24 * 60 * 60 * 1000L

        // Tiempo antes de expiración para renovar (2 horas)
        const val TOKEN_REFRESH_THRESHOLD_MS = 2 * 60 * 60 * 1000L
    }

    // ============== UI ==============

    object UI {
        const val LOADING_DELAY_MS = 500L
        const val ERROR_DISPLAY_DURATION_MS = 5000L
        const val SUCCESS_DISPLAY_DURATION_MS = 3000L

        const val ANIMATION_DURATION_MS = 300
        const val SPLASH_DURATION_MS = 2000L
    }

    // ============== GOOGLE OAUTH ==============

    object GoogleOAuth {
        const val CLIENT_ID = ""
        const val CLIENT_SECRET =  " "
        const val REDIRECT_URI = "http://localhost:8888/callback"
        const val SCOPE = "openid email profile"

        // Puerto para servidor local de callback
        const val CALLBACK_PORT = 8888
        const val CALLBACK_PATH = "/callback"

        // URLs de Google OAuth
        const val AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth"
        const val TOKEN_URL = "https://oauth2.googleapis.com/token"
        const val USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo"
    }

    // ============== VALIDATION ==============

    object Validation {
        const val MIN_PASSWORD_LENGTH = 8
        const val MAX_EMAIL_LENGTH = 100
        const val MAX_NAME_LENGTH = 50
        const val MAX_ORGANIZATION_NAME_LENGTH = 100
        const val MAX_ACRONYM_LENGTH = 10

        // Regex patterns
        const val EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        const val ACRONYM_PATTERN = "^[A-Z0-9]{2,10}$"
    }

    // ============== ERROR CODES ==============

    object ErrorCodes {
        const val NETWORK_ERROR = "NETWORK_ERROR"
        const val AUTH_ERROR = "AUTH_ERROR"
        const val VALIDATION_ERROR = "VALIDATION_ERROR"
        const val SERVER_ERROR = "SERVER_ERROR"
        const val UNKNOWN_ERROR = "UNKNOWN_ERROR"

        const val TOKEN_EXPIRED = "TOKEN_EXPIRED"
        const val INVALID_CREDENTIALS = "INVALID_CREDENTIALS"
        const val USER_NOT_FOUND = "USER_NOT_FOUND"
        const val ORGANIZATION_REQUIRED = "ORGANIZATION_REQUIRED"
    }

    // ============== APP INFO ==============

    object App {
        const val NAME = "AgendAlly"
        const val VERSION = "1.0.0"
        const val DESCRIPTION = "Sistema de gestión académica"

        const val SUPPORT_EMAIL = "support@agendally.com"
        const val PRIVACY_URL = "https://agendally.com/privacy"
        const val TERMS_URL = "https://agendally.com/terms"
    }

    // ============== DEVELOPMENT ==============

    object Development {
        const val IS_DEBUG = true
        const val ENABLE_LOGGING = true
        const val MOCK_DELAY_MS = 1000L

        // Usuarios de prueba
        const val TEST_ADMIN_EMAIL = "admin@itp.edu.mx"
        const val TEST_NEW_USER_EMAIL = "nuevo@universidad.com"
    }
}