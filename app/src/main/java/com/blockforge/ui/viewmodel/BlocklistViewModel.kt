package com.blockforge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blockforge.data.database.BlockedPrefix
import com.blockforge.data.repository.BlocklistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing the blocklist
 * Handles business logic and communicates with repository
 */
@HiltViewModel
class BlocklistViewModel @Inject constructor(
    private val repository: BlocklistRepository
) : ViewModel() {

    /**
     * List of all blocked prefixes
     * Automatically updates when database changes
     */
    val blockedPrefixes: StateFlow<List<BlockedPrefix>> = repository.getAllPrefixes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Add a new prefix to the blocklist
     */
    fun addPrefix(prefix: String, description: String? = null) {
        viewModelScope.launch {
            // Validate prefix is not empty
            if (prefix.isBlank()) return@launch

            // Check if already exists
            if (repository.prefixExists(prefix)) {
                // TODO: Show error to user
                return@launch
            }

            // Add to database
            repository.addPrefix(prefix.trim(), description?.trim())
        }
    }

    /**
     * Delete a prefix from the blocklist
     */
    fun deletePrefix(prefix: BlockedPrefix) {
        viewModelScope.launch {
            repository.deletePrefix(prefix)
        }
    }
}
