package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OpenTab
import com.example.ui.theme.ReadingPalette

@Composable
fun TabsBar(
    tabs: List<OpenTab>,
    currentUri: String,
    recentlyClosedTabs: List<OpenTab>,
    palette: ReadingPalette,
    onTabSelected: (tab: OpenTab) -> Unit,
    onCloseTab: (tab: OpenTab) -> Unit,
    onReopenClosedTab: (tab: OpenTab) -> Unit,
    modifier: Modifier = Modifier
) {
    var showClosedMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.surface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Horizontal Scrollable Tabs List
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val isSelected = tab.uriString == currentUri

                Surface(
                    onClick = { onTabSelected(tab) },
                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
                    color = if (isSelected) palette.background else palette.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) palette.primary else palette.dividerColor
                    ),
                    modifier = Modifier.testTag("tab_${tab.fileName}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = if (isSelected) palette.primary else palette.secondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tab.fileName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) palette.onSurface else palette.secondaryText
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        if (tabs.size > 1) {
                            IconButton(
                                onClick = { onCloseTab(tab) },
                                modifier = Modifier
                                    .size(18.dp)
                                    .testTag("close_tab_${tab.fileName}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Tab",
                                    tint = palette.secondaryText,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recently Closed Tabs History Dropdown Button
        if (recentlyClosedTabs.isNotEmpty()) {
            Box {
                IconButton(
                    onClick = { showClosedMenu = true },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("recently_closed_tabs_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Recently closed tabs",
                        tint = palette.secondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showClosedMenu,
                    onDismissRequest = { showClosedMenu = false }
                ) {
                    Text(
                        text = "RECENTLY CLOSED TABS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.secondaryText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    recentlyClosedTabs.forEach { tab ->
                        DropdownMenuItem(
                            text = { Text(tab.fileName, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                            onClick = {
                                showClosedMenu = false
                                onReopenClosedTab(tab)
                            }
                        )
                    }
                }
            }
        }
    }
}
