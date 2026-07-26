package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parser.ReadingStats
import com.example.ui.theme.ReadingPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsSheet(
    stats: ReadingStats,
    sheetState: SheetState,
    palette: ReadingPalette,
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
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Document Reading Analytics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.onSurface
                    )
                    Text(
                        text = "Real-time parsing and complexity metrics",
                        fontSize = 12.sp,
                        color = palette.secondaryText
                    )
                }
            }
            HorizontalDivider(color = palette.dividerColor)

            Spacer(modifier = Modifier.height(16.dp))

            val statCards = listOf(
                StatCardData("Words", "${stats.wordCount}", Icons.Default.TextFields),
                StatCardData("Characters", "${stats.charCount}", Icons.Default.TextFields),
                StatCardData("Est. Reading", "${stats.readingTimeMinutes} min", Icons.Default.Schedule),
                StatCardData("Headings", "${stats.headingCount}", Icons.Default.Title),
                StatCardData("Code Blocks", "${stats.codeBlockCount}", Icons.Default.Code),
                StatCardData("Images", "${stats.imageCount}", Icons.Default.Image),
                StatCardData("Hyperlinks", "${stats.linkCount}", Icons.Default.Link),
                StatCardData("Data Tables", "${stats.tableCount}", Icons.Default.TableChart),
                StatCardData("Math Equations", "${stats.mathCount}", Icons.Default.Functions),
                StatCardData("Mermaid Diagrams", "${stats.diagramCount}", Icons.Default.AccountTree)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                items(statCards.size) { index ->
                    val item = statCards[index]
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = palette.background,
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.dividerColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = palette.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    color = palette.secondaryText
                                )
                                Text(
                                    text = item.value,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class StatCardData(
    val label: String,
    val value: String,
    val icon: ImageVector
)
