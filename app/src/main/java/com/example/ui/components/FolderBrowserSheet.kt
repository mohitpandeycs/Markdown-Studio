package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ReadingPalette

data class SampleFolderNode(
    val name: String,
    val isExpanded: Boolean = true,
    val files: List<SampleFileNode>
)

data class SampleFileNode(
    val fileName: String,
    val uriString: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderBrowserSheet(
    sheetState: SheetState,
    palette: ReadingPalette,
    onOpenFile: (uriString: String, fileName: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sampleFolders = remember {
        listOf(
            SampleFolderNode(
                name = "Notes",
                files = listOf(
                    SampleFileNode("AI.md", "sample://notes_ai.md"),
                    SampleFileNode("Kotlin.md", "sample://notes_kotlin.md"),
                    SampleFileNode("Linux.md", "sample://notes_linux.md")
                )
            ),
            SampleFolderNode(
                name = "Journal",
                files = listOf(
                    SampleFileNode("Daily_2026.md", "sample://journal_2026.md"),
                    SampleFileNode("Ideas.md", "sample://sample_doc.md")
                )
            ),
            SampleFolderNode(
                name = "Books & Guides",
                files = listOf(
                    SampleFileNode("Architecture.md", "sample://architecture_guide.md"),
                    SampleFileNode("README.md", "sample://sample_doc.md")
                )
            )
        )
    }

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
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Folder Explorer & Workspace",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.onSurface
                    )
                    Text(
                        text = "Browse document hierarchy and notes",
                        fontSize = 12.sp,
                        color = palette.secondaryText
                    )
                }
            }
            HorizontalDivider(color = palette.dividerColor)

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sampleFolders) { folder ->
                    var isExpanded by remember { mutableStateOf(folder.isExpanded) }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = palette.background,
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.dividerColor)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Folder Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isExpanded = !isExpanded }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = palette.secondaryText
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = palette.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = folder.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${folder.files.size} files",
                                    fontSize = 12.sp,
                                    color = palette.secondaryText
                                )
                            }

                            // Folder Files
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 24.dp, end = 12.dp, bottom = 8.dp)
                                ) {
                                    folder.files.forEach { file ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onOpenFile(file.uriString, file.fileName)
                                                    onDismiss()
                                                }
                                                .padding(vertical = 8.dp, horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Description,
                                                contentDescription = null,
                                                tint = palette.secondaryText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = file.fileName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
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
        }
    }
}
