package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

data class ReadingPalette(
    val name: String,
    val background: Color,
    val surface: Color,
    val onSurface: Color,
    val primary: Color,
    val secondaryText: Color,
    val codeBackground: Color,
    val codeText: Color,
    val quoteBorder: Color,
    val tableHeaderBg: Color,
    val tableBorder: Color,
    val dividerColor: Color,
    val searchHighlight: Color
)

object ReadingThemes {

    val Light = ReadingPalette(
        name = "Light",
        background = Color(0xFFF8FAFC),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF0F172A),
        primary = Color(0xFF2563EB),
        secondaryText = Color(0xFF64748B),
        codeBackground = Color(0xFFF1F5F9),
        codeText = Color(0xFF0F172A),
        quoteBorder = Color(0xFF2563EB),
        tableHeaderBg = Color(0xFFE2E8F0),
        tableBorder = Color(0xFFCBD5E1),
        dividerColor = Color(0xFFE2E8F0),
        searchHighlight = Color(0xFFFEF08A)
    )

    val HighContrastLight = ReadingPalette(
        name = "High Contrast Light",
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF0F0F0),
        onSurface = Color(0xFF000000),
        primary = Color(0xFF0000D0),
        secondaryText = Color(0xFF222222),
        codeBackground = Color(0xFFE0E0E0),
        codeText = Color(0xFF000000),
        quoteBorder = Color(0xFF000000),
        tableHeaderBg = Color(0xFFD0D0D0),
        tableBorder = Color(0xFF000000),
        dividerColor = Color(0xFF000000),
        searchHighlight = Color(0xFFFFFF00)
    )

    val Dark = ReadingPalette(
        name = "Dark",
        background = Color(0xFF0F172A),
        surface = Color(0xFF1E293B),
        onSurface = Color(0xFFF8FAFC),
        primary = Color(0xFF60A5FA),
        secondaryText = Color(0xFF94A3B8),
        codeBackground = Color(0xFF1E293B),
        codeText = Color(0xFF38BDF8),
        quoteBorder = Color(0xFF60A5FA),
        tableHeaderBg = Color(0xFF334155),
        tableBorder = Color(0xFF475569),
        dividerColor = Color(0xFF334155),
        searchHighlight = Color(0xFF854D0E)
    )

    val HighContrastDark = ReadingPalette(
        name = "High Contrast Dark",
        background = Color(0xFF000000),
        surface = Color(0xFF111111),
        onSurface = Color(0xFFFFFFFF),
        primary = Color(0xFFFFFF00),
        secondaryText = Color(0xFFDDDDDD),
        codeBackground = Color(0xFF222222),
        codeText = Color(0xFF00FFFF),
        quoteBorder = Color(0xFFFFFF00),
        tableHeaderBg = Color(0xFF333333),
        tableBorder = Color(0xFFFFFFFF),
        dividerColor = Color(0xFFFFFFFF),
        searchHighlight = Color(0xFFFF00FF)
    )

    val AMOLED = ReadingPalette(
        name = "AMOLED",
        background = Color(0xFF000000),
        surface = Color(0xFF121212),
        onSurface = Color(0xFFFFFFFF),
        primary = Color(0xFF38BDF8),
        secondaryText = Color(0xFFA1A1AA),
        codeBackground = Color(0xFF18181B),
        codeText = Color(0xFF38BDF8),
        quoteBorder = Color(0xFF38BDF8),
        tableHeaderBg = Color(0xFF27272A),
        tableBorder = Color(0xFF3F3F46),
        dividerColor = Color(0xFF27272A),
        searchHighlight = Color(0xFFA16207)
    )

    val Sepia = ReadingPalette(
        name = "Sepia",
        background = Color(0xFFFBF0D9),
        surface = Color(0xFFF4E5C3),
        onSurface = Color(0xFF4A3B32),
        primary = Color(0xFF8C5A2B),
        secondaryText = Color(0xFF7A685B),
        codeBackground = Color(0xFFEFE2C6),
        codeText = Color(0xFF3D2E24),
        quoteBorder = Color(0xFF8C5A2B),
        tableHeaderBg = Color(0xFFE8D7B5),
        tableBorder = Color(0xFFD3C19E),
        dividerColor = Color(0xFFE3D2B0),
        searchHighlight = Color(0xFFFDE047)
    )

    val SolarizedDark = ReadingPalette(
        name = "Solarized",
        background = Color(0xFF002B36),
        surface = Color(0xFF073642),
        onSurface = Color(0xFF839496),
        primary = Color(0xFF268BD2),
        secondaryText = Color(0xFF586E75),
        codeBackground = Color(0xFF073642),
        codeText = Color(0xFF2AA198),
        quoteBorder = Color(0xFF268BD2),
        tableHeaderBg = Color(0xFF073642),
        tableBorder = Color(0xFF586E75),
        dividerColor = Color(0xFF073642),
        searchHighlight = Color(0xFFB58900)
    )

    val Nord = ReadingPalette(
        name = "Nord",
        background = Color(0xFF2E3440),
        surface = Color(0xFF3B4252),
        onSurface = Color(0xFFECEFF4),
        primary = Color(0xFF88C0D0),
        secondaryText = Color(0xFFD8DEE9),
        codeBackground = Color(0xFF3B4252),
        codeText = Color(0xFF81A1C1),
        quoteBorder = Color(0xFF88C0D0),
        tableHeaderBg = Color(0xFF434C5E),
        tableBorder = Color(0xFF4C566A),
        dividerColor = Color(0xFF434C5E),
        searchHighlight = Color(0xFFEBCB8B)
    )

    val Forest = ReadingPalette(
        name = "Forest",
        background = Color(0xFF1A231C),
        surface = Color(0xFF243328),
        onSurface = Color(0xFFE2E8F0),
        primary = Color(0xFF4ADE80),
        secondaryText = Color(0xFF94A3B8),
        codeBackground = Color(0xFF243328),
        codeText = Color(0xFF86EFAC),
        quoteBorder = Color(0xFF4ADE80),
        tableHeaderBg = Color(0xFF2D4032),
        tableBorder = Color(0xFF3E5745),
        dividerColor = Color(0xFF2D4032),
        searchHighlight = Color(0xFFCA8A04)
    )

    val allPalettes = listOf(Light, HighContrastLight, Dark, HighContrastDark, AMOLED, Sepia, SolarizedDark, Nord, Forest)

    fun getPaletteByName(name: String): ReadingPalette {
        return allPalettes.find { it.name.equals(name, ignoreCase = true) } ?: Light
    }

    fun getFontFamily(fontName: String): FontFamily {
        return when (fontName.lowercase()) {
            "serif" -> FontFamily.Serif
            "monospace", "mono" -> FontFamily.Monospace
            "cursive" -> FontFamily.Cursive
            else -> FontFamily.SansSerif
        }
    }
}
