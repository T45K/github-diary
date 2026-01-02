package core.entity

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DiaryContentTest {

    @Test
    fun `init creates content with proper date header`() {
        val date = LocalDate(2026, 1, 2)
        val diary = DiaryContent.init(date)

        assertEquals(date, diary.date)
        assertTrue(diary.content.startsWith("# 2026/01/02 (Fri)"))
    }

    @Test
    fun `init handles different days of week`() {
        val monday = LocalDate(2026, 1, 5)
        val diary = DiaryContent.init(monday)

        assertTrue(diary.content.startsWith("# 2026/01/05 (Mon)"))
    }

    @Test
    fun `updateContent keeps header when content already starts with date`() {
        val date = LocalDate(2026, 1, 2)
        val diary = DiaryContent.init(date)
        val newContent = "# 2026/01/02 (Fri)\n\nUpdated content"

        val updated = diary.updateContent(newContent)

        assertEquals(newContent, updated.content)
        assertEquals(date, updated.date)
    }

    @Test
    fun `updateContent prepends header when content does not start with date`() {
        val date = LocalDate(2026, 1, 2)
        val diary = DiaryContent.init(date)
        val contentWithoutHeader = "Some content without header"

        val updated = diary.updateContent(contentWithoutHeader)

        assertTrue(updated.content.startsWith("# 2026/01/02 (Fri)"))
        assertTrue(updated.content.contains(contentWithoutHeader))
    }

    @Test
    fun `updateContent formats date with leading zeros`() {
        val date = LocalDate(2026, 1, 5)
        val diary = DiaryContent.init(date)

        assertTrue(diary.content.startsWith("# 2026/01/05"))
    }
}
