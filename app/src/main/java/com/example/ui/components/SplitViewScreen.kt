package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parser.MarkdownParser
import com.example.ui.theme.ReadingPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitViewDialog(
    initialRawContent: String,
    palette: ReadingPalette,
    baseFontSizeSp: Int,
    fontFamilyName: String,
    onSaveAndClose: (newRawContent: String) -> Unit,
    onDismiss: () -> Unit
) {
    var rawText by remember { mutableStateOf(initialRawContent) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Split View, 1: Raw Only, 2: Preview Only

    val parsedLive = remember(rawText) {
        MarkdownParser.parse(rawText)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = palette.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = palette.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Split View Editor & Preview",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = palette.surface,
                        contentColor = palette.primary,
                        modifier = Modifier.width(220.dp)
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Split", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Raw", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Preview", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(onClick = {
                        onSaveAndClose(rawText)
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = palette.onSurface)
                    }
                }
            }
            HorizontalDivider(color = palette.dividerColor)

            // Content Area
            Row(modifier = Modifier.fillMaxSize()) {
                // Raw Editor Column
                if (selectedTab == 0 || selectedTab == 1) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "RAW MARKDOWN EDITOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.secondaryText,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = rawText,
                            onValueChange = { rawText = it },
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("raw_markdown_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = palette.surface,
                                unfocusedContainerColor = palette.surface,
                                focusedTextColor = palette.onSurface,
                                unfocusedTextColor = palette.onSurface
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }

                if (selectedTab == 0) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxSize()
                            .background(palette.dividerColor)
                    )
                }

                // Rendered Preview Column
                if (selectedTab == 0 || selectedTab == 2) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "LIVE RENDER PREVIEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.primary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = palette.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, palette.dividerColor),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                itemsIndexed(parsedLive.blocks) { idx, block ->
                                    RenderMarkdownBlock(
                                        block = block,
                                        palette = palette,
                                        baseFontSizeSp = baseFontSizeSp,
                                        fontFamilyName = fontFamilyName,
                                        lineSpacingMultiplier = 1.4f,
                                        searchQuery = "",
                                        isHighlightedBlock = false,
                                        onTaskCheckedToggle = {}
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
