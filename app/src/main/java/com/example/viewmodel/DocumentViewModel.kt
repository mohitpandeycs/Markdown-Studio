package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MarkdownRepository
import com.example.data.RecentDocument
import com.example.data.ReaderSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DocumentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MarkdownRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MarkdownRepository(
            context = application.applicationContext,
            documentDao = database.documentDao(),
            settingsDao = database.settingsDao(),
            highlightDao = database.highlightDao(),
            tabDao = database.tabDao()
        )
        viewModelScope.launch {
            repository.initializeDefaultDataIfNeeded()
        }
    }

    val searchQuery = MutableStateFlow("")
    val selectedTab = MutableStateFlow(0) // 0: Recents, 1: Favorites, 2: Samples

    val recentDocuments: StateFlow<List<RecentDocument>> = repository.allRecentDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteDocuments: StateFlow<List<RecentDocument>> = repository.favoriteDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sampleDocuments: StateFlow<List<RecentDocument>> = repository.sampleDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val readerSettings: StateFlow<ReaderSettings> = repository.readerSettings
        .combine(MutableStateFlow(ReaderSettings())) { dbSettings, defaultSettings ->
            dbSettings ?: defaultSettings
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReaderSettings())

    val filteredDocuments: StateFlow<List<RecentDocument>> = combine(
        recentDocuments,
        searchQuery,
        selectedTab
    ) { docs, query, tab ->
        val tabFiltered = when (tab) {
            1 -> docs.filter { it.isFavorite }
            2 -> docs.filter { it.isSample }
            else -> docs
        }
        if (query.isBlank()) {
            tabFiltered
        } else {
            tabFiltered.filter {
                it.fileName.contains(query, ignoreCase = true) ||
                it.snippet.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(newQuery: String) {
        searchQuery.value = newQuery
    }

    fun onTabSelected(tabIndex: Int) {
        selectedTab.value = tabIndex
    }

    fun toggleFavorite(doc: RecentDocument) {
        viewModelScope.launch {
            repository.toggleFavorite(doc.uriString, !doc.isFavorite)
        }
    }

    fun deleteDocument(doc: RecentDocument) {
        viewModelScope.launch {
            repository.deleteDocument(doc.uriString)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun saveSettings(settings: ReaderSettings) {
        viewModelScope.launch {
            repository.saveSettings(settings)
        }
    }

    suspend fun registerAndGetOpenedDoc(uri: Uri): RecentDocument {
        return repository.registerOpenedDocument(uri)
    }

    fun getRepository(): MarkdownRepository = repository
}
