package network

import io.ktor.client.*
import utils.Constants

/**
 * Configuración específica del HTTP Client para Desktop
 */

/*
object DesktopHttpConfig {

    /**
     * Engine específico para Desktop con timeouts configurados
     */
    val desktopEngine = CIO.create {
        // ⏱️ Timeouts específicos para desktop
        requestTimeout = Constants.Network.TIMEOUT_MILLIS

        // 🔧 Configuración del engine CIO
        endpoint {
            connectTimeout = Constants.Network.CONNECT_TIMEOUT_MILLIS
            requestTimeout = Constants.Network.READ_TIMEOUT_MILLIS

            // Configuración de conexiones
            maxConnectionsCount = 10

            // Keep-alive
            keepAliveTime = 5000

            // Pipeline
            pipelineMaxSize = 20
        }

        // 🛡️ Configuración HTTPS/TLS (para futuro)
        https {
            // Configuración SSL cuando sea necesaria
        }
    }

    /**
     * Cliente HTTP específico para Desktop
     */
    val client = HttpClient(desktopEngine) {
        // Usar la configuración base común
        HttpClientConfig.client.config.let { baseConfig ->
            // Aplicar configuración base
            install(baseConfig.plugins.entries.first().key) {
                // Configuración específica si es necesaria
            }
        }
    }
}*/