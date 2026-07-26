package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FolderBrowserSheet
import com.example.ui.components.HighlightsSheet
import com.example.ui.components.MarkdownSourceEditor
import com.example.ui.components.ReaderThemeSettingsSheet
import com.example.ui.components.RenderMarkdownBlock
import com.example.ui.components.StatisticsSheet
import com.example.ui.components.TableOfContentsSheet
import com.example.ui.components.TabsBar
import com.example.ui.theme.ReadingThemes
import com.example.util.PdfExportManager
import com.example.viewmodel.ReaderViewModel
import com.example.viewmodel.ReaderViewMode
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkdownReaderScreen(
    uriString: String,
    titleFallback: String,
    viewModel: ReaderViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(uriString) {
        viewModel.loadDocument(uriString, titleFallback)
    }

    val currentDoc by viewModel.currentDoc.collectAsStateWithLifecycle()
    val rawContent by viewModel.rawContent.collectAsStateWithLifecycle()
    val parsedMarkdown by viewModel.parsedMarkdown.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val settings by viewModel.readerSettings.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()

    val activeTabs by viewModel.activeTabs.collectAsStateWithLifecycle()
    val recentlyClosedTabs by viewModel.recentlyClosedTabs.collectAsStateWithLifecycle()
    val highlights by viewModel.highlights.collectAsStateWithLifecycle()

    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val searchInFileQuery by viewModel.searchInFileQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val currentMatchIndex by viewModel.currentMatchIndex.collectAsStateWithLifecycle()

    val isTocVisible by viewModel.isTocVisible.collectAsStateWithLifecycle()
    val isThemeSheetVisible by viewModel.isThemeSheetVisible.collectAsStateWithLifecycle()
    val isStatsVisible by viewModel.isStatsVisible.collectAsStateWithLifecycle()
    val isFolderVisible by viewModel.isFolderVisible.collectAsStateWithLifecycle()
    val isHighlightsVisible by viewModel.isHighlightsVisible.collectAsStateWithLifecycle()

    var isOverflowMenuExpanded by remember { mutableStateOf(false) }
    var quickHighlightText by remember { mutableStateOf<String?>(null) }
    var isFullScreenReading by remember { mutableStateOf(false) }
    var showOverlayControlsInFullScreen by remember { mutableStateOf(false) }

    val palette = remember(settings.themeName) {
        ReadingThemes.getPaletteByName(settings.themeName)
    }

    val listState = rememberLazyListState()
    val tocSheetState = rememberModalBottomSheetState()
    val themeSheetState = rememberModalBottomSheetState()
    val statsSheetState = rememberModalBottomSheetState()
    val folderSheetState = rememberModalBottomSheetState()
    val highlightsSheetState = rememberModalBottomSheetState()

    // Reading Progress calculation
    val readingProgress by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            if (totalItems == 0) 0f
            else {
                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                (lastVisible.toFloat() + 1) / totalItems.toFloat()
            }
        }
    }

    LaunchedEffect(readingProgress) {
        if (viewMode == ReaderViewMode.PREVIEW) {
            viewModel.updateProgress(readingProgress)
        }
    }

    // Scroll to active search result match
    LaunchedEffect(currentMatchIndex, searchResults) {
        if (searchResults.isNotEmpty() && currentMatchIndex in searchResults.indices) {
            val targetBlockIdx = searchResults[currentMatchIndex].blockIndex
            listState.animateScrollToItem(targetBlockIdx)
        }
    }

    // PDF Export SAF Launcher
    val pdfExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { targetUri ->
        if (targetUri != null && parsedMarkdown != null) {
            scope.launch {
                val docName = currentDoc?.fileName ?: titleFallback
                val pdfFile = PdfExportManager.exportToPdf(
                    context = context,
                    documentTitle = docName,
                    parsed = parsedMarkdown!!,
                    targetUri = targetUri
                )
                if (pdfFile != null) {
                    Toast.makeText(context, "PDF successfully exported!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to export PDF", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = palette.background
    ) {
        Scaffold(
            topBar = {
                if (!isFullScreenReading || showOverlayControlsInFullScreen) {
                    Column {
                        TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = currentDoc?.fileName ?: titleFallback,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                parsedMarkdown?.let { parsed ->
                                    Text(
                                        text = "${parsed.readingTimeMinutes} min read • ${parsed.wordCount} words",
                                        fontSize = 11.sp,
                                        color = palette.secondaryText
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier.testTag("reader_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = palette.onSurface
                                )
                            }
                        },
                        actions = {
                            // Folder Explorer Button
                            IconButton(
                                onClick = { viewModel.isFolderVisible.value = true },
                                modifier = Modifier.testTag("folder_explorer_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = "Folder Explorer",
                                    tint = palette.onSurface
                                )
                            }

                            // Search in file toggle
                            IconButton(
                                onClick = { viewModel.isSearchActive.value = !isSearchActive },
                                modifier = Modifier.testTag("reader_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search in file",
                                    tint = if (isSearchActive) palette.primary else palette.onSurface
                                )
                            }

                            // Fullscreen Reading Mode Button
                            if (viewMode == ReaderViewMode.PREVIEW) {
                                IconButton(
                                    onClick = {
                                        isFullScreenReading = !isFullScreenReading
                                        showOverlayControlsInFullScreen = false
                                    },
                                    modifier = Modifier.testTag("fullscreen_reading_button")
                                ) {
                                    Icon(
                                        imageVector = if (isFullScreenReading) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                        contentDescription = "Full-Screen Reading Mode",
                                        tint = palette.primary
                                    )
                                }
                            }

                            // Overflow Menu Toggle
                            IconButton(
                                onClick = { isOverflowMenuExpanded = true },
                                modifier = Modifier.testTag("reader_overflow_menu")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Options",
                                    tint = palette.onSurface
                                )
                            }

                            DropdownMenu(
                                expanded = isOverflowMenuExpanded,
                                onDismissRequest = { isOverflowMenuExpanded = false },
                                modifier = Modifier.background(palette.surface)
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isFullScreenReading) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                                contentDescription = null,
                                                tint = palette.primary
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(if (isFullScreenReading) "Exit Full-Screen Mode" else "Full-Screen Reading Mode")
                                        }
                                    },
                                    onClick = {
                                        isFullScreenReading = !isFullScreenReading
                                        showOverlayControlsInFullScreen = false
                                        isOverflowMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (currentDoc?.isFavorite == true) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = null,
                                                tint = if (currentDoc?.isFavorite == true) Color(0xFFEAB308) else palette.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(if (currentDoc?.isFavorite == true) "Remove Favorite" else "Mark Favorite")
                                        }
                                    },
                                    onClick = {
                                        viewModel.toggleFavorite()
                                        isOverflowMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Analytics, contentDescription = null, tint = palette.onSurface)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text("Reading Statistics")
                                        }
                                    },
                                    onClick = {
                                        viewModel.isStatsVisible.value = true
                                        isOverflowMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = palette.onSurface)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text("Export as PDF")
                                        }
                                    },
                                    onClick = {
                                        val defaultName = "${(currentDoc?.fileName ?: titleFallback).removeSuffix(".md")}.pdf"
                                        pdfExportLauncher.launch(defaultName)
                                        isOverflowMenuExpanded = false
                                    }
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = palette.surface)
                    )

                    // Multi-Tab Bar Navigation
                    if (activeTabs.isNotEmpty()) {
                        TabsBar(
                            tabs = activeTabs,
                            currentUri = currentDoc?.uriString ?: uriString,
                            recentlyClosedTabs = recentlyClosedTabs,
                            palette = palette,
                            onTabSelected = { selectedTab ->
                                viewModel.loadDocument(selectedTab.uriString, selectedTab.fileName)
                            },
                            onCloseTab = { tabToClose ->
                                viewModel.closeTab(tabToClose)
                            },
                            onReopenClosedTab = { tabToReopen ->
                                viewModel.reopenClosedTab(tabToReopen)
                            }
                        )
                    }

                    // Linear Reading Progress Indicator Bar
                    if (viewMode == ReaderViewMode.PREVIEW) {
                        LinearProgressIndicator(
                            progress = { readingProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = palette.primary,
                            trackColor = palette.dividerColor
                        )
                    }

                    // Expandable In-File Search Bar
                    AnimatedVisibility(
                        visible = isSearchActive,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        Surface(
                            color = palette.surface,
                            tonalElevation = 4.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = searchInFileQuery,
                                    onValueChange = { viewModel.onSearchInFileQueryChanged(it) },
                                    placeholder = { Text("Find text in document...") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = palette.background,
                                        unfocusedContainerColor = palette.background,
                                        focusedTextColor = palette.onSurface,
                                        unfocusedTextColor = palette.onSurface
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("search_in_file_input")
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                if (searchResults.isNotEmpty()) {
                                    Text(
                                        text = "${currentMatchIndex + 1}/${searchResults.size}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.primary
                                    )
                                    IconButton(onClick = { viewModel.previousSearchMatch() }) {
                                        Icon(Icons.Default.ChevronLeft, contentDescription = "Prev match", tint = palette.onSurface)
                                    }
                                    IconButton(onClick = { viewModel.nextSearchMatch() }) {
                                        Icon(Icons.Default.ChevronRight, contentDescription = "Next match", tint = palette.onSurface)
                                    }
                                }

                                IconButton(onClick = {
                                    viewModel.isSearchActive.value = false
                                    viewModel.onSearchInFileQueryChanged("")
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close search", tint = palette.secondaryText)
                                }
                            }
                        }
                    }
                }
            }
        },
            bottomBar = {
                if (!isFullScreenReading || showOverlayControlsInFullScreen) {
                    BottomAppBar(
                        containerColor = palette.surface,
                        contentColor = palette.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mode Switcher Segmented Control
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier.testTag("view_mode_segmented_button")
                            ) {
                                SegmentedButton(
                                    selected = viewMode == ReaderViewMode.PREVIEW,
                                    onClick = { viewModel.setViewMode(ReaderViewMode.PREVIEW) },
                                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                    colors = SegmentedButtonDefaults.colors(
                                        activeContainerColor = palette.primary,
                                        activeContentColor = Color.White,
                                        inactiveContainerColor = palette.background,
                                        inactiveContentColor = palette.onSurface
                                    )
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Preview", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                SegmentedButton(
                                    selected = viewMode == ReaderViewMode.MARKDOWN,
                                    onClick = { viewModel.setViewMode(ReaderViewMode.MARKDOWN) },
                                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                    colors = SegmentedButtonDefaults.colors(
                                        activeContainerColor = palette.primary,
                                        activeContentColor = Color.White,
                                        inactiveContainerColor = palette.background,
                                        inactiveContentColor = palette.onSurface
                                    )
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Code,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Markdown", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Frequently Used Reading Action Icons
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Table of Contents
                                IconButton(
                                    onClick = { viewModel.isTocVisible.value = true },
                                    modifier = Modifier.testTag("reader_toc_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FormatListBulleted,
                                        contentDescription = "Table of Contents",
                                        tint = palette.onSurface
                                    )
                                }

                                // Themes & Fonts
                                IconButton(
                                    onClick = { viewModel.isThemeSheetVisible.value = true },
                                    modifier = Modifier.testTag("reader_theme_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = "Themes & Typography",
                                        tint = palette.primary
                                    )
                                }

                                // Highlights & Notes
                                IconButton(
                                    onClick = { viewModel.isHighlightsVisible.value = true },
                                    modifier = Modifier.testTag("reader_highlights_button")
                                ) {
                                    if (highlights.isNotEmpty()) {
                                        BadgedBox(badge = {
                                            Surface(
                                                shape = CircleShape,
                                                color = palette.primary,
                                                modifier = Modifier.size(16.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "${highlights.size}",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.EditNote,
                                                contentDescription = "Highlights & Notes",
                                                tint = palette.onSurface
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.EditNote,
                                            contentDescription = "Highlights & Notes",
                                            tint = palette.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                if (isFullScreenReading) {
                    FloatingActionButton(
                        onClick = {
                            isFullScreenReading = false
                            showOverlayControlsInFullScreen = false
                        },
                        containerColor = palette.primary,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("exit_fullscreen_fab")
                    ) {
                        Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Fullscreen")
                    }
                } else if (viewMode == ReaderViewMode.PREVIEW && listState.firstVisibleItemIndex > 2) {
                    FloatingActionButton(
                        onClick = {
                            scope.launch { listState.animateScrollToItem(0) }
                        },
                        containerColor = palette.primary,
                        contentColor = Color.White,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .testTag("scroll_to_top_fab")
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Scroll to top")
                    }
                }
            },
            containerColor = palette.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isFullScreenReading && !showOverlayControlsInFullScreen) androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding)
                    .clickable(enabled = isFullScreenReading) {
                        showOverlayControlsInFullScreen = !showOverlayControlsInFullScreen
                    }
            ) {
                if (isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = palette.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading Markdown document...",
                            color = palette.secondaryText,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    when (viewMode) {
                        ReaderViewMode.PREVIEW -> {
                            if (parsedMarkdown == null || parsedMarkdown!!.blocks.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Empty or unreadable Markdown document.",
                                        color = palette.secondaryText
                                    )
                                }
                            } else {
                                SelectionContainer {
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 20.dp, vertical = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        itemsIndexed(
                                            items = parsedMarkdown!!.blocks,
                                            key = { _, block -> block.blockIndex }
                                        ) { _, block ->
                                            val isMatchedBlock = searchResults.getOrNull(currentMatchIndex)?.blockIndex == block.blockIndex

                                            RenderMarkdownBlock(
                                                block = block,
                                                palette = palette,
                                                baseFontSizeSp = settings.fontSizeSp,
                                                fontFamilyName = settings.fontFamily,
                                                lineSpacingMultiplier = settings.lineSpacingMultiplier,
                                                searchQuery = if (isSearchActive) searchInFileQuery else "",
                                                isHighlightedBlock = isMatchedBlock,
                                                highlights = highlights,
                                                onTaskCheckedToggle = { taskIdx ->
                                                    viewModel.toggleTaskItemChecked(block.blockIndex, taskIdx)
                                                },
                                                onQuickHighlightText = { snippet ->
                                                    quickHighlightText = snippet
                                                }
                                            )
                                        }

                                        item {
                                            Spacer(modifier = Modifier.height(32.dp))
                                        }
                                    }
                                }
                            }
                        }

                        ReaderViewMode.MARKDOWN -> {
                            MarkdownSourceEditor(
                                rawText = rawContent,
                                palette = palette,
                                baseFontSizeSp = settings.fontSizeSp,
                                onContentChange = { newRaw ->
                                    viewModel.updateRawContentFromSplitEditor(newRaw)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Quick Context Highlight Creation Dialog
        if (quickHighlightText != null) {
            var noteText by remember { mutableStateOf("") }
            var selectedColorHex by remember { mutableStateOf("#FEF08A") }
            val highlightColors = listOf(
                Pair("Yellow", "#FEF08A"),
                Pair("Green", "#BBF7D0"),
                Pair("Blue", "#BFDBFE"),
                Pair("Pink", "#FBCFE8"),
                Pair("Purple", "#E9D5FF")
            )

            AlertDialog(
                onDismissRequest = { quickHighlightText = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Highlight, contentDescription = null, tint = palette.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Highlight & Study Note", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "\"${quickHighlightText!!.take(120)}${if (quickHighlightText!!.length > 120) "..." else ""}\"",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(palette.surface, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            placeholder = { Text("Attach optional note/annotation...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Highlight Color:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            highlightColors.forEach { (_, hex) ->
                                val colorVal = try {
                                    Color(android.graphics.Color.parseColor(hex))
                                } catch (e: Exception) { Color.Yellow }

                                Surface(
                                    shape = CircleShape,
                                    color = colorVal,
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (selectedColorHex == hex) 2.5.dp else 0.dp,
                                        color = if (selectedColorHex == hex) palette.primary else Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .size(28.dp)
                                        .padding(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(colorVal, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addHighlightNote(
                                selectedText = quickHighlightText!!,
                                noteText = noteText,
                                colorHex = selectedColorHex
                            )
                            quickHighlightText = null
                        }
                    ) {
                        Text("Save Highlight")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { quickHighlightText = null }) {
                        Text("Cancel")
                    }
                },
                containerColor = palette.background,
                titleContentColor = palette.onSurface,
                textContentColor = palette.onSurface
            )
        }

        // Folder Browser Sheet
        if (isFolderVisible) {
            FolderBrowserSheet(
                sheetState = folderSheetState,
                palette = palette,
                onOpenFile = { fileUri, fileName ->
                    viewModel.loadDocument(fileUri, fileName)
                },
                onDismiss = { viewModel.isFolderVisible.value = false }
            )
        }

        // Statistics Sheet
        if (isStatsVisible && parsedMarkdown != null) {
            StatisticsSheet(
                stats = parsedMarkdown!!.stats,
                sheetState = statsSheetState,
                palette = palette,
                onDismiss = { viewModel.isStatsVisible.value = false }
            )
        }

        // Highlights & Notes Sheet
        if (isHighlightsVisible) {
            HighlightsSheet(
                highlights = highlights,
                sheetState = highlightsSheetState,
                palette = palette,
                onAddHighlightNote = { selectedText, noteText, colorHex ->
                    viewModel.addHighlightNote(selectedText, noteText, colorHex)
                },
                onDeleteHighlight = { highlight ->
                    viewModel.deleteHighlight(highlight)
                },
                onDismiss = { viewModel.isHighlightsVisible.value = false }
            )
        }

        // Table of Contents Modal Sheet
        if (isTocVisible && parsedMarkdown != null) {
            TableOfContentsSheet(
                tocList = parsedMarkdown!!.toc,
                sheetState = tocSheetState,
                palette = palette,
                onTocItemClick = { blockIdx ->
                    scope.launch { listState.animateScrollToItem(blockIdx) }
                },
                onDismiss = { viewModel.isTocVisible.value = false }
            )
        }

        // Appearance & Themes Modal Sheet
        if (isThemeSheetVisible) {
            ReaderThemeSettingsSheet(
                settings = settings,
                currentPalette = palette,
                sheetState = themeSheetState,
                onThemeSelected = { themeName -> viewModel.updateTheme(themeName) },
                onFontSizeChanged = { newSp -> viewModel.updateFontSize(newSp) },
                onFontFamilyChanged = { fontName -> viewModel.updateFontFamily(fontName) },
                onLineSpacingChanged = { multiplier -> viewModel.updateLineSpacing(multiplier) },
                onDismiss = { viewModel.isThemeSheetVisible.value = false }
            )
        }
    }
}
