package com.example.services

import com.example.*
import database.*

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BlogEventsService {

    /**
     * Obtener todos los eventos activos
     */
    fun getAllEvents(): List<EventInstituteBlog> = transaction {
        BlogEvents.selectAll()
            .orWhere { BlogEvents.isActive eq true }
            .orderBy(BlogEvents.startDate to SortOrder.ASC)
            .map { mapRowToEvent(it) }
    }

    /**
     * Obtener eventos por organización
     */
    fun getEventsByOrganization(organizationId: Int): BlogEventsResponse = transaction {
        // Obtener información de la organización
        val organization = Organizations.select { Organizations.id eq organizationId }
            .singleOrNull()?.let { mapRowToOrganization(it) }

        // Obtener eventos de la organización
        val events = BlogEvents.selectAll()
            .orWhere {
                (BlogEvents.organizationId eq organizationId) and
                        (BlogEvents.isActive eq true)
            }
            .orderBy(BlogEvents.startDate to SortOrder.ASC)
            .map { mapRowToEvent(it) }

        BlogEventsResponse(
            events = events,
            total = events.size,
            organizationInfo = organization
        )
    }

    /**
     * Obtener eventos por canal
     */
    fun getEventsByChannel(channelId: Int): List<EventInstituteBlog> = transaction {
        BlogEvents.selectAll()
            .orWhere {
                (BlogEvents.channelId eq channelId) and
                        (BlogEvents.isActive eq true)
            }
            .orderBy(BlogEvents.startDate to SortOrder.ASC)
            .map { mapRowToEvent(it) }
    }

    /**
     * Obtener evento por ID
     */
    fun getEventById(eventId: Int): EventInstituteBlog? = transaction {
        BlogEvents.select {
            (BlogEvents.id eq eventId) and
                    (BlogEvents.isActive eq true)
        }.singleOrNull()?.let { mapRowToEvent(it) }
    }

    /**
     * Buscar eventos por texto
     */
    fun searchEvents(query: String): List<EventInstituteBlog> = transaction {
        val searchTerm = "%${query.lowercase()}%"

        BlogEvents.selectAll()
            .orWhere {
                ((BlogEvents.title.lowerCase() like searchTerm) or
                        (BlogEvents.shortDescription.lowerCase() like searchTerm) or
                        (BlogEvents.longDescription.lowerCase() like searchTerm)) and
                        (BlogEvents.isActive eq true)
            }
            .orderBy(BlogEvents.startDate to SortOrder.ASC)
            .map { mapRowToEvent(it) }
    }

    /**
     * Obtener eventos próximos
     */
    fun getUpcomingEvents(): List<EventInstituteBlog> = transaction {
        val today = LocalDate.now()
        val nextMonth = today.plusMonths(1)

        BlogEvents.selectAll()
            .orWhere {
                (BlogEvents.startDate greaterEq today) and
                        (BlogEvents.startDate lessEq nextMonth) and
                        (BlogEvents.isActive eq true)
            }
            .orderBy(BlogEvents.startDate to SortOrder.ASC)
            .map { mapRowToEvent(it) }
    }

    /**
     * Obtener eventos por categoría
     */
    fun getEventsByCategory(category: String): List<EventInstituteBlog> = transaction {
        BlogEvents.selectAll()
            .orWhere {
                (BlogEvents.category eq category) and
                        (BlogEvents.isActive eq true)
            }
            .orderBy(BlogEvents.startDate to SortOrder.ASC)
            .map { mapRowToEvent(it) }
    }

    /**
     * Obtener eventos en rango de fechas
     */
    fun getEventsByDateRange(startDate: String, endDate: String): List<EventInstituteBlog> = transaction {
        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)

        BlogEvents.selectAll()
            .orWhere {
                (BlogEvents.startDate greaterEq start) and
                        (BlogEvents.startDate lessEq end) and
                        (BlogEvents.isActive eq true)
            }
            .orderBy(BlogEvents.startDate to SortOrder.ASC)
            .map { mapRowToEvent(it) }
    }

    /**
     * Obtener estadísticas de eventos
     */
    fun getEventStats(): EventStatsResponse = transaction {
        val totalEvents = BlogEvents.select { BlogEvents.isActive eq true }.count()

        val eventsByCategory = BlogEvents
            .slice(BlogEvents.category, BlogEvents.id.count())
            .select { BlogEvents.isActive eq true }
            .groupBy(BlogEvents.category)
            .associate {
                it[BlogEvents.category] to it[BlogEvents.id.count()]
            }

        EventStatsResponse(
            totalEvents = totalEvents,
            eventsByCategory = eventsByCategory,
            lastUpdated = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        )
    }

    /**
     * Mapear row de BD a modelo EventInstituteBlog
     */
    private fun mapRowToEvent(row: ResultRow): EventInstituteBlog {
        return EventInstituteBlog(
            id = row[BlogEvents.id],
            title = row[BlogEvents.title],
            shortDescription = row[BlogEvents.shortDescription],
            longDescription = row[BlogEvents.longDescription],
            location = row[BlogEvents.location],
            startDate = row[BlogEvents.startDate]?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            endDate = row[BlogEvents.endDate]?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            category = row[BlogEvents.category],
            imagePath = row[BlogEvents.imagePath],
            organizationId = row[BlogEvents.organizationId],
            channelId = row[BlogEvents.channelId],
            items = getEventItems(row[BlogEvents.id]),
            createdAt = row[BlogEvents.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            updatedAt = row[BlogEvents.updatedAt]?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            isActive = row[BlogEvents.isActive]
        )
    }

    /**
     * Obtener items de un evento
     */
    private fun getEventItems(eventId: Int): List<EventItemBlog> = transaction {
        BlogEventItems.select { BlogEventItems.eventId eq eventId }
            .orderBy(BlogEventItems.sortOrder to SortOrder.ASC)
            .map { itemRow ->
                EventItemBlog(
                    id = itemRow[BlogEventItems.id],
                    type = EventItemType.valueOf(itemRow[BlogEventItems.type]),
                    title = itemRow[BlogEventItems.title],
                    value = itemRow[BlogEventItems.value],
                    isClickable = itemRow[BlogEventItems.isClickable],
                    iconName = itemRow[BlogEventItems.iconName]
                )
            }
    }

    /**
     * Mapear row de BD a modelo Organization (método auxiliar)
     */
    private fun mapRowToOrganization(row: ResultRow): Organization {
        return Organization(
            organizationID = row[Organizations.id],
            acronym = row[Organizations.acronym],
            name = row[Organizations.name],
            description = row[Organizations.description],
            address = row[Organizations.address],
            email = row[Organizations.email],
            phone = row[Organizations.phone],
            logoUrl = row[Organizations.logoUrl],
            webSite = row[Organizations.webSite],
            facebook = row[Organizations.facebook],
            instagram = row[Organizations.instagram],
            twitter = row[Organizations.twitter],
            youtube = row[Organizations.youtube],
            linkedin = row[Organizations.linkedin],
            channels = emptyList(),
            isActive = row[Organizations.isActive],
            createdAt = row[Organizations.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            updatedAt = row[Organizations.updatedAt]?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            studentNumber = 0,
            teacherNumber = 0
        )
    }
    fun createEvent(request: CreateEventRequest, createdByUserId: Int): Int? = transaction {
        try {
            println("📝 Creando evento: ${request.title}")

            // 1. Insertar evento principal
            val eventId = BlogEvents.insert {
                it[title] = request.title
                it[shortDescription] = request.shortDescription
                it[longDescription] = request.longDescription
                it[location] = request.location
                it[startDate] = request.startDate?.let { date -> LocalDate.parse(date) }
                it[endDate] = request.endDate?.let { date -> LocalDate.parse(date) }
                it[category] = request.category
                it[imagePath] = request.imagePath
                it[organizationId] = request.organizationId
                it[channelId] = request.channelId
                it[createdAt] = LocalDateTime.now()
                it[isActive] = true
            } get BlogEvents.id

            println("✅ Evento creado con ID: $eventId")

            // 2. Insertar items del evento
            if (request.items.isNotEmpty()) {
                request.items.forEachIndexed { index, item ->
                    BlogEventItems.insert {
                        it[this.eventId] = eventId
                        it[type] = item.type
                        it[title] = item.title
                        it[value] = item.value
                        it[isClickable] = item.isClickable
                        it[iconName] = item.iconName
                        it[sortOrder] = item.sortOrder.takeIf { it > 0 } ?: index
                    }
                }
                println("✅ ${request.items.size} items del evento insertados")
            }

            eventId

        } catch (e: Exception) {
            println("❌ Error creando evento: ${e.message}")
            null
        }
    }

    /**
     * ✏️ ACTUALIZAR evento existente
     */
    fun updateEvent(eventId: Int, request: UpdateEventRequest): Boolean = transaction {
        try {
            println("✏️ Actualizando evento ID: $eventId")

            // 1. Actualizar evento principal
            val updateCount = BlogEvents.update({ BlogEvents.id eq eventId }) {
                it[title] = request.title
                it[shortDescription] = request.shortDescription
                it[longDescription] = request.longDescription
                it[location] = request.location
                it[startDate] = request.startDate?.let { date -> LocalDate.parse(date) }
                it[endDate] = request.endDate?.let { date -> LocalDate.parse(date) }
                it[category] = request.category
                it[imagePath] = request.imagePath
                it[channelId] = request.channelId
                it[updatedAt] = LocalDateTime.now()
            }

            if (updateCount == 0) {
                println("❌ No se encontró evento para actualizar")
                return@transaction false
            }

            // 2. Eliminar items existentes y crear nuevos
            BlogEventItems.deleteWhere { BlogEventItems.eventId eq eventId }

            if (request.items.isNotEmpty()) {
                request.items.forEachIndexed { index, item ->
                    BlogEventItems.insert {
                        it[this.eventId] = eventId
                        it[type] = item.type
                        it[title] = item.title
                        it[value] = item.value
                        it[isClickable] = item.isClickable
                        it[iconName] = item.iconName
                        it[sortOrder] = item.sortOrder.takeIf { it > 0 } ?: index
                    }
                }
                println("✅ ${request.items.size} items actualizados")
            }

            println("✅ Evento actualizado exitosamente")
            true

        } catch (e: Exception) {
            println("❌ Error actualizando evento: ${e.message}")
            false
        }
    }

    /**
     * 🗑️ ELIMINAR evento (soft delete)
     */
    fun deleteEvent(eventId: Int): Boolean = transaction {
        try {
            println("🗑️ Eliminando evento ID: $eventId")

            val updateCount = BlogEvents.update({ BlogEvents.id eq eventId }) {
                it[isActive] = false
                it[updatedAt] = LocalDateTime.now()
            }

            if (updateCount > 0) {
                println("✅ Evento marcado como inactivo")
                true
            } else {
                println("❌ No se encontró evento para eliminar")
                false
            }

        } catch (e: Exception) {
            println("❌ Error eliminando evento: ${e.message}")
            false
        }
    }

    /**
     * 📊 OBTENER eventos por usuario/organización (para dashboard admin)
     */
    fun getEventsByUserOrganization(userId: Int): List<EventInstituteBlog> = transaction {
        // Obtener organización del usuario
        val userOrganization = Users.select { Users.id eq userId }
            .singleOrNull()?.get(Users.organizationId)

        if (userOrganization == null) {
            return@transaction emptyList()
        }

        BlogEvents.select {
            (BlogEvents.organizationId eq userOrganization) and
                    (BlogEvents.isActive eq true)
        }
            .orderBy(BlogEvents.createdAt to SortOrder.DESC)
            .map { mapRowToEvent(it) }
    }

    /**
     * 🔢 CONTAR eventos por organización
     */
    fun getEventCountByOrganization(organizationId: Int): Long = transaction {
        BlogEvents.select {
            (BlogEvents.organizationId eq organizationId) and
                    (BlogEvents.isActive eq true)
        }.count()
    }

    /**
     * 📅 OBTENER eventos por mes específico (para calendario)
     */
    fun getEventsByMonth(year: Int, month: Int, organizationId: Int? = null): List<EventInstituteBlog> = transaction {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)

        val query = BlogEvents.select {
            (BlogEvents.startDate greaterEq startDate) and
                    (BlogEvents.startDate lessEq endDate) and
                    (BlogEvents.isActive eq true)
        }

        // Filtrar por organización si se especifica
        val finalQuery = if (organizationId != null) {
            query.andWhere { BlogEvents.organizationId eq organizationId }
        } else {
            query
        }

        finalQuery.orderBy(BlogEvents.startDate to SortOrder.ASC)
            .map { mapRowToEvent(it) }
    }

    /**
     * 🔍 BUSCAR eventos avanzada con múltiples filtros
     */
    fun searchEventsAdvanced(
        query: String? = null,
        organizationId: Int? = null,
        channelId: Int? = null,
        category: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): SearchEventsResult = transaction {

        var sqlQuery = BlogEvents.selectAll().orWhere { BlogEvents.isActive eq true }

        // Aplicar filtros
        if (!query.isNullOrBlank()) {
            val searchTerm = "%${query.lowercase()}%"
            sqlQuery = sqlQuery.andWhere {
                (BlogEvents.title.lowerCase() like searchTerm) or
                        (BlogEvents.shortDescription.lowerCase() like searchTerm) or
                        (BlogEvents.longDescription.lowerCase() like searchTerm)
            }
        }

        if (organizationId != null) {
            sqlQuery = sqlQuery.andWhere { BlogEvents.organizationId eq organizationId }
        }

        if (channelId != null) {
            sqlQuery = sqlQuery.andWhere { BlogEvents.channelId eq channelId }
        }

        if (category != null) {
            sqlQuery = sqlQuery.andWhere { BlogEvents.category eq category }
        }

        if (startDate != null) {
            sqlQuery = sqlQuery.andWhere { BlogEvents.startDate greaterEq LocalDate.parse(startDate) }
        }

        if (endDate != null) {
            sqlQuery = sqlQuery.andWhere { BlogEvents.startDate lessEq LocalDate.parse(endDate) }
        }

        // Contar total
        val total = sqlQuery.count()

        // Aplicar paginación y obtener resultados
        val events = sqlQuery
            .orderBy(BlogEvents.startDate to SortOrder.ASC)
            .limit(limit, offset.toLong())
            .map { mapRowToEvent(it) }

        SearchEventsResult(
            events = events,
            total = total,
            limit = limit,
            offset = offset,
            hasMore = (offset + limit) < total
        )
    }
}