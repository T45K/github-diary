package ui.navigation

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NavRouteTest {

    @Test
    fun `Calendar route is a NavRoute`() {
        val route: NavRoute = NavRoute.Calendar
        assertTrue(route is NavRoute.Calendar)
    }

    @Test
    fun `Preview route contains date components`() {
        val route = NavRoute.Preview(2026, 1, 15)

        assertEquals(2026, route.year)
        assertEquals(1, route.month)
        assertEquals(15, route.day)
    }

    @Test
    fun `Preview route can be created from LocalDate`() {
        val date = LocalDate(2026, 3, 20)
        val route = NavRoute.Preview(date)

        assertEquals(2026, route.year)
        assertEquals(3, route.month)
        assertEquals(20, route.day)
    }

    @Test
    fun `Preview route toLocalDate returns correct date`() {
        val route = NavRoute.Preview(2026, 5, 10)
        val date = route.toLocalDate()

        assertEquals(LocalDate(2026, 5, 10), date)
    }

    @Test
    fun `Edit route contains date components`() {
        val route = NavRoute.Edit(2026, 2, 28)

        assertEquals(2026, route.year)
        assertEquals(2, route.month)
        assertEquals(28, route.day)
    }

    @Test
    fun `Edit route can be created from LocalDate`() {
        val date = LocalDate(2026, 12, 25)
        val route = NavRoute.Edit(date)

        assertEquals(2026, route.year)
        assertEquals(12, route.month)
        assertEquals(25, route.day)
    }

    @Test
    fun `Edit route toLocalDate returns correct date`() {
        val route = NavRoute.Edit(2026, 7, 4)
        val date = route.toLocalDate()

        assertEquals(LocalDate(2026, 7, 4), date)
    }

    @Test
    fun `Settings route is a NavRoute`() {
        val route: NavRoute = NavRoute.Settings
        assertTrue(route is NavRoute.Settings)
    }

    @Test
    fun `Preview and Edit routes with same date are not equal`() {
        val preview = NavRoute.Preview(2026, 1, 1)
        val edit = NavRoute.Edit(2026, 1, 1)

        assertTrue(preview != edit)
    }
}
