package services

import com.example.LoginResponse
import com.example.User
import com.example.UserResponse

/**
 * Adaptador para convertir entre modelos internos y modelos de API
 */
object AuthAdapter {

    private fun convertToUserResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id,
            firebaseUid = user.googleId,
            email = user.email,
            name = user.name,
            profilePicture = user.profilePicture,
            role = user.role.name,
            organizationId = user.organizationId, // ✅ CAMBIAR: usar organizationId real
            createdAt = user.createdAt,
            lastLoginAt = user.lastLoginAt
        )
    }

    fun convertToLoginResponse(authResult: AuthResult): LoginResponse {
        val user = convertToUserResponse(authResult.user)

        return LoginResponse(
            success = true,
            user = user,
            organization = null,
            requiresOrganizationSetup = user.organizationId == null, // ✅ CAMBIAR: solo requiere setup si no tiene org
            message = if (user.organizationId == null) "Debe configurar su organización" else "Login exitoso"
        )
    }
}

