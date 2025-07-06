package services

import com.example.LoginResponse
import com.example.User
import com.example.UserResponse

/**
 * Adaptador para convertir entre modelos internos y modelos de API
 */
object AuthAdapter {

    fun convertToLoginResponse(authResult: AuthResult): LoginResponse {
        val user = convertToUserResponse(authResult.user)

        return LoginResponse(
            success = true,
            user = user,
            organization = null, // Siempre null para MVP
            requiresOrganizationSetup = true, // Siempre requiere setup
            message = "Debe configurar su organización"
        )
    }

    private fun convertToUserResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id,
            firebaseUid = user.googleId, // Usar googleId como firebaseUid
            email = user.email,
            name = user.name,
            profilePicture = user.profilePicture,
            role = user.role.name,
            organizationId = null, // Siempre null para MVP
            createdAt = user.createdAt,
            lastLoginAt = user.lastLoginAt
        )
    }
}