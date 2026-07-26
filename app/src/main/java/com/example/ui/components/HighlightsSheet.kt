package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HighlightNote
import com.example.ui.theme.ReadingPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighlightsSheet(
    highlights: List<HighlightNote>,
    sheetState: SheetState,
    palette: ReadingPalette,
    onAddHighlightNote: (selectedText: String, noteText: String, colorHex: String) -> Unit,
    onDeleteHighlight: (highlight: HighlightNote) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var selectedColorHex by remember { mutableStateOf("#FEF08A") } // Yellow default
    var textInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }

    val highlightColors = listOf(
        Pair("Yellow", "#FEF08A"),
        Pair("Green", "#BBF7D0"),
        Pair("Blue", "#BFDBFE"),
        Pair("Pink", "#FBCFE8"),
        Pair("Purple", "#E9D5FF")
    )

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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Highlights & Study Notes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.onSurface
                        )
                        Text(
                            text = "${highlights.size} saved annotations",
                            fontSize = 12.sp,
                            color = palette.secondaryText
                        )
                    }
                }

                Button(
                    onClick = { showAddNoteDialog = !showAddNoteDialog },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_highlight_button")
                ) {
                    Icon(Icons.Default.AddComment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Note", fontSize = 12.sp)
                }
            }
            HorizontalDivider(color = palette.dividerColor, modifier = Modifier.padding(vertical = 12.dp))

            // Add Note Creation Box
            if (showAddNoteDialog) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = palette.background,
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "NEW ANNOTATION / HIGHLIGHT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Highlighted text or sentence...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = noteInput,
                            onValueChange = { noteInput = it },
                            placeholder = { Text("Your study note / thoughts...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Color selection
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Color:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.secondaryText)
                            highlightColors.forEach { (name, hex) ->
                                val colorVal = Color(android.graphics.Color.parseColor(hex))
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(colorVal, CircleShape)
                                        .border(
                                            width = if (selectedColorHex == hex) 2.dp else 0.dp,
                                            color = palette.primary,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColorHex = hex }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    onAddHighlightNote(textInput, noteInput, selectedColorHex)
                                    textInput = ""
                                    noteInput = ""
                                    showAddNoteDialog = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Highlight Note")
                        }
                    }
                }
            }

            // Highlights List
            if (highlights.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No highlights or study notes added yet.",
                        color = palette.secondaryText,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(highlights) { item ->
                        val highlightColor = remember(item.colorHex) {
                            try {
                                Color(android.graphics.Color.parseColor(item.colorHex))
                            } catch (e: Exception) {
                                Color(0xFFFEF08A)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = palette.background,
                            border = androidx.compose.foundation.BorderStroke(1.dp, palette.dividerColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(36.dp)
                                        .background(highlightColor, RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "\"${item.selectedText}\"",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = palette.onSurface,
                                        modifier = Modifier
                                            .background(highlightColor.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    if (item.noteText.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.noteText,
                                            fontSize = 13.sp,
                                            color = palette.secondaryText
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { onDeleteHighlight(item) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete highlight",
                                        tint = palette.secondaryText,
                                        modifier = Modifier.size(16.dp)
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
