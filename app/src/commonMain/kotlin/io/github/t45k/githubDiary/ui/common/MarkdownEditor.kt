package io.github.t45k.githubDiary.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getParentOfType
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

@Composable
fun MarkdownEditor(
    textFieldState: TextFieldState,
) {
    LaunchedEffect(textFieldState) {
        var previousText = textFieldState.text.toString()
        snapshotFlow { textFieldState.text }
            .collect { charSequence ->
                val newText = charSequence.toString()
                if (newText != previousText) {
                    applyMarkdownContinuation(previousText, newText, textFieldState)
                    previousText = textFieldState.text.toString()
                }
            }
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    OutlinedTextField(
        state = textFieldState,
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
    )
}

private val markdownParser = MarkdownParser(GFMFlavourDescriptor())

private data class ParsedMarkdown(
    val text: String,
    val tree: ASTNode,
)

internal fun applyMarkdownContinuation(
    oldText: String,
    newText: String,
    textFieldState: TextFieldState,
) {
    // Find the first differing position to locate the added character
    var addedIndex = -1
    for (i in newText.indices) {
        if (i >= oldText.length || newText[i] != oldText[i]) {
            addedIndex = i
            break
        }
    }
    if (addedIndex == -1 || newText[addedIndex] != '\n') return

    val parsedMarkdown = parseMarkdown(oldText)

    // Check if inside fenced code block
    if (isInsideCodeFence(parsedMarkdown, addedIndex)) return

    val textBeforeNewline = newText.substring(0, addedIndex)
    val lastLineStart = textBeforeNewline.lastIndexOf('\n') + 1
    val lastLine = textBeforeNewline.substring(lastLineStart)

    val continuation = buildContinuation(parsedMarkdown, lastLineStart, lastLine) ?: return

    textFieldState.edit {
        if (continuation.isEmpty) {
            val action = determineEmptyMarkerAction(parsedMarkdown, lastLineStart)
            when (action) {
                EmptyMarkerAction.REMOVE -> {
                    replace(lastLineStart, addedIndex + 1, "")
                    placeCursorAfterCharAt(lastLineStart - 1)
                }
                EmptyMarkerAction.MAKE_NON_TIGHT -> {
                    val blankContent = getBlankLineContent(continuation.quotePrefix)
                    val replacement = "\n$blankContent\n$lastLine"
                    replace(lastLineStart - 1, addedIndex + 1, replacement)
                    placeCursorAfterCharAt(lastLineStart - 1 + replacement.length - 1)
                }
            }
        } else {
            val insertPosition = addedIndex + 1
            val prefix = continuation.prefix
            replace(insertPosition, insertPosition, prefix)
            placeCursorAfterCharAt(insertPosition + prefix.length - 1)
        }
    }
}

// --- Parser-based features ---

private fun parseMarkdown(text: String): ParsedMarkdown {
    return ParsedMarkdown(
        text = text,
        tree = markdownParser.buildMarkdownTreeFromString(text),
    )
}

private fun isInsideCodeFence(parsedMarkdown: ParsedMarkdown, position: Int): Boolean {
    if (parsedMarkdown.text.isEmpty()) return false

    val node = findDeepestNodeContaining(
        node = parsedMarkdown.tree,
        position = position.coerceAtMost(parsedMarkdown.text.lastIndex),
    ) ?: return false
    val codeFence = node.getParentOfType(MarkdownElementTypes.CODE_FENCE)
        ?: if (node.type == MarkdownElementTypes.CODE_FENCE) node else return false

    val hasEnd = codeFence.children.any { it.type == MarkdownTokenTypes.CODE_FENCE_END }
    return if (!hasEnd) {
        position >= codeFence.startOffset
    } else {
        position > codeFence.startOffset && position < codeFence.endOffset
    }
}

private enum class EmptyMarkerAction { REMOVE, MAKE_NON_TIGHT }

private fun determineEmptyMarkerAction(parsedMarkdown: ParsedMarkdown, markerLineStart: Int): EmptyMarkerAction {
    if (markerLineStart == 0) return EmptyMarkerAction.REMOVE

    val listNode = findInnermostList(parsedMarkdown, markerLineStart) ?: return EmptyMarkerAction.REMOVE

    val listItems = listNode.children.filter { it.type == MarkdownElementTypes.LIST_ITEM }
    if (listItems.size < 2) return EmptyMarkerAction.REMOVE

    val emptyItemIndex = listItems.indexOfFirst { it.startOffset >= markerLineStart }
    if (emptyItemIndex != 1) return EmptyMarkerAction.REMOVE

    return if (isListTight(listNode)) EmptyMarkerAction.MAKE_NON_TIGHT else EmptyMarkerAction.REMOVE
}

private fun findInnermostList(parsedMarkdown: ParsedMarkdown, position: Int): ASTNode? {
    val probePosition = position.coerceAtMost((parsedMarkdown.text.length - 1).coerceAtLeast(0))
    val node = findDeepestNodeContaining(parsedMarkdown.tree, probePosition) ?: return null
    return node.getParentOfType(MarkdownElementTypes.ORDERED_LIST, MarkdownElementTypes.UNORDERED_LIST)
        ?: if (node.type == MarkdownElementTypes.ORDERED_LIST || node.type == MarkdownElementTypes.UNORDERED_LIST) {
            node
        } else {
            null
        }
}

private fun isListTight(listNode: ASTNode): Boolean {
    var prevWasEol = false
    for (child in listNode.children) {
        if (child.type == MarkdownTokenTypes.EOL) {
            if (prevWasEol) return false
            prevWasEol = true
        } else {
            prevWasEol = false
        }
    }
    return true
}

private fun findDeepestNodeContaining(node: ASTNode, position: Int): ASTNode? {
    if (position < node.startOffset || position > node.endOffset) return null
    return node.children.firstNotNullOfOrNull { child ->
        findDeepestNodeContaining(child, position)
    } ?: node
}

private fun getBlankLineContent(quotePrefix: String): String {
    return quotePrefix.trimEnd()
}

// --- Continuation building ---

private data class Continuation(
    val prefix: String,
    val isEmpty: Boolean,
    val quotePrefix: String = "",
)

private fun buildContinuation(
    parsedMarkdown: ParsedMarkdown,
    lineStart: Int,
    line: String,
): Continuation? {
    if (line.isEmpty()) return null

    val quoteContext = parseBlockquoteContext(line)
    val probePosition = (lineStart + line.length - 1).coerceAtMost((parsedMarkdown.text.length - 1).coerceAtLeast(0))
    val node = findDeepestNodeContaining(parsedMarkdown.tree, probePosition)
    val isBlockQuote = node?.getParentOfType(MarkdownElementTypes.BLOCK_QUOTE) != null || node?.type == MarkdownElementTypes.BLOCK_QUOTE

    if (isBlockQuote) {
        buildListContinuation(quoteContext.content)?.let { continuation ->
            return continuation.copy(
                prefix = quoteContext.prefix + continuation.prefix,
                quotePrefix = quoteContext.prefix,
            )
        }

        return if (quoteContext.content.isEmpty()) {
            Continuation(prefix = "", isEmpty = true, quotePrefix = quoteContext.prefix)
        } else {
            Continuation(prefix = quoteContext.prefix, isEmpty = false, quotePrefix = quoteContext.prefix)
        }
    }

    val listItem = node?.getParentOfType(MarkdownElementTypes.LIST_ITEM)
        ?: if (node?.type == MarkdownElementTypes.LIST_ITEM) node else null
    if (listItem != null) {
        return buildListContinuation(line)
    }

    return buildListContinuation(line)
}

private fun buildListContinuation(line: String): Continuation? {
    parseBulletListMarker(line)?.let { marker ->
        return if (marker.content.isEmpty()) {
            Continuation(prefix = "", isEmpty = true)
        } else if (marker.checkbox != null) {
            Continuation(prefix = "${marker.indent}${marker.marker} ${uncheckedCheckboxMarker}", isEmpty = false)
        } else {
            Continuation(prefix = "${marker.indent}${marker.marker} ", isEmpty = false)
        }
    }

    parseOrderedListMarker(line)?.let { marker ->
        return if (marker.content.isEmpty()) {
            Continuation(prefix = "", isEmpty = true)
        } else {
            Continuation(prefix = "${marker.indent}${marker.number + 1}${marker.delimiter} ", isEmpty = false)
        }
    }

    return null
}

private data class BlockquoteContext(
    val prefix: String,
    val content: String,
)

private fun parseBlockquoteContext(line: String): BlockquoteContext {
    var index = 0
    val prefix = buildString {
        while (index < line.length) {
            val indentStart = index
            while (index < line.length && line[index].isWhitespace() && line[index] != '\n') {
                index++
            }
            if (index >= line.length || line[index] != '>') {
                index = indentStart
                break
            }
            append(line, indentStart, index + 1)
            index++
            if (index < line.length && line[index] == ' ') {
                append(' ')
                index++
            }
        }
    }
    return BlockquoteContext(prefix = prefix, content = line.substring(index))
}

private data class BulletListMarker(
    val indent: String,
    val marker: Char,
    val checkbox: String?,
    val content: String,
)

private data class OrderedListMarker(
    val indent: String,
    val number: Int,
    val delimiter: Char,
    val content: String,
)

private val uncheckedCheckboxMarker = "[ ] "

private fun parseBulletListMarker(line: String): BulletListMarker? {
    val indentEnd = line.indexOfFirst { !it.isWhitespace() }.let { if (it == -1) line.length else it }
    if (indentEnd >= line.length) return null
    val marker = line[indentEnd]
    if (marker !in setOf('-', '*', '+')) return null
    val spaceIndex = indentEnd + 1
    if (spaceIndex >= line.length || line[spaceIndex] != ' ') return null

    val contentStart = spaceIndex + 1
    val checkbox = parseCheckbox(line, contentStart)
    val content = if (checkbox != null) {
        line.substring(contentStart + checkbox.length)
    } else {
        line.substring(contentStart)
    }

    return BulletListMarker(
        indent = line.substring(0, indentEnd),
        marker = marker,
        checkbox = checkbox,
        content = content,
    )
}

private fun parseOrderedListMarker(line: String): OrderedListMarker? {
    val indentEnd = line.indexOfFirst { !it.isWhitespace() }.let { if (it == -1) line.length else it }
    if (indentEnd >= line.length || !line[indentEnd].isDigit()) return null

    var index = indentEnd
    while (index < line.length && line[index].isDigit()) {
        index++
    }
    if (index >= line.length || line[index] !in setOf('.', ')')) return null
    val delimiter = line[index]
    index++
    if (index >= line.length || line[index] != ' ') return null

    return OrderedListMarker(
        indent = line.substring(0, indentEnd),
        number = line.substring(indentEnd, index - 1).toInt(),
        delimiter = delimiter,
        content = line.substring(index + 1),
    )
}

private fun parseCheckbox(line: String, startIndex: Int): String? {
    val checkboxEndIndex = startIndex + 3
    val spaceIndex = startIndex + 3
    if (spaceIndex >= line.length) return null
    val checkbox = line.substring(startIndex, checkboxEndIndex)
    return if ((checkbox == "[ ]" || checkbox == "[x]" || checkbox == "[X]") && line[spaceIndex] == ' ') {
        line.substring(startIndex, spaceIndex + 1)
    } else {
        null
    }
}
