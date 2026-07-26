package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "highlight_notes")
data class HighlightNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val docUriString: String,
    val blockIndex: Int,
    val selectedText: String,
    val noteText: String = "",
    val colorHex: String = "#FEF08A", // Default Yellow
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "open_tabs")
data class OpenTab(
    @PrimaryKey
    val uriString: String,
    val fileName: String,
    val scrollIndex: Int = 0,
    val scrollOffset: Int = 0,
    val lastAccessedTimestamp: Long = System.currentTimeMillis(),
    val isClosed: Boolean = false
)
