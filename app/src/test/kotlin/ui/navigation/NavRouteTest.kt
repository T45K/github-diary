package ui.navigation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NavRouteTest {

    @Test
    fun `NavRoute has four routes`() {
        assertEquals(4, NavRoute.entries.size)
    }

    @Test
    fun `NavRoute contains Calendar`() {
        assertEquals("Calendar", NavRoute.Calendar.name)
    }

    @Test
    fun `NavRoute contains Preview`() {
        assertEquals("Preview", NavRoute.Preview.name)
    }

    @Test
    fun `NavRoute contains Edit`() {
        assertEquals("Edit", NavRoute.Edit.name)
    }

    @Test
    fun `NavRoute contains Settings`() {
        assertEquals("Settings", NavRoute.Settings.name)
    }

    @Test
    fun `NavRoute valueOf returns correct route`() {
        assertEquals(NavRoute.Calendar, NavRoute.valueOf("Calendar"))
        assertEquals(NavRoute.Preview, NavRoute.valueOf("Preview"))
        assertEquals(NavRoute.Edit, NavRoute.valueOf("Edit"))
        assertEquals(NavRoute.Settings, NavRoute.valueOf("Settings"))
    }
}
