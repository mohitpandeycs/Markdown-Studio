package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ReaderSettings
import com.example.parser.TocItem
import com.example.ui.theme.ReadingPalette
import com.example.ui.theme.ReadingThemes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableOfContentsSheet(
    tocList: List<TocItem>,
    sheetState: SheetState,
    palette: ReadingPalette,
    onTocItemClick: (blockIndex: Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surface,
        contentColor = palette.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FormatListBulleted,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Table of Contents",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.onSurface
                )
            }
            HorizontalDivider(color = palette.dividerColor)

            if (tocList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No headings found in this document.",
                        color = palette.secondaryText,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 24.dp)
                ) {
                    items(tocList) { item ->
                        val indent = ((item.level - 1) * 16).dp
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onTocItemClick(item.blockIndex)
                                    onDismiss()
                                }
                                .padding(start = indent, top = 8.dp, bottom = 8.dp, end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "H${item.level}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.primary,
                                modifier = Modifier
                                    .background(palette.primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = item.title,
                                fontSize = 15.sp,
                                fontWeight = if (item.level <= 2) FontWeight.SemiBold else FontWeight.Normal,
                                color = palette.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReaderThemeSettingsSheet(
    settings: ReaderSettings,
    currentPalette: ReadingPalette,
    sheetState: SheetState,
    onThemeSelected: (themeName: String) -> Unit,
    onFontSizeChanged: (newSp: Int) -> Unit,
    onFontFamilyChanged: (fontFamily: String) -> Unit,
    onLineSpacingChanged: (multiplier: Float) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = currentPalette.surface,
        contentColor = currentPalette.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = currentPalette.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Reader Appearance & Theme",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = currentPalette.onSurface
                )
            }
            HorizontalDivider(color = currentPalette.dividerColor)

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Reading Theme Selection
            Text(
                text = "READING THEME",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = currentPalette.secondaryText
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ReadingThemes.allPalettes.forEach { palette ->
                    val isSelected = palette.name.equals(currentPalette.name, ignoreCase = true)
                    Surface(
                        onClick = { onThemeSelected(palette.name) },
                        shape = RoundedCornerShape(20.dp),
                        color = palette.background,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) palette.primary else palette.tableBorder
                        ),
                        modifier = Modifier
                            .testTag("theme_chip_${palette.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(palette.primary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = palette.name,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = palette.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Font Size Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FONT SIZE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = currentPalette.secondaryText
                )
                Text(
                    text = "${settings.fontSizeSp} sp",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = currentPalette.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Slider(
                value = settings.fontSizeSp.toFloat(),
                onValueChange = { onFontSizeChanged(it.toInt()) },
                valueRange = 12f..28f,
                steps = 15,
                colors = SliderDefaults.colors(
                    thumbColor = currentPalette.primary,
                    activeTrackColor = currentPalette.primary,
                    inactiveTrackColor = currentPalette.dividerColor
                ),
                modifier = Modifier.testTag("font_size_slider")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Font Family Selector
            Text(
                text = "TYPOGRAPHY FONT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = currentPalette.secondaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("SansSerif", "Serif", "Monospace", "Cursive").forEach { fontName ->
                    val isSelected = settings.fontFamily.equals(fontName, ignoreCase = true)
                    Surface(
                        onClick = { onFontFamilyChanged(fontName) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) currentPalette.primary else currentPalette.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, currentPalette.dividerColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (fontName) {
                                    "SansSerif" -> "Sans"
                                    "Monospace" -> "Mono"
                                    else -> fontName
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = ReadingThemes.getFontFamily(fontName),
                                color = if (isSelected) Color.White else currentPalette.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
