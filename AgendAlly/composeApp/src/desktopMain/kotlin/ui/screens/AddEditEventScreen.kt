package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import models.Event
import models.EventCategory
import models.EventTestData

@Composable
fun AddEditEventScreen(
    isEditing: Boolean,
    event: Event? = null,
    onSave: (Event) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(event?.title ?: "") }
    var description by remember { mutableStateOf(event?.shortDescription ?: "") }
    var location by remember { mutableStateOf(event?.location ?: "") }
    var selectedColorIndex by remember { mutableIntStateOf(event?.colorIndex ?: 0) }
    var selectedCategory by remember { mutableStateOf(event?.category ?: EventCategory.INSTITUTIONAL) }
    var startDate by remember { mutableStateOf(event?.startDate ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date) }
    var endDate by remember { mutableStateOf(event?.endDate ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date) }
    var isMultiDay by remember { mutableStateOf(event?.let { it.startDate != it.endDate } ?: false) }

    var showColorPicker by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Encabezado
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            IconButton(onClick = onCancel) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Cancelar"
                )
            }

            Text(
                text = if (isEditing) "Editar evento" else "Nuevo evento",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Título
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título del evento") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Descripción
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Ubicación
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Ubicación (opcional)") },
            leadingIcon = {
                Icon(Icons.Default.LocationOn, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Categoría
        CategorySelector(
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Switch de evento de varios días
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Evento de varios días",
                style = MaterialTheme.typography.bodyLarge
            )

            Switch(
                checked = isMultiDay,
                onCheckedChange = {
                    isMultiDay = it
                    if (!it) {
                        endDate = startDate
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fechas
        if (isMultiDay) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Fecha de inicio
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fecha de inicio",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = "${startDate.dayOfMonth}/${startDate.monthNumber}/${startDate.year}",
                        onValueChange = { },
                        readOnly = true,
                        leadingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showStartDatePicker = true }
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Fecha de fin
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fecha de fin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = "${endDate.dayOfMonth}/${endDate.monthNumber}/${endDate.year}",
                        onValueChange = { },
                        readOnly = true,
                        leadingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showEndDatePicker = true }
                    )
                }
            }
        } else {
            // Una sola fecha
            OutlinedTextField(
                value = "${startDate.dayOfMonth}/${startDate.monthNumber}/${startDate.year}",
                onValueChange = { },
                label = { Text("Fecha") },
                readOnly = true,
                leadingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showStartDatePicker = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selector de color
        ColorSelector(
            selectedColorIndex = selectedColorIndex,
            onColorSelected = { selectedColorIndex = it },
            onShowColorPicker = { showColorPicker = true }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val newEvent = Event(
                            id = event?.id ?: 0,
                            title = title.trim(),
                            shortDescription = description.trim(),
                            longDescription = description.trim(),
                            location = location.trim(),
                            colorIndex = selectedColorIndex,
                            startDate = startDate,
                            endDate = if (isMultiDay) endDate else startDate,
                            category = selectedCategory
                        )
                        onSave(newEvent)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isEditing) "Guardar" else "Crear evento")
            }
        }
    }

    // Diálogos
    if (showColorPicker) {
        ColorPickerDialog(
            selectedColorIndex = selectedColorIndex,
            onColorSelected = {
                selectedColorIndex = it
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            selectedDate = startDate,
            onDateSelected = {
                startDate = it
                if (!isMultiDay || endDate < it) {
                    endDate = it
                }
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            selectedDate = endDate,
            minDate = startDate,
            onDateSelected = {
                endDate = it
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@Composable
fun CategorySelector(
    selectedCategory: EventCategory,
    onCategorySelected: (EventCategory) -> Unit
) {
    Column {
        Text(
            text = "Categoría",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EventCategory.values().forEach { category ->
                FilterChip(
                    onClick = { onCategorySelected(category) },
                    label = { Text(category.displayName) },
                    selected = selectedCategory == category,
                    leadingIcon = {
                        Icon(
                            imageVector = when (category) {
                                EventCategory.INSTITUTIONAL -> Icons.Default.Business
                                EventCategory.CAREER -> Icons.Default.School
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ColorSelector(
    selectedColorIndex: Int,
    onColorSelected: (Int) -> Unit,
    onShowColorPicker: () -> Unit
) {
    Column {
        Text(
            text = "Color",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Color seleccionado
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(EventTestData.scheduleColors[selectedColorIndex % EventTestData.scheduleColors.size]))
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable { onShowColorPicker() }
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Colores rápidos (primeros 4)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EventTestData.scheduleColors.take(4).forEachIndexed { index, colorLong ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(colorLong))
                            .border(
                                width = if (selectedColorIndex == index) 2.dp else 1.dp,
                                color = if (selectedColorIndex == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .clickable { onColorSelected(index) }
                    )
                }

                // Botón "más colores"
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable { onShowColorPicker() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MoreHoriz,
                        contentDescription = "Más colores",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ColorPickerDialog(
    selectedColorIndex: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Seleccionar color",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(EventTestData.scheduleColors.mapIndexed { index, color -> index to color }) { (index, colorLong) ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(colorLong))
                                .border(
                                    width = if (selectedColorIndex == index) 3.dp else 1.dp,
                                    color = if (selectedColorIndex == index)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .clickable { onColorSelected(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColorIndex == index) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

@Composable
fun DatePickerDialog(
    selectedDate: LocalDate,
    minDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Seleccionar fecha",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Aquí iría un componente de calendario simple
                // Por simplicidad, usaremos un placeholder
                SimpleDatePicker(
                    selectedDate = selectedDate,
                    minDate = minDate,
                    onDateSelected = onDateSelected
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }

                    Button(onClick = onDismiss) {
                        Text("Aceptar")
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleDatePicker(
    selectedDate: LocalDate,
    minDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    // Por simplicidad, mostraremos campos de entrada para día, mes y año
    var day by remember { mutableIntStateOf(selectedDate.dayOfMonth) }
    var month by remember { mutableIntStateOf(selectedDate.monthNumber) }
    var year by remember { mutableIntStateOf(selectedDate.year) }

    LaunchedEffect(day, month, year) {
        try {
            val newDate = LocalDate(year, month, day)
            if (minDate == null || newDate >= minDate) {
                onDateSelected(newDate)
            }
        } catch (e: Exception) {
            // Fecha inválida, no hacer nada
        }
    }

    Column {
        Text(
            text = "Fecha: ${day}/${month}/${year}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Día
            OutlinedTextField(
                value = day.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { newDay ->
                        if (newDay in 1..31) day = newDay
                    }
                },
                label = { Text("Día") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            // Mes
            OutlinedTextField(
                value = month.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { newMonth ->
                        if (newMonth in 1..12) month = newMonth
                    }
                },
                label = { Text("Mes") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            // Año
            OutlinedTextField(
                value = year.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { newYear ->
                        if (newYear > 2020) year = newYear
                    }
                },
                label = { Text("Año") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
    }
}
