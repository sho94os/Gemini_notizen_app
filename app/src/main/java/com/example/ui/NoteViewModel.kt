package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Note
import com.example.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    // Filtering & Searching state
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("Alle")
    val currentSortOrder = MutableStateFlow(SortOrder.UPDATED_DESC)

    // Reactive note list flat-mapping the query flow
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val rawNotesFlow = searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            repository.allNotes
        } else {
            repository.searchNotes(query)
        }
    }

    // Combined filtered & sorted notes
    val notes: StateFlow<List<Note>> = combine(
        rawNotesFlow,
        selectedCategory,
        currentSortOrder
    ) { notes, category, sortOrder ->
        // 1. Filter by category
        val filtered = if (category == "Alle") {
            notes
        } else {
            notes.filter { it.category.equals(category, ignoreCase = true) }
        }

        // 2. Sort notes (pin is always handled first by Room, but we verify here)
        val sorted = when (sortOrder) {
            SortOrder.UPDATED_DESC -> filtered.sortedWith(
                compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt }
            )
            SortOrder.UPDATED_ASC -> filtered.sortedWith(
                compareByDescending<Note> { it.isPinned }.thenBy { it.updatedAt }
            )
            SortOrder.TITLE_ASC -> filtered.sortedWith(
                compareByDescending<Note> { it.isPinned }.thenBy { it.title.lowercase() }
            )
            SortOrder.TITLE_DESC -> filtered.sortedWith(
                compareByDescending<Note> { it.isPinned }.thenByDescending { it.title.lowercase() }
            )
        }
        sorted
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dynamically retrieve unique categories from database to present in filters
    val categories: StateFlow<List<String>> = repository.allNotes
        .combine(MutableStateFlow(listOf("Alle", "Persönlich", "Arbeit", "Ideen", "Einkauf"))) { notesList, defaults ->
            val existing = notesList.map { it.category }.filter { it.isNotBlank() }.distinct()
            (defaults + existing).distinct()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("Alle", "Persönlich", "Arbeit", "Ideen", "Einkauf")
        )

    fun addNote(title: String, content: String, category: String, colorHex: String, isPinned: Boolean = false) {
        viewModelScope.launch {
            val note = Note(
                title = title,
                content = content,
                category = category.trim().ifBlank { "Persönlich" },
                colorHex = colorHex,
                isPinned = isPinned,
                updatedAt = System.currentTimeMillis()
            )
            repository.insert(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            val updated = note.copy(updatedAt = System.currentTimeMillis())
            repository.insert(updated)
        }
    }

    fun deleteNote(noteId: Int) {
        viewModelScope.launch {
            repository.deleteById(noteId)
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            val updated = note.copy(
                isPinned = !note.isPinned,
                updatedAt = System.currentTimeMillis()
            )
            repository.insert(updated)
        }
    }

    enum class SortOrder {
        UPDATED_DESC,
        UPDATED_ASC,
        TITLE_ASC,
        TITLE_DESC
    }

    // Factory to easily pass Repository dependency to ViewModel
    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
                return NoteViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
