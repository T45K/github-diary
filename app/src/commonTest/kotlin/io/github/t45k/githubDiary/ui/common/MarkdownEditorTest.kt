package io.github.t45k.githubDiary.ui.common

import androidx.compose.foundation.text.input.TextFieldState
import kotlin.test.assertEquals
import kotlin.test.Test

class MarkdownEditorTest {

    private fun applyAndGetResult(oldText: String, newText: String): String {
        val textFieldState = TextFieldState(initialText = newText)
        applyMarkdownContinuation(oldText, newText, textFieldState)
        return textFieldState.text.toString()
    }

    // --- Bullet list continuation ---

    @Test
    fun `bullet dash continuation`() {
        val result = applyAndGetResult("- item", "- item\n")
        assertEquals("- item\n- ", result)
    }

    @Test
    fun `bullet asterisk continuation`() {
        val result = applyAndGetResult("* item", "* item\n")
        assertEquals("* item\n* ", result)
    }

    @Test
    fun `bullet plus continuation`() {
        val result = applyAndGetResult("+ item", "+ item\n")
        assertEquals("+ item\n+ ", result)
    }

    // --- Ordered list continuation ---

    @Test
    fun `ordered list dot continuation with increment`() {
        val result = applyAndGetResult("1. item", "1. item\n")
        assertEquals("1. item\n2. ", result)
    }

    @Test
    fun `ordered list paren continuation with increment`() {
        val result = applyAndGetResult("2) item", "2) item\n")
        assertEquals("2) item\n3) ", result)
    }

    // --- Checklist continuation ---

    @Test
    fun `unchecked checklist continuation`() {
        val result = applyAndGetResult("- [ ] todo", "- [ ] todo\n")
        assertEquals("- [ ] todo\n- [ ] ", result)
    }

    @Test
    fun `checked checklist continues as unchecked`() {
        val result = applyAndGetResult("- [x] done", "- [x] done\n")
        assertEquals("- [x] done\n- [ ] ", result)
    }

    // --- Blockquote continuation ---

    @Test
    fun `blockquote continuation`() {
        val result = applyAndGetResult("> text", "> text\n")
        assertEquals("> text\n> ", result)
    }

    @Test
    fun `nested blockquote continuation`() {
        val result = applyAndGetResult("> > text", "> > text\n")
        assertEquals("> > text\n> > ", result)
    }

    // --- Indented list continuation ---

    @Test
    fun `indented bullet list continuation`() {
        val result = applyAndGetResult("  - item", "  - item\n")
        assertEquals("  - item\n  - ", result)
    }

    @Test
    fun `indented ordered list continuation`() {
        val result = applyAndGetResult("    1. item", "    1. item\n")
        assertEquals("    1. item\n    2. ", result)
    }

    // --- Empty marker cancellation (tight list → non-tight conversion) ---

    @Test
    fun `empty bullet in tight list converts to non-tight`() {
        // CodeMirror: second empty item in tight list → add blank line, keep marker
        val result = applyAndGetResult("- item\n- ", "- item\n- \n")
        assertEquals("- item\n\n- ", result)
    }

    @Test
    fun `empty checklist in tight list converts to non-tight`() {
        val result = applyAndGetResult("- [x] done\n- [ ] ", "- [x] done\n- [ ] \n")
        assertEquals("- [x] done\n\n- [ ] ", result)
    }

    // --- Empty marker cancellation (removal) ---

    @Test
    fun `empty bullet in non-tight list is removed`() {
        // After converting to non-tight, pressing Enter again removes the marker
        val result = applyAndGetResult("- item\n\n- ", "- item\n\n- \n")
        assertEquals("- item\n\n", result)
    }

    @Test
    fun `empty ordered list item is removed`() {
        // Single ordered list item → removed
        val result = applyAndGetResult("- item\n2. ", "- item\n2. \n")
        assertEquals("- item\n", result)
    }

    @Test
    fun `first empty bullet item is removed`() {
        // First (and only) item → always removed
        val result = applyAndGetResult("- ", "- \n")
        assertEquals("", result)
    }

    @Test
    fun `third empty bullet in tight list is removed`() {
        // Third+ item → always removed
        val result = applyAndGetResult("- one\n- two\n- ", "- one\n- two\n- \n")
        assertEquals("- one\n- two\n", result)
    }

    @Test
    fun `empty blockquote cancellation`() {
        val result = applyAndGetResult("> text\n> ", "> text\n> \n")
        assertEquals("> text\n", result)
    }

    // --- Blockquote with list continuation ---

    @Test
    fun `blockquote with bullet list continuation`() {
        val result = applyAndGetResult("> - item", "> - item\n")
        assertEquals("> - item\n> - ", result)
    }

    @Test
    fun `blockquote with ordered list continuation`() {
        val result = applyAndGetResult("> 1. item", "> 1. item\n")
        assertEquals("> 1. item\n> 2. ", result)
    }

    @Test
    fun `empty bullet in tight list inside blockquote converts to non-tight`() {
        val result = applyAndGetResult("> - item\n> - ", "> - item\n> - \n")
        assertEquals("> - item\n>\n> - ", result)
    }

    // --- Fenced code block (parser-based) ---

    @Test
    fun `no continuation inside fenced code block`() {
        val oldText = "```\n- item"
        val newText = "```\n- item\n"
        val result = applyAndGetResult(oldText, newText)
        assertEquals("```\n- item\n", result)
    }

    @Test
    fun `no continuation for unclosed fence in list`() {
        val oldText = "- ```foo"
        val newText = "- ```foo\n"
        val result = applyAndGetResult(oldText, newText)
        assertEquals("- ```foo\n", result)
    }

    @Test
    fun `continuation works after closed fence`() {
        val oldText = "```\ncode\n```\n- item"
        val newText = "```\ncode\n```\n- item\n"
        val result = applyAndGetResult(oldText, newText)
        assertEquals("```\ncode\n```\n- item\n- ", result)
    }

    // --- No-op cases ---

    @Test
    fun `plain text newline does nothing`() {
        val result = applyAndGetResult("hello", "hello\n")
        assertEquals("hello\n", result)
    }

    @Test
    fun `non-newline character addition does nothing`() {
        val result = applyAndGetResult("- item", "- items")
        assertEquals("- items", result)
}
}
