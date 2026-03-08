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

// Regex patterns for markdown continuation markers
private val BULLET_LIST_REGEX = Regex("""^(\s*)([-*+])(\s)(\[[ xX]]\s)?(.*)$""")
private val ORDERED_LIST_REGEX = Regex("""^(\s*)(\d+)([.)]\s)(.*)$""")
private val BLOCKQUOTE_PREFIX_REGEX = Regex("""^((?:\s*>\s?)+)(.*)$""")

private val markdownParser = MarkdownParser(GFMFlavourDescriptor())

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

    // Check if inside fenced code block
    if (isInsideCodeFence(oldText, addedIndex)) return

    val textBeforeNewline = newText.substring(0, addedIndex)
    val lastLineStart = textBeforeNewline.lastIndexOf('\n') + 1
    val lastLine = textBeforeNewline.substring(lastLineStart)

    val continuation = buildContinuation(lastLine) ?: return

    textFieldState.edit {
        if (continuation.isEmpty) {
            val action = determineEmptyMarkerAction(oldText, lastLineStart)
            when (action) {
                EmptyMarkerAction.REMOVE -> {
                    replace(lastLineStart, addedIndex + 1, "")
                    placeCursorAfterCharAt(lastLineStart - 1)
                }
                EmptyMarkerAction.MAKE_NON_TIGHT -> {
                    val blankContent = getBlankLineContent(lastLine)
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

private fun isInsideCodeFence(text: String, position: Int): Boolean {
    if (text.isEmpty()) return false
    val tree = markdownParser.buildMarkdownTreeFromString(text)
    return containsCodeFence(tree, position)
}

private fun containsCodeFence(node: ASTNode, position: Int): Boolean {
    if (node.type == MarkdownElementTypes.CODE_FENCE) {
        val hasEnd = node.children.any { it.type == MarkdownTokenTypes.CODE_FENCE_END }
        return if (!hasEnd) {
            position >= node.startOffset
        } else {
            position > node.startOffset && position < node.endOffset
        }
    }
    return node.children.any { containsCodeFence(it, position) }
}

private enum class EmptyMarkerAction { REMOVE, MAKE_NON_TIGHT }

private fun determineEmptyMarkerAction(text: String, markerLineStart: Int): EmptyMarkerAction {
    if (markerLineStart == 0) return EmptyMarkerAction.REMOVE

    val tree = markdownParser.buildMarkdownTreeFromString(text)
    val listNode = findInnermostList(tree, markerLineStart) ?: return EmptyMarkerAction.REMOVE

    val listItems = listNode.children.filter { it.type == MarkdownElementTypes.LIST_ITEM }
    if (listItems.size < 2) return EmptyMarkerAction.REMOVE

    val emptyItemIndex = listItems.indexOfFirst { it.startOffset >= markerLineStart }
    if (emptyItemIndex != 1) return EmptyMarkerAction.REMOVE

    return if (isListTight(listNode)) EmptyMarkerAction.MAKE_NON_TIGHT else EmptyMarkerAction.REMOVE
}

private fun findInnermostList(node: ASTNode, position: Int): ASTNode? {
    if ((node.type == MarkdownElementTypes.ORDERED_LIST || node.type == MarkdownElementTypes.UNORDERED_LIST)
        && position >= node.startOffset && position <= node.endOffset
    ) {
        for (child in node.children) {
            val nested = findInnermostList(child, position)
            if (nested != null) return nested
        }
        return node
    }
    for (child in node.children) {
        val found = findInnermostList(child, position)
        if (found != null) return found
    }
    return null
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

private fun getBlankLineContent(line: String): String {
    val blockquoteMatch = BLOCKQUOTE_PREFIX_REGEX.matchEntire(line)
    return if (blockquoteMatch != null) {
        blockquoteMatch.groupValues[1].trimEnd()
    } else {
        ""
    }
}

// --- Continuation building ---

private data class Continuation(
    val prefix: String,
    val isEmpty: Boolean,
)

private fun buildContinuation(line: String): Continuation? {
    val blockquoteMatch = BLOCKQUOTE_PREFIX_REGEX.matchEntire(line)
    if (blockquoteMatch != null) {
        val quotePrefix = blockquoteMatch.groupValues[1]
        val innerContent = blockquoteMatch.groupValues[2]

        val innerContinuation = buildListContinuation(innerContent)
        if (innerContinuation != null) {
            return Continuation(
                prefix = quotePrefix + innerContinuation.prefix,
                isEmpty = innerContinuation.isEmpty,
            )
        }

        return if (innerContent.isEmpty()) {
            Continuation(prefix = "", isEmpty = true)
        } else {
            Continuation(prefix = quotePrefix, isEmpty = false)
        }
    }

    return buildListContinuation(line)
}

private fun buildListContinuation(line: String): Continuation? {
    BULLET_LIST_REGEX.matchEntire(line)?.let { match ->
        val indent = match.groupValues[1]
        val marker = match.groupValues[2]
        val space = match.groupValues[3]
        val checkbox = match.groupValues[4]
        val content = match.groupValues[5]

        return if (content.isEmpty() && checkbox.isEmpty()) {
            Continuation(prefix = "", isEmpty = true)
        } else if (content.isEmpty() && checkbox.isNotEmpty()) {
            Continuation(prefix = "", isEmpty = true)
        } else if (checkbox.isNotEmpty()) {
            Continuation(prefix = "$indent$marker$space[ ] ", isEmpty = false)
        } else {
            Continuation(prefix = "$indent$marker$space", isEmpty = false)
        }
    }

    ORDERED_LIST_REGEX.matchEntire(line)?.let { match ->
        val indent = match.groupValues[1]
        val number = match.groupValues[2].toInt()
        val delimiter = match.groupValues[3]
        val content = match.groupValues[4]

        return if (content.isEmpty()) {
            Continuation(prefix = "", isEmpty = true)
        } else {
            Continuation(
                prefix = "$indent${number + 1}$delimiter",
                isEmpty = false,
            )
        }
    }

    return null
}
