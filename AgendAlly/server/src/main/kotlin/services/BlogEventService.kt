package com.example.services

import com.example.*
import database.BlogEventItems
import database.BlogEvents
import database.Channels

import database.Organizations
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
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
            studentNumber = row[Organizations.studentNumber],
            teacherNumber = row[Organizations.teacherNumber],
            logoUrl = row[Organizations.logoUrl],
            webSite = row[Organizations.webSite],
            facebook = row[Organizations.facebook],
            instagram = row[Organizations.instagram],
            twitter = row[Organizations.twitter],
            youtube = row[Organizations.youtube],
            linkedin = row[Organizations.linkedin],
            channels = emptyList(), // No cargar canales aquí para evitar recursión
            isActive = row[Organizations.isActive],
            createdAt = row[Organizations.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            updatedAt = row[Organizations.updatedAt]?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
    }
}