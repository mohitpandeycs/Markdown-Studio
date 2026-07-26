package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HighlightNote
import com.example.data.MarkdownRepository
import com.example.data.OpenTab
import com.example.data.RecentDocument
import com.example.data.ReaderSettings
import com.example.parser.MarkdownBlock
import com.example.parser.MarkdownParser
import com.example.parser.ParsedMarkdown
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ReaderViewMode {
    PREVIEW,
    MARKDOWN
}

data class SearchMatch(
    val blockIndex: Int,
    val textSnippet: String
)

class ReaderViewModel(application: Application) : AndroidViewModel(application) {

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
    }

    private val _viewMode = MutableStateFlow(ReaderViewMode.PREVIEW)
    val viewMode: StateFlow<ReaderViewMode> = _viewMode.asStateFlow()

    fun setViewMode(mode: ReaderViewMode) {
        _viewMode.value = mode
    }

    private val _currentDoc = MutableStateFlow<RecentDocument?>(null)
    val currentDoc: StateFlow<RecentDocument?> = _currentDoc.asStateFlow()

    private val _rawContent = MutableStateFlow("")
    val rawContent: StateFlow<String> = _rawContent.asStateFlow()

    private val _parsedMarkdown = MutableStateFlow<ParsedMarkdown?>(null)
    val parsedMarkdown: StateFlow<ParsedMarkdown?> = _parsedMarkdown.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Tabs
    val activeTabs: StateFlow<List<OpenTab>> = repository.getActiveTabs()
        ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        ?: MutableStateFlow(emptyList())

    val recentlyClosedTabs: StateFlow<List<OpenTab>> = repository.getRecentlyClosedTabs()
        ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        ?: MutableStateFlow(emptyList())

    // Highlights
    val highlights: StateFlow<List<HighlightNote>> = _currentDoc.flatMapLatest { doc ->
        if (doc != null) {
            repository.getHighlightsForDocument(doc.uriString) ?: flowOf(emptyList())
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search state within file
    val isSearchActive = MutableStateFlow(false)
    val searchInFileQuery = MutableStateFlow("")
    private val _searchResults = MutableStateFlow<List<SearchMatch>>(emptyList())
    val searchResults: StateFlow<List<SearchMatch>> = _searchResults.asStateFlow()
    val currentMatchIndex = MutableStateFlow(0)

    // Sheets visibility
    val isTocVisible = MutableStateFlow(false)
    val isThemeSheetVisible = MutableStateFlow(false)
    val isStatsVisible = MutableStateFlow(false)
    val isFolderVisible = MutableStateFlow(false)
    val isHighlightsVisible = MutableStateFlow(false)
    val isSplitViewVisible = MutableStateFlow(false)

    val readerSettings: StateFlow<ReaderSettings> = repository.readerSettings
        .combine(MutableStateFlow(ReaderSettings())) { dbSettings, defaultSettings ->
            dbSettings ?: defaultSettings
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReaderSettings())

    fun loadDocument(uriString: String, titleFallback: String = "Document.md") {
        viewModelScope.launch {
            _isLoading.value = true
            val content = repository.loadMarkdownContent(uriString)
            _rawContent.value = content
            val parsed = MarkdownParser.parse(content)
            _parsedMarkdown.value = parsed

            val doc = RecentDocument(
                uriString = uriString,
                fileName = titleFallback,
                fileSize = content.toByteArray().size.toLong(),
                lastOpenedTimestamp = System.currentTimeMillis(),
                snippet = content.lines().take(3).joinToString(" ") { it.replace("#", "").trim() }.take(150)
            )
            _currentDoc.value = doc
            repository.updateDocumentLastOpened(doc)

            // Register/update open tab
            repository.upsertTab(
                OpenTab(
                    uriString = uriString,
                    fileName = titleFallback,
                    lastAccessedTimestamp = System.currentTimeMillis(),
                    isClosed = false
                )
            )

            _isLoading.value = false
        }
    }

    fun updateRawContentFromSplitEditor(newRaw: String) {
        _rawContent.value = newRaw
        _parsedMarkdown.value = MarkdownParser.parse(newRaw)
    }

    fun addHighlightNote(selectedText: String, noteText: String, colorHex: String, blockIndex: Int = 0) {
        val doc = _currentDoc.value ?: return
        viewModelScope.launch {
            repository.addHighlightNote(
                HighlightNote(
                    docUriString = doc.uriString,
                    blockIndex = blockIndex,
                    selectedText = selectedText,
                    noteText = noteText,
                    colorHex = colorHex
                )
            )
        }
    }

    fun deleteHighlight(highlight: HighlightNote) {
        viewModelScope.launch {
            repository.deleteHighlight(highlight)
        }
    }

    fun closeTab(tab: OpenTab) {
        viewModelScope.launch {
            repository.closeTab(tab.uriString)
        }
    }

    fun reopenClosedTab(tab: OpenTab) {
        viewModelScope.launch {
            repository.reopenTab(tab.uriString)
            loadDocument(tab.uriString, tab.fileName)
        }
    }

    fun onSearchInFileQueryChanged(query: String) {
        searchInFileQuery.value = query
        val parsed = _parsedMarkdown.value
        if (query.isBlank() || parsed == null) {
            _searchResults.value = emptyList()
            currentMatchIndex.value = 0
            return
        }

        val matches = mutableListOf<SearchMatch>()
        parsed.blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    if (block.text.contains(query, ignoreCase = true)) {
                        matches.add(SearchMatch(block.idx, block.text))
                    }
                }
                is MarkdownBlock.Paragraph -> {
                    val full = block.runs.joinToString("") { it.text }
                    if (full.contains(query, ignoreCase = true)) {
                        matches.add(SearchMatch(block.idx, full))
                    }
                }
                is MarkdownBlock.CodeBlock -> {
                    if (block.code.contains(query, ignoreCase = true)) {
                        matches.add(SearchMatch(block.idx, block.code.take(60)))
                    }
                }
                is MarkdownBlock.BlockQuote -> {
                    if (block.text.contains(query, ignoreCase = true)) {
                        matches.add(SearchMatch(block.idx, block.text))
                    }
                }
                is MarkdownBlock.UnorderedList -> {
                    block.items.forEach { item ->
                        val text = item.joinToString("") { it.text }
                        if (text.contains(query, ignoreCase = true)) {
                            matches.add(SearchMatch(block.idx, text))
                        }
                    }
                }
                is MarkdownBlock.OrderedList -> {
                    block.items.forEach { item ->
                        val text = item.second.joinToString("") { it.text }
                        if (text.contains(query, ignoreCase = true)) {
                            matches.add(SearchMatch(block.idx, text))
                        }
                    }
                }
                is MarkdownBlock.TaskList -> {
                    block.items.forEach { item ->
                        val text = item.runs.joinToString("") { it.text }
                        if (text.contains(query, ignoreCase = true)) {
                            matches.add(SearchMatch(block.idx, text))
                        }
                    }
                }
                else -> {}
            }
        }

        _searchResults.value = matches
        currentMatchIndex.value = if (matches.isNotEmpty()) 0 else 0
    }

    fun nextSearchMatch() {
        val total = _searchResults.value.size
        if (total > 0) {
            currentMatchIndex.value = (currentMatchIndex.value + 1) % total
        }
    }

    fun previousSearchMatch() {
        val total = _searchResults.value.size
        if (total > 0) {
            currentMatchIndex.value = (currentMatchIndex.value - 1 + total) % total
        }
    }

    fun toggleTaskItemChecked(blockIndex: Int, taskItemIndex: Int) {
        val currentParsed = _parsedMarkdown.value ?: return
        val newBlocks = currentParsed.blocks.map { block ->
            if (block is MarkdownBlock.TaskList && block.idx == blockIndex) {
                val updatedItems = block.items.mapIndexed { idx, item ->
                    if (idx == taskItemIndex) {
                        item.copy(checked = !item.checked)
                    } else item
                }
                block.copy(items = updatedItems)
            } else block
        }
        _parsedMarkdown.value = currentParsed.copy(blocks = newBlocks)
    }

    fun toggleFavorite() {
        val doc = _currentDoc.value ?: return
        val newFav = !doc.isFavorite
        _currentDoc.value = doc.copy(isFavorite = newFav)
        viewModelScope.launch {
            repository.toggleFavorite(doc.uriString, newFav)
        }
    }

    fun updateProgress(progress: Float) {
        val doc = _currentDoc.value ?: return
        if (Math.abs(doc.readingProgress - progress) > 0.05f) {
            _currentDoc.value = doc.copy(readingProgress = progress)
            viewModelScope.launch {
                repository.updateReadingProgress(doc.uriString, progress)
            }
        }
    }

    fun updateTheme(themeName: String) {
        val current = readerSettings.value
        val updated = current.copy(themeName = themeName)
        viewModelScope.launch {
            repository.saveSettings(updated)
        }
    }

    fun updateFontSize(newSizeSp: Int) {
        val current = readerSettings.value
        val updated = current.copy(fontSizeSp = newSizeSp)
        viewModelScope.launch {
            repository.saveSettings(updated)
        }
    }

    fun updateFontFamily(fontFamily: String) {
        val current = readerSettings.value
        val updated = current.copy(fontFamily = fontFamily)
        viewModelScope.launch {
            repository.saveSettings(updated)
        }
    }

    fun updateLineSpacing(multiplier: Float) {
        val current = readerSettings.value
        val updated = current.copy(lineSpacingMultiplier = multiplier)
        viewModelScope.launch {
            repository.saveSettings(updated)
        }
    }
}
