package ui.navigation

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NavRouteTest {

    @Test
    fun `Calendar route contains YearMonth`() {
        val yearMonth = YearMonth(2026, 1)
        val route = NavRoute.Calendar(yearMonth)

        assertEquals(yearMonth, route.yearMonth)
        assertEquals(2026, route.yearMonth.year)
        assertEquals(1, route.yearMonth.month.number)
    }

    @Test
    fun `Calendar route is a NavRoute`() {
        val route: NavRoute = NavRoute.Calendar(YearMonth(2026, 1))
        assertTrue(route is NavRoute.Calendar)
    }

    @Test
    fun `Preview route contains LocalDate`() {
        val date = LocalDate(2026, 1, 15)
        val route = NavRoute.Preview(date)

        assertEquals(date, route.date)
        assertEquals(2026, route.date.year)
        assertEquals(1, route.date.month.number)
        assertEquals(15, route.date.day)
    }

    @Test
    fun `Preview route is a NavRoute`() {
        val route: NavRoute = NavRoute.Preview(LocalDate(2026, 3, 20))
        assertTrue(route is NavRoute.Preview)
    }

    @Test
    fun `Edit route contains LocalDate`() {
        val date = LocalDate(2026, 2, 28)
        val route = NavRoute.Edit(date)

        assertEquals(date, route.date)
        assertEquals(2026, route.date.year)
        assertEquals(2, route.date.month.number)
        assertEquals(28, route.date.day)
    }

    @Test
    fun `Edit route is a NavRoute`() {
        val route: NavRoute = NavRoute.Edit(LocalDate(2026, 12, 25))
        assertTrue(route is NavRoute.Edit)
    }

    @Test
    fun `Settings route is a NavRoute`() {
        val route: NavRoute = NavRoute.Settings
        assertTrue(route is NavRoute.Settings)
    }

    @Test
    fun `Preview and Edit routes with same date are not equal`() {
        val date = LocalDate(2026, 1, 1)
        val preview = NavRoute.Preview(date)
        val edit = NavRoute.Edit(date)

        assertTrue(preview != edit)
    }

    @Test
    fun `Calendar routes with different YearMonth are not equal`() {
        val route1 = NavRoute.Calendar(YearMonth(2026, 1))
        val route2 = NavRoute.Calendar(YearMonth(2026, 2))

        assertTrue(route1 != route2)
    }

    @Test
    fun `Calendar routes with same YearMonth are equal`() {
        val route1 = NavRoute.Calendar(YearMonth(2026, 5))
        val route2 = NavRoute.Calendar(YearMonth(2026, 5))

        assertEquals(route1, route2)
    }
}
