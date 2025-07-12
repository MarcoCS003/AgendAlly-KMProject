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
    val description: String = "",
    val address: String,
    val email: String,
    val phone: String,
    val studentNumber: Int = 0,
    val teacherNumber: Int = 0,
    val webSite: String? = null,
    val facebook: String? = null,
    val instagram: String? = null,
    val twitter: String? = null,
    val youtube: String? = null,
    val linkedin: String? = null
)

@Serializable
data class UpdateOrganizationRequest(
    val name: String,
    val acronym: String,
    val description: String = "",
    val address: String,
    val email: String,
    val phone: String,
    val studentNumber: Int = 0,
    val teacherNumber: Int = 0,
    val logoUrl: String? = null,
    val webSite: String? = null,
    val facebook: String? = null,
    val instagram: String? = null,
    val twitter: String? = null,
    val youtube: String? = null,
    val linkedin: String? = null
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
@Serializable
data class Organization(
    val organizationID: Int,
    val acronym: String,
    val name: String,
    val description: String = "",
    val address: String,
    val email: String,
    val phone: String,
    val studentNumber: Int,
    val teacherNumber: Int,
    val logoUrl: String? = null,
    val webSite: String? = null,
    val facebook: String? = null,
    val instagram: String? = null,
    val twitter: String? = null,
    val youtube: String? = null,
    val linkedin: String? = null,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
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