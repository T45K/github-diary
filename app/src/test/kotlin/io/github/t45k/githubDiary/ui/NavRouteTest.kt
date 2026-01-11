package io.github.t45k.githubDiary.ui

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number
import org.junit.jupiter.api.Test

class NavRouteTest {

    @Test
    fun `Calendar route contains YearMonth`() {
        // given
        val yearMonth = YearMonth(2026, 1)

        // when
        val route = NavRoute.Calendar(yearMonth)

        // then
        assert(route.yearMonth == yearMonth)
        assert(route.yearMonth.year == 2026)
        assert(route.yearMonth.month.number == 1)
    }

    @Test
    fun `Calendar route is a NavRoute`() {
        // given
        val yearMonth = YearMonth(2026, 1)

        // when
        val route: NavRoute = NavRoute.Calendar(yearMonth)

        // then
        assert(route is NavRoute.Calendar)
    }

    @Test
    fun `Preview route contains LocalDate`() {
        // given
        val date = LocalDate(2026, 1, 15)

        // when
        val route = NavRoute.DiaryPreview(date)

        // then
        assert(route.date == date)
        assert(route.date.year == 2026)
        assert(route.date.month.number == 1)
        assert(route.date.dayOfMonth == 15)
    }

    @Test
    fun `Preview route is a NavRoute`() {
        // given
        val date = LocalDate(2026, 3, 20)

        // when
        val route: NavRoute = NavRoute.DiaryPreview(date)

        // then
        assert(route is NavRoute.DiaryPreview)
    }

    @Test
    fun `Edit route contains LocalDate`() {
        // given
        val date = LocalDate(2026, 2, 28)

        // when
        val route = NavRoute.DiaryEdit(date)

        // then
        assert(route.date == date)
        assert(route.date.year == 2026)
        assert(route.date.month.number == 2)
        assert(route.date.dayOfMonth == 28)
    }

    @Test
    fun `Edit route is a NavRoute`() {
        // given
        val date = LocalDate(2026, 12, 25)

        // when
        val route: NavRoute = NavRoute.DiaryEdit(date)

        // then
        assert(route is NavRoute.DiaryEdit)
    }

    @Test
    fun `Settings route is a NavRoute`() {
        // given & when
        val route: NavRoute = NavRoute.Settings

        // then
        assert(route is NavRoute.Settings)
    }

    @Test
    fun `Preview and Edit routes with same date are not equal`() {
        // given
        val date = LocalDate(2026, 1, 1)

        // when
        val diaryPreview = NavRoute.DiaryPreview(date)
        val diaryEdit = NavRoute.DiaryEdit(date)

        // then
        assert(diaryPreview != diaryEdit)
    }

    @Test
    fun `Calendar routes with different YearMonth are not equal`() {
        // given
        val yearMonth1 = YearMonth(2026, 1)
        val yearMonth2 = YearMonth(2026, 2)

        // when
        val route1 = NavRoute.Calendar(yearMonth1)
        val route2 = NavRoute.Calendar(yearMonth2)

        // then
        assert(route1 != route2)
    }

    @Test
    fun `Calendar routes with same YearMonth are equal`() {
        // given
        val yearMonth = YearMonth(2026, 5)

        // when
        val route1 = NavRoute.Calendar(yearMonth)
        val route2 = NavRoute.Calendar(yearMonth)

        // then
        assert(route1 == route2)
    }
}
