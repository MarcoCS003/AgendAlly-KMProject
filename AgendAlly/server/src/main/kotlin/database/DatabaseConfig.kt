package database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime

// ================================
// ✅ DEFINICIÓN DE TABLAS
// ================================

/**
 * Tabla principal: Organizations (antes Institutes)
 */
object Organizations : Table("organizations") {
    val id = integer("id").autoIncrement()
    val acronym = varchar("acronym", 10).uniqueIndex()
    val name = varchar("name", 255)
    val description = text("description").default("")
    val address = text("address")
    val email = varchar("email", 100)
    val phone = varchar("phone", 20)
    val studentNumber = integer("student_number").default(0)
    val teacherNumber = integer("teacher_number").default(0)
    val logoUrl = varchar("logo_url", 500).nullable()
    val webSite = varchar("website", 255).nullable()
    val facebook = varchar("facebook", 255).nullable()
    val instagram = varchar("instagram", 255).nullable()
    val twitter = varchar("twitter", 255).nullable()
    val youtube = varchar("youtube", 255).nullable()
    val linkedin = varchar("linkedin", 255).nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").nullable()

    override val primaryKey = PrimaryKey(id)
}

/**
 * Tabla de canales: Channels (reemplaza Careers)
 */
object Channels : Table("channels") {
    val id = integer("id").autoIncrement()
    val organizationId = reference("organization_id", Organizations.id)
    val name = varchar("name", 255) // "Ingeniería en TICS", "Servicios Escolares"
    val acronym = varchar("acronym", 50) // "TICS", "SE", "BIBLIO"
    val description = text("description").default("")
    val type = varchar("type", 50).default("CAREER") // CAREER, DEPARTMENT, ADMINISTRATIVE
    val email = varchar("email", 255).nullable()
    val phone = varchar("phone", 20).nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").nullable()

    override val primaryKey = PrimaryKey(id)

    // Evitar duplicados por organización + acrónimo
    init {
        uniqueIndex(organizationId, acronym)
    }
}

/**
 * Tabla de eventos del blog
 */
object BlogEvents : Table("blog_events") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val shortDescription = text("short_description").default("")
    val longDescription = text("long_description").default("")
    val location = varchar("location", 255).default("")
    val startDate = date("start_date").nullable()
    val endDate = date("end_date").nullable()
    val category = varchar("category", 50).default("INSTITUTIONAL") // INSTITUTIONAL, CAREER, DEPARTMENT
    val imagePath = varchar("image_path", 500).default("")
    val organizationId = reference("organization_id", Organizations.id)
    val channelId = reference("channel_id", Channels.id).nullable() // Para eventos específicos de canal
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").nullable()
    val isActive = bool("is_active").default(true)

    override val primaryKey = PrimaryKey(id)
}

/**
 * Tabla de items/detalles de eventos
 */
object BlogEventItems : Table("blog_event_items") {
    val id = integer("id").autoIncrement()
    val eventId = reference("event_id", BlogEvents.id)
    val type = varchar("type", 50) // SCHEDULE, ATTACHMENT, PHONE, etc.
    val title = varchar("title", 255) // Título descriptivo
    val value = text("value") // Valor del item (URL, texto, etc.)
    val isClickable = bool("is_clickable").default(false)
    val iconName = varchar("icon_name", 50).nullable()
    val sortOrder = integer("sort_order").default(0) // Para ordenar los items

    override val primaryKey = PrimaryKey(id)
}

/**
 * Tabla de usuarios
 */
object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val googleId = varchar("google_id", 255).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val name = varchar("name", 255)
    val profilePicture = varchar("profile_picture", 500).nullable()
    val role = varchar("role", 50).default("STUDENT") // STUDENT, ADMIN, SUPER_ADMIN
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val lastLoginAt = datetime("last_login_at").nullable()
    val authToken = text("auth_token").nullable() // JWT token
    val tokenExpiresAt = datetime("token_expires_at").nullable()
    val notificationsEnabled = bool("notifications_enabled").default(true)
    val syncEnabled = bool("sync_enabled").default(false)
    val lastSyncAt = datetime("last_sync_at").nullable()

    // ✨ NUEVOS CAMPOS PARA AUTENTICACIÓN FIREBASE
    val organizationId = reference("organization_id", Organizations.id).nullable()
    val firebaseUid = varchar("firebase_uid", 255).uniqueIndex().nullable()
    val refreshToken = text("refresh_token").nullable()
    val lastOrganizationAccess = datetime("last_organization_access").nullable()
    val emailVerified = bool("email_verified").default(false)
    val accountType = varchar("account_type", 50).default("GOOGLE") // GOOGLE, MICROSOFT, etc.

    override val primaryKey = PrimaryKey(id)
}
/**
 * Tabla de suscripciones de usuarios a canales
 */
object UserSubscriptions : Table("user_subscriptions") {
    val id = integer("id").autoIncrement()
    val userId = reference("user_id", Users.id)
    val channelId = reference("channel_id", Channels.id)
    val subscribedAt = datetime("subscribed_at").default(LocalDateTime.now())
    val isActive = bool("is_active").default(true)
    val notificationsEnabled = bool("notifications_enabled").default(true)
    val syncedAt = datetime("synced_at").nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        // Un usuario solo puede suscribirse una vez por canal
        uniqueIndex(userId, channelId)
    }
}

// ================================
// ✅ CONFIGURACIÓN DE LA BASE DE DATOS
// ================================

/**
 * Inicializar base de datos con todas las tablas y datos de ejemplo
 */
fun initDatabase() {
    // Conectar a H2 (base de datos en memoria para desarrollo)
    Database.connect(
        url = "jdbc:h2:mem:academically;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        driver = "org.h2.Driver"
    )

    println("🔗 Conectando a base de datos H2...")

    // Crear todas las tablas
    transaction {
        SchemaUtils.create(
            Organizations,
            Channels,
            BlogEvents,
            BlogEventItems,
            Users,
            UserSubscriptions
        )

        println("✅ Tablas creadas exitosamente")

        // Insertar datos de ejemplo solo si las tablas están vacías
        if (Organizations.selectAll().count() == 0L) {
            println("📊 Insertando datos de ejemplo...")
            insertSampleData()
            println("✅ Datos de ejemplo insertados")
        } else {
            println("ℹ️ Datos ya existen, omitiendo inserción")
        }
    }

    println("✅ Base de datos H2 inicializada exitosamente")
    println("📊 URL: jdbc:h2:mem:academically")
    println("🔗 Console: http://localhost:8080/h2-console (si está habilitado)")
}

// ================================
// ✅ INSERCIÓN DE DATOS DE EJEMPLO
// ================================

/**
 * Insertar todas las organizaciones, canales y eventos de ejemplo
 */
private fun insertSampleData() {
    insertOrganizations()
    insertChannels()
    insertEvents()
    insertEventItems()
    insertSampleUsers()
}

/**
 * Insertar organizaciones de ejemplo
 */
private fun insertOrganizations() {
    println("🏢 Insertando organizaciones...")

    // Instituto Tecnológico de Puebla
    val itpId = Organizations.insert {
        it[acronym] = "ITP"
        it[name] = "Instituto Tecnológico de Puebla"
        it[description] = "Instituto líder en educación tecnológica en el estado de Puebla"
        it[address] = "Del Tecnológico 420, Corredor Industrial la Ciénega, 72220 Heroica Puebla de Zaragoza, Pue."
        it[email] = "info@puebla.tecnm.mx"
        it[phone] = "222 229 8810"
        it[studentNumber] = 8500
        it[teacherNumber] = 280
        it[webSite] = "https://www.puebla.tecnm.mx"
        it[facebook] = "https://www.facebook.com/itpuebla"
        it[instagram] = "https://www.instagram.com/itpuebla"
        it[isActive] = true
    } get Organizations.id

    // Instituto Tecnológico de Tijuana
    val ittId = Organizations.insert {
        it[acronym] = "ITT"
        it[name] = "Instituto Tecnológico de Tijuana"
        it[description] = "Excelencia académica en la frontera norte"
        it[address] = "Calzada del Tecnológico S/N, Fraccionamiento Tomas Aquino, 22414 Tijuana, B.C."
        it[email] = "info@tijuana.tecnm.mx"
        it[phone] = "664 607 8400"
        it[studentNumber] = 7500
        it[teacherNumber] = 350
        it[webSite] = "https://www.tijuana.tecnm.mx"
        it[facebook] = "https://www.facebook.com/tectijuana"
        it[instagram] = "https://www.instagram.com/tecnmtijuana"
        it[isActive] = true
    } get Organizations.id

    // Instituto Tecnológico de Monterrey (Campus ejemplo)
    val itmId = Organizations.insert {
        it[acronym] = "ITM"
        it[name] = "Instituto Tecnológico de Monterrey"
        it[description] = "Campus tecnológico de vanguardia en el noreste"
        it[address] = "Av. Eugenio Garza Sada 2501 Sur, Tecnológico, 64849 Monterrey, N.L."
        it[email] = "info@monterrey.tecnm.mx"
        it[phone] = "81 8358 2000"
        it[studentNumber] = 12000
        it[teacherNumber] = 450
        it[webSite] = "https://www.monterrey.tecnm.mx"
        it[isActive] = true
    } get Organizations.id

    println("✅ ${Organizations.selectAll().count()} organizaciones insertadas")
}

/**
 * Insertar canales para todas las organizaciones
 */
private fun insertChannels() {
    println("📺 Insertando canales...")

    // Obtener IDs de organizaciones
    val itpId = Organizations.select { Organizations.acronym eq "ITP" }.single()[Organizations.id]
    val ittId = Organizations.select { Organizations.acronym eq "ITT" }.single()[Organizations.id]
    val itmId = Organizations.select { Organizations.acronym eq "ITM" }.single()[Organizations.id]

    // Canales del ITP
    val itpChannels = listOf(
        Triple("Ingeniería en Tecnologías de la Información y Comunicaciones", "TICS", "CAREER"),
        Triple("Ingeniería Industrial", "INDUSTRIAL", "CAREER"),
        Triple("Ingeniería en Sistemas Computacionales", "SISTEMAS", "CAREER"),
        Triple("Licenciatura en Administración", "ADMIN", "CAREER"),
        Triple("Servicios Escolares", "SE", "ADMINISTRATIVE"),
        Triple("Biblioteca", "BIBLIO", "DEPARTMENT"),
        Triple("Centro de Cómputo", "COMPUTO", "DEPARTMENT"),
        Triple("Recursos Humanos", "RH", "ADMINISTRATIVE")
    )

    itpChannels.forEach { (name, acronym, type) ->
        Channels.insert {
            it[organizationId] = itpId
            it[this.name] = name
            it[this.acronym] = acronym
            it[description] = "Canal de $name del ITP"
            it[this.type] = type
            it[email] = "${acronym.lowercase()}@puebla.tecnm.mx"
            it[phone] = "222 229 8810"
            it[isActive] = true
        }
    }

    // Canales del ITT
    val ittChannels = listOf(
        Triple("Ingeniería en Tecnologías de la Información", "TICS_TIJ", "CAREER"),
        Triple("Ingeniería Electrónica", "ELECTRONICA", "CAREER"),
        Triple("Ingeniería Mecánica", "MECANICA", "CAREER"),
        Triple("Servicios Escolares", "SE_TIJ", "ADMINISTRATIVE"),
        Triple("Biblioteca", "BIBLIO_TIJ", "DEPARTMENT")
    )

    ittChannels.forEach { (name, acronym, type) ->
        Channels.insert {
            it[organizationId] = ittId
            it[this.name] = name
            it[this.acronym] = acronym
            it[description] = "Canal de $name del ITT"
            it[this.type] = type
            it[email] = "${acronym.lowercase()}@tijuana.tecnm.mx"
            it[phone] = "664 607 8400"
            it[isActive] = true
        }
    }

    // Canales del ITM
    val itmChannels = listOf(
        Triple("Ingeniería en Software", "SOFTWARE", "CAREER"),
        Triple("Ingeniería en Datos e Inteligencia Artificial", "IA", "CAREER"),
        Triple("Servicios Escolares", "SE_MTY", "ADMINISTRATIVE"),
        Triple("Biblioteca", "BIBLIO_MTY", "DEPARTMENT")
    )

    itmChannels.forEach { (name, acronym, type) ->
        Channels.insert {
            it[organizationId] = itmId
            it[this.name] = name
            it[this.acronym] = acronym
            it[description] = "Canal de $name del ITM"
            it[this.type] = type
            it[email] = "${acronym.lowercase()}@monterrey.tecnm.mx"
            it[phone] = "81 8358 2000"
            it[isActive] = true
        }
    }

    println("✅ ${Channels.selectAll().count()} canales insertados")
}

/**
 * Insertar eventos de ejemplo
 */
private fun insertEvents() {
    println("📅 Insertando eventos...")

    // Obtener IDs necesarios
    val itpId = Organizations.select { Organizations.acronym eq "ITP" }.single()[Organizations.id]
    val ittId = Organizations.select { Organizations.acronym eq "ITT" }.single()[Organizations.id]

    val ticsChannelId = Channels.select {
        (Channels.organizationId eq itpId) and (Channels.acronym eq "TICS")
    }.single()[Channels.id]

    val seChannelId = Channels.select {
        (Channels.organizationId eq itpId) and (Channels.acronym eq "SE")
    }.single()[Channels.id]

    val biblioChannelId = Channels.select {
        (Channels.organizationId eq itpId) and (Channels.acronym eq "BIBLIO")
    }.single()[Channels.id]

    // Evento 1: INNOVATECNM 2025
    val evento1Id = BlogEvents.insert {
        it[title] = "INNOVATECNM 2025"
        it[shortDescription] = "Cumbre nacional de desarrollo tecnológico e investigación"
        it[longDescription] = """
            Cumbre nacional de desarrollo tecnológico, investigación e innovación INNOVATECNM. 
            Dirigida al estudiantado inscrito al periodo Enero-Junio 2025, personal docente y 
            de investigación del Instituto Tecnológico de Puebla.
            
            Incluye conferencias magistrales, talleres especializados, exposición de proyectos 
            estudiantiles y networking con empresas del sector tecnológico.
        """.trimIndent()
        it[location] = "Auditorio Principal ITP"
        it[startDate] = LocalDate.of(2025, 7, 15)
        it[endDate] = LocalDate.of(2025, 7, 17)
        it[category] = "INSTITUTIONAL"
        it[imagePath] = "InnovaTecNM.jpg"
        it[organizationId] = itpId
        it[channelId] = null // Evento institucional
        it[isActive] = true
    } get BlogEvents.id

    // Evento 2: Taller de Machine Learning
    val evento2Id = BlogEvents.insert {
        it[title] = "Taller de Machine Learning con Python"
        it[shortDescription] = "Introducción práctica al aprendizaje automático"
        it[longDescription] = """
            Taller intensivo sobre fundamentos de Machine Learning con Python y TensorFlow. 
            Incluye ejemplos prácticos, proyectos hands-on y casos de uso reales en la industria.
            
            Requisitos: Conocimientos básicos de Python y matemáticas.
        """.trimIndent()
        it[location] = "Laboratorio de Cómputo TICS"
        it[startDate] = LocalDate.of(2025, 7, 1)
        it[endDate] = LocalDate.of(2025, 7, 3)
        it[category] = "CAREER"
        it[imagePath] = "conferencia_ia.jpg"
        it[organizationId] = itpId
        it[channelId] = ticsChannelId
        it[isActive] = true
    } get BlogEvents.id

    // Evento 3: Proceso de Inscripciones
    BlogEvents.insert {
        it[title] = "Proceso de Inscripciones Agosto 2025"
        it[shortDescription] = "Información importante sobre inscripciones del próximo semestre"
        it[longDescription] = """
            Fechas importantes, documentos requeridos y procedimientos para el proceso de 
            inscripciones del periodo Agosto-Diciembre 2025.
            
            Incluye información sobre becas disponibles, horarios de atención y 
            plataformas digitales para realizar trámites.
        """.trimIndent()
        it[location] = "Servicios Escolares"
        it[startDate] = LocalDate.of(2025, 7, 20)
        it[endDate] = LocalDate.of(2025, 7, 30)
        it[category] = "INSTITUTIONAL"
        it[organizationId] = itpId
        it[channelId] = seChannelId
        it[isActive] = true
    }

    // Evento 4: Renovación de Biblioteca Digital
    BlogEvents.insert {
        it[title] = "Renovación de Biblioteca Digital"
        it[shortDescription] = "Nueva plataforma de recursos digitales disponible"
        it[longDescription] = """
            La biblioteca ha actualizado su plataforma digital con nuevos recursos académicos, 
            bases de datos especializadas y herramientas de investigación.
            
            Capacitación disponible para estudiantes y docentes sobre el uso de las nuevas herramientas.
        """.trimIndent()
        it[location] = "Biblioteca Central"
        it[startDate] = LocalDate.of(2025, 6, 30)
        it[endDate] = null
        it[category] = "INSTITUTIONAL"
        it[organizationId] = itpId
        it[channelId] = biblioChannelId
        it[isActive] = true
    }

    // Evento 5: Hackathon TICS 2025
    BlogEvents.insert {
        it[title] = "Hackathon TICS 2025"
        it[shortDescription] = "Competencia de programación de 48 horas"
        it[longDescription] = """
            Competencia de desarrollo de software de 48 horas continuas. 
            Equipos de 3-5 estudiantes crearán soluciones innovadoras para problemas reales.
            
            Premios: $50,000 primer lugar, $30,000 segundo lugar, $20,000 tercer lugar.
        """.trimIndent()
        it[location] = "Centro de Innovación ITP"
        it[startDate] = LocalDate.of(2025, 8, 15)
        it[endDate] = LocalDate.of(2025, 8, 17)
        it[category] = "CAREER"
        it[organizationId] = itpId
        it[imagePath] = "concurso_programacion.jpg"
        it[channelId] = ticsChannelId
        it[isActive] = true
    }

    println("✅ ${BlogEvents.selectAll().count()} eventos insertados")
}

/**
 * Insertar items de eventos (detalles adicionales)
 */
private fun insertEventItems() {
    println("📋 Insertando items de eventos...")

    // Obtener evento INNOVATECNM
    val innovatecnmEvent = BlogEvents.select {
        BlogEvents.title eq "INNOVATECNM 2025"
    }.single()
    val eventId = innovatecnmEvent[BlogEvents.id]

    // Items para INNOVATECNM 2025
    val items = listOf(
        Triple("SCHEDULE", "Horario", "15-17 Julio 2025, 9:00 AM - 6:00 PM"),
        Triple("REGISTRATION_LINK", "Registro", "https://innovatecnm.tecnm.mx/registro"),
        Triple("EMAIL", "Contacto", "innovatecnm@puebla.tecnm.mx"),
        Triple("PHONE", "Teléfono", "222 229 8810 ext. 500"),
        Triple("REQUIREMENTS", "Requisitos", "Estudiante activo del TecNM"),
        Triple("CAPACITY", "Cupo", "500 participantes")
    )

    items.forEachIndexed { index, (type, title, value) ->
        BlogEventItems.insert {
            it[this.eventId] = eventId
            it[this.type] = type
            it[this.title] = title
            it[this.value] = value
            it[isClickable] = type in listOf("REGISTRATION_LINK", "EMAIL", "PHONE")
            it[iconName] = when(type) {
                "SCHEDULE" -> "schedule"
                "REGISTRATION_LINK" -> "link"
                "EMAIL" -> "email"
                "PHONE" -> "phone"
                "REQUIREMENTS" -> "info"
                "CAPACITY" -> "people"
                else -> null
            }
            it[sortOrder] = index
        }
    }

    println("✅ ${BlogEventItems.selectAll().count()} items de eventos insertados")
}

/**
 * Insertar usuarios de ejemplo
 */
private fun insertSampleUsers() {
    println("👥 Insertando usuarios de ejemplo...")

    // Usuario administrador
    Users.insert {
        it[googleId] = "admin_google_id_123"
        it[email] = "admin@puebla.tecnm.mx"
        it[name] = "Administrador ITP"
        it[role] = "ADMIN"
        it[isActive] = true
        it[notificationsEnabled] = true
    }

    // Usuario estudiante
    Users.insert {
        it[googleId] = "student_google_id_456"
        it[email] = "estudiante@alumnos.puebla.tecnm.mx"
        it[name] = "Juan Pérez"
        it[role] = "STUDENT"
        it[isActive] = true
        it[notificationsEnabled] = true
    }

    println("✅ ${Users.selectAll().count()} usuarios insertados")
}

// ================================
// ✅ UTILIDADES PARA DESARROLLO
// ================================

/**
 * Limpiar todas las tablas (para reiniciar datos)
 */
fun clearAllTables() {
    transaction {
        SchemaUtils.drop(
            UserSubscriptions,
            BlogEventItems,
            BlogEvents,
            Channels,
            Organizations,
            Users
        )

        SchemaUtils.create(
            Organizations,
            Channels,
            BlogEvents,
            BlogEventItems,
            Users,
            UserSubscriptions
        )

        insertSampleData()
    }
    println("✅ Base de datos reiniciada con datos frescos")
}

/**
 * Obtener estadísticas generales de la base de datos
 */
fun getDatabaseStats(): Map<String, Long> = transaction {
    mapOf(
        "organizations" to Organizations.selectAll().count(),
        "channels" to Channels.selectAll().count(),
        "events" to BlogEvents.selectAll().count(),
        "event_items" to BlogEventItems.selectAll().count(),
        "users" to Users.selectAll().count(),
        "subscriptions" to UserSubscriptions.selectAll().count()
    )
}