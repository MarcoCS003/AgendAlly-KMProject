package ui.graphics

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object SocialMediaIcons {

    fun getIcon(socialType: String): ImageVector {
        return when (socialType.lowercase()) {
            "facebook" -> Icons.Default.Facebook
            "instagram" -> Icons.Default.CameraAlt  // Instagram se representa comúnmente con cámara
            "twitter", "x" -> Icons.Default.CheckBox
            "youtube" -> Icons.Default.VideoLibrary
            "linkedin" -> Icons.Default.Business
            "website", "web" -> Icons.Default.Language
            "email" -> Icons.Default.Email
            "phone" -> Icons.Default.Phone
            else -> Icons.Default.Link
        }
    }

    fun getDisplayName(socialType: String): String {
        return when (socialType.lowercase()) {
            "facebook" -> "Facebook"
            "instagram" -> "Instagram"
            "twitter" -> "Twitter/X"
            "youtube" -> "YouTube"
            "linkedin" -> "LinkedIn"
            "website", "web" -> "Sitio Web"
            "email" -> "Email"
            "phone" -> "Teléfono"
            else -> socialType.replaceFirstChar { it.uppercase() }
        }
    }

    fun getPlaceholder(socialType: String): String {
        return when (socialType.lowercase()) {
            "facebook" -> "https://facebook.com/usuario"
            "instagram" -> "https://instagram.com/usuario"
            "twitter" -> "https://x.com/usuario"
            "youtube" -> "https://youtube.com/canal"
            "linkedin" -> "https://linkedin.com/company/empresa"
            "website", "web" -> "https://www.ejemplo.com"
            "email" -> "contacto@ejemplo.com"
            "phone" -> "+52 222 123 4567"
            else -> "https://..."
        }
    }
}