package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Note
import java.text.SimpleDateFormat
import java.util.*

// Pastel colors suitable for light and dark themes
val NoteColors = listOf(
    "#FFFFFF", // Default / Slate
    "#FFF9A6", // Pastel Yellow
    "#FFC6FF", // Pastel Magenta
    "#BDB2FF", // Pastel Purple
    "#9BF6FF", // Pastel Cyan
    "#CAFFBF", // Pastel Green
    "#FFADAD", // Pastel Red
    "#FFD166"  // Pastel Gold
)

// Helper to convert hex string to Compose Color
fun parseNoteColor(hex: String, isDark: Boolean): Color {
    return try {
        val parsed = Color(android.graphics.Color.parseColor(hex))
        if (isDark) {
            // In dark mode, we blend with dark charcoal to keep text legible and elegant
            Color(
                red = (parsed.red + 0.12f).coerceAtMost(1f),
                green = (parsed.green + 0.12f).coerceAtMost(1f),
                blue = (parsed.blue + 0.15f).coerceAtMost(1f),
                alpha = 0.15f
            )
        } else {
            // In light mode, full pastel
            parsed
        }
    } catch (e: Exception) {
        if (isDark) Color(0xFF1E1E2E) else Color.White
    }
}

// Correct contrast text color depending on background mode
fun getTextColorForNote(isDark: Boolean): Color {
    return if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B)
}

fun getSecondaryTextColorForNote(isDark: Boolean): Color {
    return if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteScreen(
    viewModel: NoteViewModel,
    modifier: Modifier = Modifier
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val currentSortOrder by viewModel.currentSortOrder.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("add_note_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Neue Notiz hinzufügen")
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Elegant App Title & Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Notizen",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Gedanken & Pläne organisieren",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                // Sort Button Custom UI using standard MoreVert icon
                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier.testTag("sort_notes_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Notizen sortieren"
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Neueste zuerst") },
                            onClick = {
                                viewModel.currentSortOrder.value = NoteViewModel.SortOrder.UPDATED_DESC
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (currentSortOrder == NoteViewModel.SortOrder.UPDATED_DESC) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Älteste zuerst") },
                            onClick = {
                                viewModel.currentSortOrder.value = NoteViewModel.SortOrder.UPDATED_ASC
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (currentSortOrder == NoteViewModel.SortOrder.UPDATED_ASC) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Titel A-Z") },
                            onClick = {
                                viewModel.currentSortOrder.value = NoteViewModel.SortOrder.TITLE_ASC
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (currentSortOrder == NoteViewModel.SortOrder.TITLE_ASC) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Titel Z-A") },
                            onClick = {
                                viewModel.currentSortOrder.value = NoteViewModel.SortOrder.TITLE_DESC
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (currentSortOrder == NoteViewModel.SortOrder.TITLE_DESC) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                    }
                }
            }

            // Beautiful Integrated Modern Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .testTag("search_bar"),
                placeholder = { Text("Notizen durchsuchen...", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Suchen Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Suche löschen"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                )
            )

            // Horizontal Scrollable Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = category.equals(selectedCategory, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectedCategory.value = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = if (isSelected) null else FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = false,
                            borderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Notes Section
            if (notes.isEmpty()) {
                // Polish Empty State Aligned with Design guidelines
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Keine Notizen",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty() || selectedCategory != "Alle") "Keine Ergebnisse gefunden" else "Keine Notizen vorhanden",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty() || selectedCategory != "Alle") "Passe deine Suchbegriffe oder Filter an." else "Schreibe deine erste Notiz, indem du unten rechts auf das + klickst.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                // Exquisite Pinterest-style staggered grid
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            isDark = isDark,
                            onClick = { noteToEdit = note },
                            onDelete = { viewModel.deleteNote(note.id) },
                            onPinToggle = { viewModel.togglePin(note) }
                        )
                    }
                }
            }
        }
    }

    // Modal Add Note Dialog
    if (showAddDialog) {
        NoteEditDialog(
            isDark = isDark,
            onDismiss = { showAddDialog = false },
            onSave = { title, content, category, colorHex, isPinned ->
                viewModel.addNote(title, content, category, colorHex, isPinned)
                showAddDialog = false
            }
        )
    }

    // Modal Edit Note Dialog
    noteToEdit?.let { note ->
        NoteEditDialog(
            note = note,
            isDark = isDark,
            onDismiss = { noteToEdit = null },
            onSave = { title, content, category, colorHex, isPinned ->
                viewModel.updateNote(
                    note.copy(
                        title = title,
                        content = content,
                        category = category,
                        colorHex = colorHex,
                        isPinned = isPinned
                    )
                )
                noteToEdit = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    isDark: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onPinToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = parseNoteColor(note.colorHex, isDark)
    val textColor = getTextColorForNote(isDark)
    val secondaryTextColor = getSecondaryTextColorForNote(isDark)

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val formattedDate = remember(note.updatedAt) { dateFormat.format(Date(note.updatedAt)) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onPinToggle
            )
            .testTag("note_card_${note.id}"),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(
            width = 1.dp,
            color = if (note.isPinned) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            } else {
                if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            // Header: Category & Pin button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Styled Category Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.12f)
                            else Color.Black.copy(alpha = 0.06f)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = note.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pin badge / toggle tap area (Star icon used for core compatibility)
                    IconButton(
                        onClick = onPinToggle,
                        modifier = Modifier
                            .size(22.dp)
                            .testTag("pin_note_${note.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = if (note.isPinned) "Notiz anpinnen" else "Notiz entpinnen",
                            tint = if (note.isPinned) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.25f),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Delete button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(22.dp)
                            .testTag("delete_note_${note.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Notiz löschen",
                            tint = textColor.copy(alpha = 0.45f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Note Title
            if (note.title.isNotEmpty()) {
                Text(
                    text = note.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Note Content
            Text(
                text = note.content,
                fontSize = 13.sp,
                color = secondaryTextColor,
                maxLines = 8,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Timestamp
            Text(
                text = formattedDate,
                fontSize = 10.sp,
                color = secondaryTextColor.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteEditDialog(
    note: Note? = null,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, category: String, colorHex: String, isPinned: Boolean) -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var category by remember { mutableStateOf(note?.category ?: "Persönlich") }
    var selectedColor by remember { mutableStateOf(note?.colorHex ?: NoteColors.first()) }
    var isPinned by remember { mutableStateOf(note?.isPinned ?: false) }

    val textColor = if (isDark) Color.White else Color.Black

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("note_edit_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (note == null) "Neue Notiz" else "Notiz bearbeiten",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_title_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Field
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategorie / Tag") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_category_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Content Input (Rich expansion size)
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Inhalt") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp)
                        .testTag("dialog_content_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Pin Switch Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "An Pin-Leiste fixieren (Favorit)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it },
                        modifier = Modifier.testTag("dialog_pin_switch")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Color Selection Row
                Text(
                    text = "Hintergrundfarbe wählen",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Color list FlowLayout styled circles
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NoteColors.forEach { hex ->
                        val itemColor = parseNoteColor(hex, isDark)
                        val isSelected = hex == selectedColor

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(itemColor)
                                .clickable { selectedColor = hex }
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Besetzt",
                                    tint = if (isDark) Color.White else Color.DarkGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cancel + Save Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dialog_cancel_button")
                    ) {
                        Text("Abbrechen")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (content.isNotBlank() || title.isNotBlank()) {
                                onSave(title, content, category, selectedColor, isPinned)
                            }
                        },
                        enabled = content.isNotBlank() || title.isNotBlank(),
                        modifier = Modifier.testTag("dialog_save_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Speichern")
                    }
                }
            }
        }
    }
}
