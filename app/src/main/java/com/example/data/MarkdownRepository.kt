package com.example.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class MarkdownRepository(
    private val context: Context,
    private val documentDao: DocumentDao,
    private val settingsDao: SettingsDao,
    private val highlightDao: HighlightDao? = null,
    private val tabDao: TabDao? = null
) {
    val allRecentDocuments: Flow<List<RecentDocument>> = documentDao.getAllRecentDocuments()
    val favoriteDocuments: Flow<List<RecentDocument>> = documentDao.getFavoriteDocuments()
    val sampleDocuments: Flow<List<RecentDocument>> = documentDao.getSampleDocuments()
    val readerSettings: Flow<ReaderSettings?> = settingsDao.getSettings()

    fun getHighlightsForDocument(docUri: String): Flow<List<HighlightNote>>? {
        return highlightDao?.getHighlightsForDocument(docUri)
    }

    suspend fun addHighlightNote(note: HighlightNote) = withContext(Dispatchers.IO) {
        highlightDao?.insertHighlight(note)
    }

    suspend fun deleteHighlight(note: HighlightNote) = withContext(Dispatchers.IO) {
        highlightDao?.deleteHighlight(note)
    }

    fun getActiveTabs(): Flow<List<OpenTab>>? {
        return tabDao?.getActiveTabs()
    }

    fun getRecentlyClosedTabs(): Flow<List<OpenTab>>? {
        return tabDao?.getRecentlyClosedTabs()
    }

    suspend fun upsertTab(tab: OpenTab) = withContext(Dispatchers.IO) {
        tabDao?.upsertTab(tab)
    }

    suspend fun closeTab(uri: String) = withContext(Dispatchers.IO) {
        tabDao?.closeTab(uri)
    }

    suspend fun reopenTab(uri: String) = withContext(Dispatchers.IO) {
        tabDao?.reopenTab(uri)
    }

    suspend fun initializeDefaultDataIfNeeded() = withContext(Dispatchers.IO) {
        val existing = documentDao.getAllRecentDocuments().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            SampleDocuments.list.forEachIndexed { index, sample ->
                val uriString = "sample://${sample.key}"
                val snippet = sample.content.lines().take(3).joinToString(" ") { it.replace("#", "").trim() }
                val doc = RecentDocument(
                    uriString = uriString,
                    fileName = sample.title,
                    fileSize = sample.content.toByteArray().size.toLong(),
                    lastOpenedTimestamp = now - (index * 600000),
                    isFavorite = index == 0,
                    snippet = snippet,
                    isSample = true,
                    sampleKey = sample.key
                )
                documentDao.insertDocument(doc)

                // Add sample tab
                tabDao?.upsertTab(
                    OpenTab(
                        uriString = uriString,
                        fileName = sample.title,
                        lastAccessedTimestamp = now - (index * 600000)
                    )
                )
            }
            // Default settings
            settingsDao.saveSettings(ReaderSettings())
        }
    }

    suspend fun loadMarkdownContent(uriString: String): String = withContext(Dispatchers.IO) {
        if (uriString.startsWith("sample://")) {
            val key = uriString.removePrefix("sample://")
            return@withContext SampleDocuments.list.find { it.key == key }?.content ?: "# Document Not Found"
        }

        try {
            val uri = Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: "Error: Unable to open file stream."
        } catch (e: Exception) {
            "Error opening document: ${e.localizedMessage}"
        }
    }

    suspend fun registerOpenedDocument(uri: Uri): RecentDocument = withContext(Dispatchers.IO) {
        val uriString = uri.toString()
        var fileName = "Document.md"
        var fileSize = 0L

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex != -1) fileName = cursor.getString(nameIndex) ?: fileName
                if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
            }
        }

        try {
            val takeFlags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (ignored: Exception) {}

        val content = loadMarkdownContent(uriString)
        val snippet = content.lines().take(3).joinToString(" ") { it.replace("#", "").trim() }.take(150)

        val existingDoc = documentDao.getDocumentByUri(uriString)
        val now = System.currentTimeMillis()

        val docToSave = RecentDocument(
            id = existingDoc?.id ?: 0,
            uriString = uriString,
            fileName = fileName,
            fileSize = if (fileSize > 0) fileSize else content.toByteArray().size.toLong(),
            lastOpenedTimestamp = now,
            isFavorite = existingDoc?.isFavorite ?: false,
            snippet = snippet,
            readingProgress = existingDoc?.readingProgress ?: 0f,
            isSample = false,
            sampleKey = null
        )

        documentDao.insertDocument(docToSave)

        // Register tab
        tabDao?.upsertTab(
            OpenTab(
                uriString = uriString,
                fileName = fileName,
                lastAccessedTimestamp = now
            )
        )

        return@withContext docToSave
    }

    suspend fun toggleFavorite(uriString: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        documentDao.setFavorite(uriString, isFavorite)
    }

    suspend fun updateReadingProgress(uriString: String, progress: Float) = withContext(Dispatchers.IO) {
        documentDao.updateProgress(uriString, progress)
    }

    suspend fun updateDocumentLastOpened(doc: RecentDocument) = withContext(Dispatchers.IO) {
        documentDao.updateDocument(doc.copy(lastOpenedTimestamp = System.currentTimeMillis()))
    }

    suspend fun deleteDocument(uriString: String) = withContext(Dispatchers.IO) {
        documentDao.deleteDocumentByUri(uriString)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        documentDao.clearNonSampleHistory()
    }

    suspend fun saveSettings(settings: ReaderSettings) = withContext(Dispatchers.IO) {
        settingsDao.saveSettings(settings)
    }
}
