package models

import kotlinx.serialization.Serializable

// ============== REQUEST MODELS ==============

@Serializable
data class LoginRequest(
    val email: String = "",
    val idToken: String = "",
    val clientType: String = "DESKTOP_ADMIN"
)

@Serializable
data class OrganizationSetupRequest(
    val name: String,
    val acronym: String,
    val contactEmail: String
)

// ============== RESPONSE MODELS ==============

@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String? = null,
    val user: UserResponse? = null,
    val requiresOrganizationSetup: Boolean = false,
    val jwtToken: String? = null
)

@Serializable
data class UserResponse(
    val id: Int,
    val firebaseUid: String? = null,
    val email: String,
    val name: String,
    val profilePicture: String? = null,
    val role: String,
    val organizationId: Int? = null,
    val createdAt: String,
    val lastLoginAt: String
)

@Serializable
data class OrganizationSetupResponse(
    val success: Boolean,
    val message: String,
    val organizationId: Int? = null,
    val user: UserResponse? = null
)

@Serializable
data class StatusResponse(
    val firebase_initialized: Boolean,
    val environment: String,
    val auth_flow: String,
    val client_types: List<String>
)

@Serializable
data class ErrorResponse(
    val success: Boolean = false,
    val error: String,
    val message: String? = null
)

// ============== UI DATA MODELS ==============

data class UserData(
    val id: String,
    val name: String,
    val email: String,
    val profilePicture: String?,
    val hasOrganization: Boolean,
    val organizationName: String?
)

// Corregir esto no se para que

/**
 * Convierte UserResponse del API a UserData del UI
 */
fun UserResponse.toUserData(organizationName: String? = null): UserData {
    return UserData(
        id = id.toString(),
        name = name,
        email = email,
        profilePicture = profilePicture,
        hasOrganization = organizationId != null,
        organizationName = organizationName
    )
}