package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reader_settings")
data class ReaderSettings(
    @PrimaryKey
    val id: Int = 1,
    val themeName: String = "Light",
    val fontSizeSp: Int = 16,
    val fontFamily: String = "SansSerif",
    val lineSpacingMultiplier: Float = 1.3f,
    val showLineNumbers: Boolean = false,
    val enableAutoToc: Boolean = true,
    val appThemeMode: String = "SYSTEM"
)
