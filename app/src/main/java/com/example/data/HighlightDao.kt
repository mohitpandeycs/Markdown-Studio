package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HighlightDao {
    @Query("SELECT * FROM highlight_notes WHERE docUriString = :docUri ORDER BY blockIndex ASC, createdTimestamp DESC")
    fun getHighlightsForDocument(docUri: String): Flow<List<HighlightNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: HighlightNote): Long

    @Delete
    suspend fun deleteHighlight(highlight: HighlightNote)

    @Query("DELETE FROM highlight_notes WHERE id = :id")
    suspend fun deleteHighlightById(id: Long)
}

@Dao
interface TabDao {
    @Query("SELECT * FROM open_tabs WHERE isClosed = 0 ORDER BY lastAccessedTimestamp DESC")
    fun getActiveTabs(): Flow<List<OpenTab>>

    @Query("SELECT * FROM open_tabs WHERE isClosed = 1 ORDER BY lastAccessedTimestamp DESC LIMIT 10")
    fun getRecentlyClosedTabs(): Flow<List<OpenTab>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTab(tab: OpenTab)

    @Query("UPDATE open_tabs SET isClosed = 1 WHERE uriString = :uri")
    suspend fun closeTab(uri: String)

    @Query("UPDATE open_tabs SET isClosed = 0, lastAccessedTimestamp = :now WHERE uriString = :uri")
    suspend fun reopenTab(uri: String, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM open_tabs WHERE uriString = :uri")
    suspend fun deleteTab(uri: String)
}
