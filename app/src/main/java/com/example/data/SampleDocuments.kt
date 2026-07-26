package com.example.data

data class SampleDoc(
    val key: String,
    val title: String,
    val content: String,
    val description: String
)

object SampleDocuments {
    val list = listOf(
        SampleDoc(
            key = "syntax_guide",
            title = "Markdown Syntax Showcase.md",
            description = "Complete reference guide demonstrating Headings, Formatting, Tables, Code blocks & Tasks.",
            content = """
# Markdown Syntax Showcase

Welcome to **Markdown Reader** for Android! This document demonstrates all supported Markdown formatting features.

---

## 1. Typography & Formatting

You can write text in *italics*, **bold text**, or ***bold-italics***. Strikethrough is supported with ~~double tildes~~.

Inline code look like `val x = 42` or `fun renderMarkdown()`.

> "Simplicity is about subtracting the obvious and adding the meaningful."  
> — *John Maeda, The Laws of Simplicity*

---

## 2. Interactive Task Lists

- [x] Download and install Markdown Reader app
- [x] Support offline rendering of `.md` files
- [x] Custom typography and reading themes
- [ ] Export rendered documents to PDF format
- [ ] Star favorite documents for quick access

---

## 3. Lists & Ordered Steps

### Unordered Features
* **Material Design 3**: Clean interface with Material You accents.
* **Storage Access Framework**: Open any local document seamlessly.
* **Offline First**: Zero cloud dependency or telemetry.

### Step-by-Step Guide
1. Tap the **Open File** button or select a sample document.
2. Customize your reading experience using the **Palette** menu.
3. Use **Search in File** to quickly highlight keywords.
4. Tap **Export PDF** to share or save formatted documents.

---

## 4. Tables & Data Display

| Feature | Support Level | Theme Compatibility |
| :--- | :---: | ---: |
| Headings H1-H6 | Full | All Themes |
| Syntax Highlighting | Full | Dark & Light |
| Custom Fonts | Full | System & Monospace |
| PDF Export | Native | Printable Layout |

---

## 5. Code Syntax Blocks

```kotlin
package com.example.markdown

data class DocumentInfo(
    val title: String,
    val wordCount: Int,
    val readingTimeMinutes: Int
) {
    fun getFormattedSummary(): String {
        return "${'$'}title • ${'$'}wordCount words (${'$'}readingTimeMinutes min read)"
    }
}
```

```json
{
  "app_name": "Markdown Reader",
  "version": "1.0.0",
  "offline_mode": true,
  "supported_themes": ["Light", "Dark", "AMOLED", "Sepia", "Solarized", "Nord"]
}
```

---

## 6. Links & Images

Check out the official [Markdown Guide](https://www.markdownguide.org) for detailed syntax rules.

Below is an image banner preview:

![Markdown Banner](img_hero_banner)

---

## 7. Blockquotes & Callouts

> **Pro Tip**: Use the **Table of Contents** button in the top bar to jump directly to any section heading in long documents!

""".trimIndent()
        ),
        SampleDoc(
            key = "architecture_spec",
            title = "Android Architecture Specification.md",
            description = "Technical software engineering design doc with diagrams, API specs, and tables.",
            content = """
# System Architecture Specification

## Overview
This document specifies the technical architecture for the offline-first Android Markdown Reader application.

---

## Core Components Hierarchy

1. **UI Layer (Jetpack Compose)**
   - `HomeScreen`: Handles document selection and history browsing.
   - `MarkdownReaderScreen`: Main distraction-free viewer canvas.
   - `SettingsScreen`: Theme, font scale, and database preferences.

2. **Domain & ViewModels**
   - `DocumentViewModel`: Manages state flows for recent documents, search filters, and file loading.
   - `ReaderViewModel`: Coordinates block parsing, search query highlighting, and TOC navigation.

3. **Data Layer (Room & Repository)**
   - `AppDatabase`: Local SQLite storage via Room DAO.
   - `MarkdownRepository`: SAF content resolver reading and caching.

---

## Key Performance Indicators

| Metric | Target Goal | Status |
| :--- | :---: | :--- |
| Startup Time | < 300ms | Achieved |
| Scroll Frame Rate | 60-120 FPS | Smooth |
| Memory Footprint | < 35 MB | Optimized |
| Offline Capability | 100% | Verified |

---

## Code Structure Example

```kotlin
// Reactive Flow pattern in ViewModel
val uiState: StateFlow<ReaderUiState> = repository.getSettings()
    .flatMapLatest { settings ->
        readerFlow(settings)
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReaderUiState.Loading
    )
```

> **Security Mandate**: All file access operates through Android's Storage Access Framework with scoped URI permissions. No arbitrary file system access required!

""".trimIndent()
        ),
        SampleDoc(
            key = "roadmap_sprint",
            title = "Product Roadmap & Backlog.md",
            description = "Sprint task backlog and release schedule checklist.",
            content = """
# Product Roadmap & Backlog

## Q3 - Core Foundation Release

### Sprint 1: Local File Engine & Rendering
- [x] Implement SAF file picking launcher
- [x] Build AST Markdown block parser for H1-H6, tables, and lists
- [x] Add code block copy button and syntax cards
- [x] Support image rendering via Coil framework

### Sprint 2: Aesthetics & Reader Customization
- [x] Integrate Material 3 Dynamic Color and Custom Themes
- [x] Add AMOLED pitch black, Warm Sepia, and Nord color palettes
- [x] Build font size adjustment slider (12sp to 28sp)
- [x] Support SansSerif, Serif, Monospace, and Cursive fonts

### Sprint 3: Document Utilities & Export
- [x] In-file search with match highlighting and prev/next navigation
- [x] Automatic Table of Contents (TOC) extraction
- [x] Native Android PDF Export engine
- [x] Star/favorite document pinning

---

> "Focus on building a fast, lightweight reader that respects user privacy and system resources."
""".trimIndent()
        ),
        SampleDoc(
            key = "reading_mastery",
            title = "The Art of Effective Reading.md",
            description = "Long-form article crafted for testing comfortable long-form reading typography and Sepia/Solarized themes.",
            content = """
# The Art of Effective Reading

Reading is not merely a passive absorption of printed symbols; it is an active dialogue between the author's thoughts and the reader's intellect.

---

## 1. The Three Levels of Reading

1. **Elementary Reading**: Recognising words and sentences.
2. **Inspectional Reading**: Systematic skimming to grasp the structure and main thesis.
3. **Analytical Reading**: In-depth engagement, questioning assumptions, and taking actionable notes.

> "To read well is to think deeply. A great book asks questions of you as much as you ask questions of it."

---

## 2. Note Taking in Markdown

Markdown has emerged as the premier plain-text format for modern note taking because:

* **Longevity**: Plain text files will remain readable for decades without proprietary app lock-in.
* **Portability**: Effortlessly viewable on mobile devices, laptops, and tablets.
* **Structure**: Clean headings and lists make information easy to parse at a glance.

```markdown
# My Book Summary
## Key Takeaways
- Concept 1: Focus on core principles.
- Concept 2: Build atomic habits.
```

---

## 3. Creating a Distraction-Free Environment

To achieve deep reading flow:
- Choose an eye-safe reading theme like **Warm Sepia** or **AMOLED Dark**.
- Adjust font sizes to match comfortable viewing distance.
- Use full-screen immersive mode to minimize visual clutter.

""".trimIndent()
        )
    )
}
