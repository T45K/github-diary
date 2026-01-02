package ui

import core.time.DateProvider
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ui.navigation.NavRoute

class AppViewModelTest {

    private lateinit var dateProvider: DateProvider
    private lateinit var viewModel: AppViewModel

    @BeforeEach
    fun setup() {
        val fixedInstant = Instant.parse("2026-01-02T12:00:00Z")
        val fixedClock = object : Clock {
            override fun now(): Instant = fixedInstant
        }
        dateProvider = DateProvider(fixedClock)
        viewModel = AppViewModel(dateProvider)
    }

    @Test
    fun `initial state has Calendar route`() {
        assertEquals(NavRoute.Calendar.name, viewModel.state.value.currentRoute)
    }

    @Test
    fun `initial state has today as selected date`() {
        assertEquals(LocalDate(2026, 1, 2), viewModel.state.value.selectedDate)
    }

    @Test
    fun `initial state is not loading`() {
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `initial state has no error message`() {
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `navigate changes current route`() {
        viewModel.navigate(NavRoute.Settings.name)

        assertEquals(NavRoute.Settings.name, viewModel.state.value.currentRoute)
    }

    @Test
    fun `navigate with date updates selected date`() {
        val newDate = LocalDate(2026, 2, 15)
        viewModel.navigate(NavRoute.Preview.name, newDate)

        assertEquals(NavRoute.Preview.name, viewModel.state.value.currentRoute)
        assertEquals(newDate, viewModel.state.value.selectedDate)
    }

    @Test
    fun `navigate without date keeps current selected date`() {
        val originalDate = viewModel.state.value.selectedDate
        viewModel.navigate(NavRoute.Edit.name)

        assertEquals(originalDate, viewModel.state.value.selectedDate)
    }

    @Test
    fun `goToday updates selected date to today`() {
        viewModel.navigate(NavRoute.Preview.name, LocalDate(2025, 12, 25))

        viewModel.goToday()

        assertEquals(LocalDate(2026, 1, 2), viewModel.state.value.selectedDate)
    }

    @Test
    fun `goToCalendarToday navigates to Calendar and sets today`() {
        viewModel.navigate(NavRoute.Settings.name, LocalDate(2025, 6, 1))

        viewModel.goToCalendarToday()

        assertEquals(NavRoute.Calendar.name, viewModel.state.value.currentRoute)
        assertEquals(LocalDate(2026, 1, 2), viewModel.state.value.selectedDate)
    }

    @Test
    fun `setLoading updates isLoading state`() {
        viewModel.setLoading(true)
        assertEquals(true, viewModel.state.value.isLoading)

        viewModel.setLoading(false)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `setError updates errorMessage state`() {
        viewModel.setError("Something went wrong")
        assertEquals("Something went wrong", viewModel.state.value.errorMessage)

        viewModel.setError(null)
        assertNull(viewModel.state.value.errorMessage)
    }
}
