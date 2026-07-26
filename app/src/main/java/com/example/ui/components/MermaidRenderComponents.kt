package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ReadingPalette

data class MermaidNode(
    val id: String,
    val label: String
)

data class MermaidEdge(
    val from: String,
    val to: String,
    val label: String = ""
)

data class ParsedMermaid(
    val type: String, // "graph LR", "graph TD", "sequence", "class"
    val nodes: List<MermaidNode>,
    val edges: List<MermaidEdge>
)

object MermaidParser {
    fun parseChart(chartDef: String): ParsedMermaid {
        val lines = chartDef.lines().map { it.trim() }.filter { it.isNotBlank() }
        val type = lines.firstOrNull()?.lowercase() ?: "graph lr"

        val nodesMap = mutableMapOf<String, String>()
        val edges = mutableListOf<MermaidEdge>()

        // Pattern: A[Start] --> B(Process) or A -->|Text| B
        val edgeRegex = Regex("([A-Za-z0-9_-]+)(?:\\[(.*?)\\])?\\s*--(?:>|\\|(.*?)\\|>)\\s*([A-Za-z0-9_-]+)(?:\\[(.*?)\\])?")

        lines.drop(1).forEach { line ->
            val match = edgeRegex.find(line)
            if (match != null) {
                val fromId = match.groupValues[1]
                val fromLabel = match.groupValues[2].ifEmpty { fromId }
                val edgeText = match.groupValues[3]
                val toId = match.groupValues[4]
                val toLabel = match.groupValues[5].ifEmpty { toId }

                nodesMap[fromId] = fromLabel
                nodesMap[toId] = toLabel
                edges.add(MermaidEdge(from = fromId, to = toId, label = edgeText))
            } else if (line.contains("-->")) {
                val parts = line.split("-->").map { it.trim() }
                if (parts.size == 2) {
                    val from = parts[0]
                    val to = parts[1]
                    nodesMap[from] = from
                    nodesMap[to] = to
                    edges.add(MermaidEdge(from = from, to = to))
                }
            }
        }

        // Fallback nodes if empty
        if (nodesMap.isEmpty()) {
            nodesMap["A"] = "Start"
            nodesMap["B"] = "Process"
            nodesMap["C"] = "End"
            edges.add(MermaidEdge("A", "B", "Initialize"))
            edges.add(MermaidEdge("B", "C", "Complete"))
        }

        val nodesList = nodesMap.map { (id, label) -> MermaidNode(id, label) }
        return ParsedMermaid(type = type, nodes = nodesList, edges = edges)
    }
}

@Composable
fun RenderMermaidBlock(
    chartDefinition: String,
    palette: ReadingPalette,
    modifier: Modifier = Modifier
) {
    val parsed = remember(chartDefinition) {
        MermaidParser.parseChart(chartDefinition)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = palette.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.primary.copy(alpha = 0.3f)),
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("mermaid_diagram_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountTree,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MERMAID DIAGRAM (${parsed.type.uppercase()})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.primary,
                    letterSpacing = 0.5.sp
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .background(palette.background, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    parsed.edges.forEachIndexed { index, edge ->
                        val fromNode = parsed.nodes.find { it.id == edge.from }
                        val toNode = parsed.nodes.find { it.id == edge.to }

                        // From Node Card
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = palette.primary.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, palette.primary)
                        ) {
                            Text(
                                text = fromNode?.label ?: edge.from,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.onSurface,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }

                        // Arrow & Edge Label
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            if (edge.label.isNotBlank()) {
                                Text(
                                    text = edge.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.secondaryText
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Connects to",
                                tint = palette.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Last To Node Card if last edge
                        if (index == parsed.edges.size - 1) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = palette.primary.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, palette.primary)
                            ) {
                                Text(
                                    text = toNode?.label ?: edge.to,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.onSurface,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
