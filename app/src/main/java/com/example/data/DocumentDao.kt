package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM recent_documents ORDER BY isFavorite DESC, lastOpenedTimestamp DESC")
    fun getAllRecentDocuments(): Flow<List<RecentDocument>>

    @Query("SELECT * FROM recent_documents WHERE isFavorite = 1 ORDER BY lastOpenedTimestamp DESC")
    fun getFavoriteDocuments(): Flow<List<RecentDocument>>

    @Query("SELECT * FROM recent_documents WHERE isSample = 1 ORDER BY id ASC")
    fun getSampleDocuments(): Flow<List<RecentDocument>>

    @Query("SELECT * FROM recent_documents WHERE uriString = :uri LIMIT 1")
    suspend fun getDocumentByUri(uri: String): RecentDocument?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: RecentDocument): Long

    @Update
    suspend fun updateDocument(doc: RecentDocument)

    @Query("DELETE FROM recent_documents WHERE uriString = :uri")
    suspend fun deleteDocumentByUri(uri: String)

    @Query("DELETE FROM recent_documents WHERE isSample = 0")
    suspend fun clearNonSampleHistory()

    @Query("UPDATE recent_documents SET isFavorite = :isFav WHERE uriString = :uri")
    suspend fun setFavorite(uri: String, isFav: Boolean)

    @Query("UPDATE recent_documents SET readingProgress = :progress WHERE uriString = :uri")
    suspend fun updateProgress(uri: String, progress: Float)
}
