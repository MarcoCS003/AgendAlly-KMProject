package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import models.Channel
import models.ChannelOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelSelector(
    channels: List<Channel>,
    selectedChannel: ChannelOption,
    onChannelSelected: (ChannelOption) -> Unit,
    label: String = "Canal",
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Crear lista de opciones con "Todos" al inicio
    val channelOptions = remember(channels) {
        listOf(ChannelOption.ALL_CHANNELS) + channels.map { channel ->
            ChannelOption(
                id = channel.id,
                name = channel.name,
                acronym = channel.acronym,
                isAll = false
            )
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = "$label ${selectedChannel.acronym}",
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expandir selector",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            channelOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Acrónimo
                            Surface(
                                color = if (option.isAll) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                },
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = option.acronym,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (option.isAll) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    },
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Nombre completo
                            Text(
                                text = option.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    onClick = {
                        onChannelSelected(option)
                        expanded = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Versión simplificada para formularios
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleChannelSelector(
    channels: List<Channel>,
    selectedChannelId: Int?,
    onChannelSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    includeAllOption: Boolean = true,
    label: String = "Canal"
) {
    var expanded by remember { mutableStateOf(false) }

    val options = if (includeAllOption) {
        listOf(ChannelOption.ALL_CHANNELS) + channels.map { channel ->
            ChannelOption(channel.id, channel.name, channel.acronym)
        }
    } else {
        channels.map { channel ->
            ChannelOption(channel.id, channel.name, channel.acronym)
        }
    }

    val selectedOption = options.find {
        if (selectedChannelId == null) it.isAll else it.id == selectedChannelId
    } ?: options.firstOrNull()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption?.let { "$label ${it.acronym}" } ?: label,
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expandir selector"
                )
            },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = if (option.isAll) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                },
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = option.acronym,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(option.name, style = MaterialTheme.typography.bodyMedium)
                        }
                    },
                    onClick = {
                        onChannelSelected(if (option.isAll) null else option.id)
                        expanded = false
                    }
                )
            }
        }
    }
}