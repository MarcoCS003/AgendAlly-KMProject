// AgendAlly/server/src/main/kotlin/services/PermissionsService.kt
package services

import com.example.UserRole

/**
 * 🛡️ SERVICIO DE PERMISOS PARA SISTEMA DE EVENTOS
 */
object PermissionsService {

    /**
     * Verificar si un usuario puede crear eventos
     */
    fun canCreateEvents(userRole: UserRole, hasOrganization: Boolean): Boolean {
        return when (userRole) {
            UserRole.SUPER_ADMIN -> true
            UserRole.ADMIN -> hasOrganization
            UserRole.STUDENT -> false
        }
    }

    /**
     * Verificar si un usuario puede editar un evento específico
     */
    fun canEditEvent(
        userRole: UserRole,
        userOrganizationId: Int?,
        eventOrganizationId: Int
    ): Boolean {
        return when (userRole) {
            UserRole.SUPER_ADMIN -> true
            UserRole.ADMIN -> userOrganizationId == eventOrganizationId
            UserRole.STUDENT -> false
        }
    }

    /**
     * Verificar si un usuario puede eliminar un evento específico
     */
    fun canDeleteEvent(
        userRole: UserRole,
        userOrganizationId: Int?,
        eventOrganizationId: Int
    ): Boolean {
        return when (userRole) {
            UserRole.SUPER_ADMIN -> true
            UserRole.ADMIN -> userOrganizationId == eventOrganizationId
            UserRole.STUDENT -> false
        }
    }

    /**
     * Verificar si un usuario puede gestionar archivos/imágenes
     */
    fun canManageFiles(userRole: UserRole): Boolean {
        return when (userRole) {
            UserRole.SUPER_ADMIN, UserRole.ADMIN -> true
            UserRole.STUDENT -> false
        }
    }

    /**
     * Verificar si un usuario puede ver eventos de una organización específica
     */
    fun canViewOrganizationEvents(
        userRole: UserRole,
        userOrganizationId: Int?,
        targetOrganizationId: Int
    ): Boolean {
        return when (userRole) {
            UserRole.SUPER_ADMIN -> true
            UserRole.ADMIN -> userOrganizationId == targetOrganizationId
            UserRole.STUDENT -> true // Los estudiantes pueden ver eventos públicos
        }
    }

    /**
     * Verificar si un usuario puede gestionar canales
     */
    fun canManageChannels(userRole: UserRole, hasOrganization: Boolean): Boolean {
        return when (userRole) {
            UserRole.SUPER_ADMIN -> true
            UserRole.ADMIN -> hasOrganization
            UserRole.STUDENT -> false
        }
    }

    /**
     * Verificar si un usuario puede ver estadísticas avanzadas
     */
    fun canViewAdvancedStats(userRole: UserRole): Boolean {
        return when (userRole) {
            UserRole.SUPER_ADMIN, UserRole.ADMIN -> true
            UserRole.STUDENT -> false
        }
    }

    /**
     * Obtener límites de eventos según el rol
     */
    fun getEventLimits(userRole: UserRole): EventLimits {
        return when (userRole) {
            UserRole.SUPER_ADMIN -> EventLimits(
                maxEventsPerMonth = -1, // Sin límite
                maxImageSize = 10 * 1024 * 1024, // 10MB
                canCreateRecurringEvents = true,
                canScheduleFutureEvents = true
            )
            UserRole.ADMIN -> EventLimits(
                maxEventsPerMonth = 50,
                maxImageSize = 5 * 1024 * 1024, // 5MB
                canCreateRecurringEvents = true,
                canScheduleFutureEvents = true
            )
            UserRole.STUDENT -> EventLimits(
                maxEventsPerMonth = 0,
                maxImageSize = 0,
                canCreateRecurringEvents = false,
                canScheduleFutureEvents = false
            )
        }
    }
}

/**
 * 📋 Límites de eventos por rol
 */
data class EventLimits(
    val maxEventsPerMonth: Int, // -1 = sin límite
    val maxImageSize: Int,
    val canCreateRecurringEvents: Boolean,
    val canScheduleFutureEvents: Boolean
)

/**
 * 🎯 Resultado de verificación de permisos
 */
sealed class PermissionResult {
    object Allowed : PermissionResult()
    data class Denied(val reason: String) : PermissionResult()
}

/**
 * 🛡️ EXTENSIONES PARA AuthResult
 */
fun AuthResult.hasPermission(permission: Permission): PermissionResult {
    return when (permission) {
        is Permission.CreateEvent -> {
            if (PermissionsService.canCreateEvents(this.user.role, this.user.organizationId != null)) {
                PermissionResult.Allowed
            } else {
                PermissionResult.Denied("No tienes permisos para crear eventos")
            }
        }
        is Permission.EditEvent -> {
            if (PermissionsService.canEditEvent(this.user.role, this.user.organizationId, permission.eventOrganizationId)) {
                PermissionResult.Allowed
            } else {
                PermissionResult.Denied("Solo puedes editar eventos de tu organización")
            }
        }
        is Permission.DeleteEvent -> {
            if (PermissionsService.canDeleteEvent(this.user.role, this.user.organizationId, permission.eventOrganizationId)) {
                PermissionResult.Allowed
            } else {
                PermissionResult.Denied("Solo puedes eliminar eventos de tu organización")
            }
        }
        is Permission.ManageFiles -> {
            if (PermissionsService.canManageFiles(this.user.role)) {
                PermissionResult.Allowed
            } else {
                PermissionResult.Denied("No tienes permisos para gestionar archivos")
            }
        }
    }
}

/**
 * 🎯 Tipos de permisos
 */
sealed class Permission {
    object CreateEvent : Permission()
    data class EditEvent(val eventOrganizationId: Int) : Permission()
    data class DeleteEvent(val eventOrganizationId: Int) : Permission()
    object ManageFiles : Permission()
}
