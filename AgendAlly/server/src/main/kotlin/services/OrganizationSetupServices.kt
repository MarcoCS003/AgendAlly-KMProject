package services

import database.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * 🏢 Servicio para configurar nuevas organizaciones
 */
class OrganizationSetupService {

    /**
     * Crear organización completa con canales básicos
     */
    fun createOrganizationWithBasicChannels(
        name: String,
        acronym: String,
        description: String,
        address: String,
        email: String,
        phone: String,
        studentNumber: Int,
        teacherNumber: Int,
        website: String?,
        facebook: String?,
        instagram: String?,
        twitter: String?,
        youtube: String?,
        linkedin: String?,
        adminUserId: Int
    ): Int {
        return transaction {
            println("🏢 Creando organización: $name")

            // 1. Crear organización
            val organizationId = Organizations.insert {
                it[Organizations.name] = name
                it[Organizations.acronym] = acronym.uppercase()
                it[Organizations.description] = description
                it[Organizations.address] = address
                it[Organizations.email] = email
                it[Organizations.phone] = phone
                it[Organizations.studentNumber] = studentNumber
                it[Organizations.teacherNumber] = teacherNumber
                it[Organizations.webSite] = website
                it[Organizations.facebook] = facebook
                it[Organizations.instagram] = instagram
                it[Organizations.twitter] = twitter
                it[Organizations.youtube] = youtube
                it[Organizations.linkedin] = linkedin
                it[Organizations.isActive] = true
                it[Organizations.createdAt] = LocalDateTime.now()
            } get Organizations.id

            println("✅ Organización creada con ID: $organizationId")

            // 2. Crear canales básicos
            createBasicChannels(organizationId)

            // 3. Asociar usuario admin con la organización
            Users.update({ Users.id eq adminUserId }) {
                it[Users.organizationId] = organizationId
                it[Users.role] = "SUPER_ADMIN" // El creador es SUPER_ADMIN
            }

            println("✅ Usuario $adminUserId asignado como SUPER_ADMIN de organización $organizationId")

            // 4. Suscribir admin a todos los canales
            subscribeAdminToAllChannels(adminUserId, organizationId)

            organizationId
        }
    }

    /**
     * Crear canales básicos para la organización
     */
    private fun createBasicChannels(organizationId: Int) {
        println("📋 Creando canales básicos para organización $organizationId")

        val basicChannels = listOf(
            Triple("General", "GEN", "DEPARTMENT"),
        )

        basicChannels.forEach { (name, acronym, description) ->
            val channelId = Channels.insert {
                it[Channels.organizationId] = organizationId
                it[Channels.name] = name
                it[Channels.acronym] = acronym
                it[Channels.description] = description

                it[Channels.isActive] = true
                it[Channels.createdAt] = LocalDateTime.now()
            } get Channels.id

            println("   ✅ Canal creado: $name (ID: $channelId)")
        }
    }

    /**
     * Suscribir admin a todos los canales de la organización
     */
    private fun subscribeAdminToAllChannels(userId: Int, organizationId: Int) {
        println("🔗 Suscribiendo admin $userId a canales de organización $organizationId")

        val channels = Channels.select {
            (Channels.organizationId eq organizationId) and (Channels.isActive eq true)
        }

        channels.forEach { channel ->
            try {
                UserSubscriptions.insert {
                    it[UserSubscriptions.userId] = userId
                    it[UserSubscriptions.channelId] = channel[Channels.id]
                    it[UserSubscriptions.isActive] = true
                    it[UserSubscriptions.notificationsEnabled] = true
                    it[UserSubscriptions.subscribedAt] = LocalDateTime.now()
                }
                println("   ✅ Suscrito a: ${channel[Channels.name]}")
            } catch (e: Exception) {
                println("   ⚠️ Error suscribiendo a canal ${channel[Channels.id]}: ${e.message}")
            }
        }
    }

}