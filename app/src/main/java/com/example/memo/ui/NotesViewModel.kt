package com.example.memo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.memo.data.Note
import com.example.memo.data.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(private val repository: NoteRepository) : ViewModel() {
    val notes = repository.notes.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun save(id: Long, title: String, content: String, reminderAt: Long?) = viewModelScope.launch {
        if (title.isNotBlank() || content.isNotBlank()) {
            repository.save(id, title, content, reminderAt)
        }
    }

    fun delete(note: Note) = viewModelScope.launch { repository.delete(note) }
}

class NotesViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return NotesViewModel(repository) as T
    }
}
