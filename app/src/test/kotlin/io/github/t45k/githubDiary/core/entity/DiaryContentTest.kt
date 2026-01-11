package io.github.t45k.githubDiary.core.entity

import io.github.t45k.githubDiary.core.entity.DiaryContent
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Test

class DiaryContentTest {

    @Test
    fun `init creates content with proper date header`() {
        // given
        val date = LocalDate(2026, 1, 2)

        // when
        val diary = DiaryContent.init(date)

        // then
        assert(diary.date == date)
        assert(diary.content.startsWith("# 2026/01/02 (Fri)"))
    }

    @Test
    fun `init handles different days of week`() {
        // given
        val monday = LocalDate(2026, 1, 5)

        // when
        val diary = DiaryContent.init(monday)

        // then
        assert(diary.content.startsWith("# 2026/01/05 (Mon)"))
    }

    @Test
    fun `updateContent keeps header when content already starts with date`() {
        // given
        val date = LocalDate(2026, 1, 2)
        val diary = DiaryContent.init(date)
        val newContent = "# 2026/01/02 (Fri)\n\nUpdated content"

        // when
        val updated = diary.updateContent(newContent)

        // then
        assert(updated.content == newContent)
        assert(updated.date == date)
    }

    @Test
    fun `updateContent prepends header when content does not start with date`() {
        // given
        val date = LocalDate(2026, 1, 2)
        val diary = DiaryContent.init(date)
        val contentWithoutHeader = "Some content without header"

        // when
        val updated = diary.updateContent(contentWithoutHeader)

        // then
        assert(updated.content.startsWith("# 2026/01/02 (Fri)"))
        assert(updated.content.contains(contentWithoutHeader))
    }

    @Test
    fun `updateContent formats date with leading zeros`() {
        // given
        val date = LocalDate(2026, 1, 5)

        // when
        val diary = DiaryContent.init(date)

        // then
        assert(diary.content.startsWith("# 2026/01/05"))
    }
}
