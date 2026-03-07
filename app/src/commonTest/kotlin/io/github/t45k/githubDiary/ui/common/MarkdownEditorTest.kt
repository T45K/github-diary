package io.github.t45k.githubDiary.ui.common

import androidx.compose.foundation.text.input.TextFieldState
import org.junit.jupiter.api.Test

class MarkdownEditorTest {

    private fun applyAndGetResult(oldText: String, newText: String): String {
        val textFieldState = TextFieldState(initialText = newText)
        applyMarkdownContinuation(oldText, newText, textFieldState)
        return textFieldState.text.toString()
    }

    // --- Bullet list continuation ---

    @Test
    fun `bullet dash continuation`() {
        // given
        val oldText = "- item"
        val newText = "- item\n"

        // when
        val result = applyAndGetResult(oldText, newText)

        // then
        assert(result == "- item\n- ")
    }

    @Test
    fun `bullet asterisk continuation`() {
        // given
        val oldText = "* item"
        val newText = "* item\n"

        // when
        val result = applyAndGetResult(oldText, newText)

        // then
        assert(result == "* item\n* ")
    }

    @Test
    fun `bullet plus continuation`() {
        // given
        val oldText = "+ item"
        val newText = "+ item\n"

        // when
        val result = applyAndGetResult(oldText, newText)

        // then
        assert(result == "+ item\n+ ")
    }

    // --- Ordered list continuation ---

    @Test
    fun `ordered list dot continuation with increment`() {
        // given
        val oldText = "1. item"
        val newText = "1. item\n"

        // when
        val result = applyAndGetResult(oldText, newText)

        // then
        assert(result == "1. item\n2. ")
    }

    @Test
    fun `ordered list paren continuation with increment`() {
        // given
        val oldText = "2) item"
        val newText = "2) item\n"

        // when
        val result = applyAndGetResult(oldText, newText)

        // then
        assert(result == "2) item\n3) ")
    }

    // --- Checklist continuation ---

    @Test
    fun `unchecked checklist continuation`() {
        // given
        val oldText = "- [ ] todo"
        val newText = "- [ ] todo\n"

        // when
        val result = applyAndGetResult(oldText, newText)

        // then
        assert(result == "- [ ] todo\n- [ ] ")
    }

    @Test
    fun `checked checklist continues as unchecked`() {
        // given
        val oldText = "- [x] done"
        val newText = "- [x] done\n"

        // when
        val result = applyAndGetResult(oldText, newText)

        // then
        assert(result == "- [x] done\n- [ ] ")
    }

    // --- Blockquote continuation ---

    @Test
    fun `blockquote continuation`() {
        // given
        val oldText = "> text"
        val newText = "> text\n"

        // when
        val result = applyAndGetResult(oldText, newText)

        // then
        assert(result == "> text\n> ")
    }

    @Test
    fun `nested blockquote continuation`() {
        // given
        val oldText = "> > text"
        val newText = "> > text\n"

        // when
        val result = applyAndGetResult(oldText, newText)

        // then
        assert(result == "> > text\n> > ")
    }

    // --- Indented list continuation ---

    @Test
    fun `indented bullet list continuation`() {
        // given
        val oldText = "  - item"
        val newText = "  - item\n"

        // when
        val result = applyAndGetResult(oldText, newText)

        // then
        assert(result == "  - item\n  - ")
    }

    @Test
    fun `indented ordered list continuation`() {
        // given
        val oldText = "    1. item"
        val newText = "    1. item\n"

        // when
        val result = applyAndGetResult(oldText, newText)

        // then
        assert(result == "    1. item\n    2. ")
    }

    // --- Empty marker cancellation ---

    @Test
    fun `empty bullet item cancellation`() {
        // given
        val oldText = "- item\n- "
        val newText = "- item\n- \n"

        // when
        val result = applyAndGetResult(oldText, newText)

        // then
        assert(result == "- item\n")
    }

    @Test
    fun `empty ordered list item cancellation`() {
        // given
        val oldText = "- item\n2. "
        val newText = "- item\n2. \n"

        // when
        val result = applyAndGetResult(oldText, newText)

        // then
        assert(result == "- item\n")
    }

    @Test
    fun `empty checklist item cancellation`() {
        // given
        val oldText = "- [x] done\n- [ ] "
        val newText = "- [x] done\n- [ ] \n"

        // when
        val result = applyAndGetResult(oldText, newText)

        // then
        assert(result == "- [x] done\n")
    }

    @Test
    fun `empty blockquote cancellation`() {
        // given
        val oldText = "> text\n> "
        val newText = "> text\n> \n"

        // when
        val result = applyAndGetResult(oldText, newText)

        // then
        assert(result == "> text\n")
    }

    // --- Blockquote with list continuation ---

    @Test
    fun `blockquote with bullet list continuation`() {
        // given
        val oldText = "> - item"
        val newText = "> - item\n"

        // when
        val result = applyAndGetResult(oldText, newText)

        // then
        assert(result == "> - item\n> - ")
    }

    @Test
    fun `blockquote with ordered list continuation`() {
        // given
        val oldText = "> 1. item"
        val newText = "> 1. item\n"

        // when
        val result = applyAndGetResult(oldText, newText)

        // then
        assert(result == "> 1. item\n> 2. ")
    }

    // --- No-op cases ---

    @Test
    fun `plain text newline does nothing`() {
        // given
        val oldText = "hello"
        val newText = "hello\n"

        // when
        val result = applyAndGetResult(oldText, newText)

        // then
        assert(result == "hello\n")
    }

    @Test
    fun `non-newline character addition does nothing`() {
        // given
        val oldText = "- item"
        val newText = "- items"

        // when
        val result = applyAndGetResult(oldText, newText)

        // then
        assert(result == "- items")
    }
}
