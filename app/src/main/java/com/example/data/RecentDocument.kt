package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_documents")
data class RecentDocument(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uriString: String,
    val fileName: String,
    val fileSize: Long,
    val lastOpenedTimestamp: Long,
    val isFavorite: Boolean = false,
    val snippet: String = "",
    val readingProgress: Float = 0.0f,
    val isSample: Boolean = false,
    val sampleKey: String? = null
)
