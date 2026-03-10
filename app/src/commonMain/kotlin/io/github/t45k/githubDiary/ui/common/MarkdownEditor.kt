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

private data class NewlineInsertion(
    val addedIndex: Int,
    val lineStart: Int,
    val line: String,
)

private data class ContinuationContext(
    val insertion: NewlineInsertion,
    val node: ASTNode?,
    val quoteContext: BlockquoteContext,
)

private sealed interface EditorAction {
    data class InsertPrefix(
        val position: Int,
        val prefix: String,
    ) : EditorAction

    data class ReplaceRange(
        val start: Int,
        val end: Int,
        val replacement: String,
    ) : EditorAction
}

private sealed interface ContinuationTarget {
    data object Blockquote : ContinuationTarget
    data object ListItem : ContinuationTarget
    data object PlainListPattern : ContinuationTarget
}

internal fun applyMarkdownContinuation(
    oldText: String,
    newText: String,
    textFieldState: TextFieldState,
) {
    val parsedMarkdown = parseMarkdown(oldText)
    val editorAction = resolveEditorAction(parsedMarkdown, oldText, newText) ?: return

    textFieldState.edit {
        when (editorAction) {
            is EditorAction.InsertPrefix -> {
                replace(editorAction.position, editorAction.position, editorAction.prefix)
                placeCursorAfterCharAt(editorAction.position + editorAction.prefix.length - 1)
            }

            is EditorAction.ReplaceRange -> {
                replace(editorAction.start, editorAction.end, editorAction.replacement)
                placeCursorAfterCharAt(editorAction.start + editorAction.replacement.length - 1)
            }
        }
    }
}

private fun resolveEditorAction(
    parsedMarkdown: ParsedMarkdown,
    oldText: String,
    newText: String,
): EditorAction? {
    val insertion = findNewlineInsertion(oldText, newText) ?: return null
    if (isInsideCodeFence(parsedMarkdown, insertion.addedIndex)) return null

    val continuationContext = buildContinuationContext(parsedMarkdown, insertion)
    val continuation = buildContinuation(continuationContext) ?: return null
    return continuation.toEditorAction(parsedMarkdown, insertion)
}

private fun findNewlineInsertion(oldText: String, newText: String): NewlineInsertion? {
    val addedIndex = newText.indices.firstOrNull { index ->
        index >= oldText.length || newText[index] != oldText[index]
    } ?: return null
    if (newText[addedIndex] != '\n') return null

    val textBeforeNewline = newText.substring(0, addedIndex)
    val lineStart = textBeforeNewline.lastIndexOf('\n') + 1
    return NewlineInsertion(
        addedIndex = addedIndex,
        lineStart = lineStart,
        line = textBeforeNewline.substring(lineStart),
    )
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
    val codeFence = node.findSelfOrParentOfType(MarkdownElementTypes.CODE_FENCE) ?: return false

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

    return listNode
        .takeIf(::isListTight)
        ?.let { EmptyMarkerAction.MAKE_NON_TIGHT }
        ?: EmptyMarkerAction.REMOVE
}

private fun findInnermostList(parsedMarkdown: ParsedMarkdown, position: Int): ASTNode? {
    val probePosition = position.coerceAtMost((parsedMarkdown.text.length - 1).coerceAtLeast(0))
    val node = findDeepestNodeContaining(parsedMarkdown.tree, probePosition) ?: return null
    return node.findSelfOrParentOfTypes(
        MarkdownElementTypes.ORDERED_LIST,
        MarkdownElementTypes.UNORDERED_LIST,
    )
}

private fun isListTight(listNode: ASTNode): Boolean =
    listNode.children
        .zipWithNext()
        .none { (previous, current) ->
            previous.type == MarkdownTokenTypes.EOL && current.type == MarkdownTokenTypes.EOL
        }

private fun findDeepestNodeContaining(node: ASTNode, position: Int): ASTNode? {
    if (position < node.startOffset || position > node.endOffset) return null
    return node.children.firstNotNullOfOrNull { child ->
        findDeepestNodeContaining(child, position)
    } ?: node
}

private fun ASTNode.findSelfOrParentOfType(type: org.intellij.markdown.IElementType): ASTNode? =
    takeIf { it.type == type } ?: getParentOfType(type)

private fun ASTNode.findSelfOrParentOfTypes(vararg types: org.intellij.markdown.IElementType): ASTNode? =
    types.firstNotNullOfOrNull(::findSelfOrParentOfType)

private fun getBlankLineContent(quotePrefix: String): String {
    return quotePrefix.trimEnd()
}

// --- Continuation building ---

private data class Continuation(
    val prefix: String,
    val isEmpty: Boolean,
    val quotePrefix: String = "",
)

private fun buildContinuationContext(
    parsedMarkdown: ParsedMarkdown,
    insertion: NewlineInsertion,
): ContinuationContext {
    val quoteContext = parseBlockquoteContext(insertion.line)
    val probePosition = (insertion.lineStart + insertion.line.length - 1)
        .coerceAtMost((parsedMarkdown.text.length - 1).coerceAtLeast(0))
    return ContinuationContext(
        insertion = insertion,
        node = findDeepestNodeContaining(parsedMarkdown.tree, probePosition),
        quoteContext = quoteContext,
    )
}

private fun Continuation.toEditorAction(
    parsedMarkdown: ParsedMarkdown,
    insertion: NewlineInsertion,
): EditorAction {
    if (!isEmpty) {
        return EditorAction.InsertPrefix(
            position = insertion.addedIndex + 1,
            prefix = prefix,
        )
    }

    return when (determineEmptyMarkerAction(parsedMarkdown, insertion.lineStart)) {
        EmptyMarkerAction.REMOVE -> EditorAction.ReplaceRange(
            start = insertion.lineStart,
            end = insertion.addedIndex + 1,
            replacement = "",
        )

        EmptyMarkerAction.MAKE_NON_TIGHT -> {
            val replacement = "\n${getBlankLineContent(quotePrefix)}\n${insertion.line}"
            EditorAction.ReplaceRange(
                start = insertion.lineStart - 1,
                end = insertion.addedIndex + 1,
                replacement = replacement,
            )
        }
    }
}

private fun buildContinuation(context: ContinuationContext): Continuation? {
    if (context.insertion.line.isEmpty()) return null

    return when (resolveContinuationTarget(context.node)) {
        ContinuationTarget.Blockquote -> buildBlockquoteContinuation(context.quoteContext)
        ContinuationTarget.ListItem,
        ContinuationTarget.PlainListPattern,
            -> buildListContinuation(context.insertion.line)
    }
}

private fun resolveContinuationTarget(node: ASTNode?): ContinuationTarget = when {
    node.isOfTypeOrInside(MarkdownElementTypes.BLOCK_QUOTE) -> ContinuationTarget.Blockquote
    node.isOfTypeOrInside(MarkdownElementTypes.LIST_ITEM) -> ContinuationTarget.ListItem
    else -> ContinuationTarget.PlainListPattern
}

private fun ASTNode?.isOfTypeOrInside(type: org.intellij.markdown.IElementType): Boolean =
    this?.type == type || this?.getParentOfType(type) != null

private fun buildBlockquoteContinuation(quoteContext: BlockquoteContext): Continuation =
    buildListContinuation(quoteContext.content)
        ?.let { continuation ->
            continuation.copy(
                prefix = quoteContext.prefix + continuation.prefix,
                quotePrefix = quoteContext.prefix,
            )
        }
        ?: quoteContext.toPlainBlockquoteContinuation()

private fun BlockquoteContext.toPlainBlockquoteContinuation(): Continuation =
    if (content.isEmpty()) {
        Continuation(prefix = "", isEmpty = true, quotePrefix = prefix)
    } else {
        Continuation(prefix = prefix, isEmpty = false, quotePrefix = prefix)
    }

private fun buildListContinuation(line: String): Continuation? =
    parseBulletListMarker(line)?.toContinuation()
        ?: parseOrderedListMarker(line)?.toContinuation()

private fun BulletListMarker.toContinuation(): Continuation = when {
    content.isEmpty() -> Continuation(prefix = "", isEmpty = true)
    checkbox != null -> Continuation(prefix = "${indent}${marker} ${uncheckedCheckboxMarker}", isEmpty = false)
    else -> Continuation(prefix = "${indent}${marker} ", isEmpty = false)
}

private fun OrderedListMarker.toContinuation(): Continuation =
    if (content.isEmpty()) {
        Continuation(prefix = "", isEmpty = true)
    } else {
        Continuation(prefix = "${indent}${number + 1}${delimiter} ", isEmpty = false)
    }

private data class BlockquoteContext(
    val prefix: String,
    val content: String,
)

private data class BlockquoteSegment(
    val prefix: String,
    val nextIndex: Int,
)

private fun parseBlockquoteContext(line: String): BlockquoteContext {
    val segments = generateSequence(parseBlockquoteSegment(line, 0)) { segment ->
        parseBlockquoteSegment(line, segment.nextIndex)
    }.toList()
    val contentStart = segments.lastOrNull()?.nextIndex ?: 0

    return BlockquoteContext(
        prefix = segments.joinToString(separator = "") { it.prefix },
        content = line.substring(contentStart),
    )
}

private fun parseBlockquoteSegment(line: String, startIndex: Int): BlockquoteSegment? {
    if (startIndex >= line.length) return null

    val indentEnd = line.indexOfFirstNonBlockquoteIndent(startIndex)
    if (indentEnd >= line.length || line[indentEnd] != '>') return null

    val nextIndex = (indentEnd + 1).let { markerEnd ->
        if (markerEnd < line.length && line[markerEnd] == ' ') markerEnd + 1 else markerEnd
    }

    return BlockquoteSegment(
        prefix = line.substring(startIndex, nextIndex),
        nextIndex = nextIndex,
    )
}

private fun String.indexOfFirstNonBlockquoteIndent(startIndex: Int): Int =
    drop(startIndex)
        .indexOfFirst { !it.isWhitespace() || it == '\n' }
        .let { offset -> if (offset == -1) length else startIndex + offset }

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

    val numberEnd = line.indexOfFirstFrom(indentEnd) { !it.isDigit() }
    if (numberEnd == line.length || line[numberEnd] !in setOf('.', ')')) return null

    val delimiter = line[numberEnd]
    val contentStart = numberEnd + 1
    if (contentStart >= line.length || line[contentStart] != ' ') return null

    return OrderedListMarker(
        indent = line.substring(0, indentEnd),
        number = line.substring(indentEnd, numberEnd).toInt(),
        delimiter = delimiter,
        content = line.substring(contentStart + 1),
    )
}

private fun String.indexOfFirstFrom(startIndex: Int, predicate: (Char) -> Boolean): Int =
    drop(startIndex)
        .indexOfFirst(predicate)
        .let { offset -> if (offset == -1) length else startIndex + offset }

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
