package dev.teogor.traverse.test

import dev.teogor.traverse.core.Destination
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// ── Fixtures ──────────────────────────────────────────────────────────────────

private data object SetupHome : Destination

private data class SetupDetail(val id: String) : Destination

class TraverseComposeTestSetupTest {

    private fun setup() = TraverseComposeTestSetup(SetupHome) {
        // graph builder is required but can be empty for these unit tests
    }

    @Test
    fun initialCurrentDestinationIsStartDestination() {
        val s = setup()
        assertEquals<Destination>(SetupHome, s.currentDestination)
    }

    @Test
    fun canNotNavigateUpFromStartDestination() {
        val s = setup()
        assertFalse(s.canNavigateUp, "Should not be able to navigate up from the start destination")
    }

    @Test
    fun navigatePushesDestinationAndRecordsCall() {
        val s = setup()
        s.navigate(SetupDetail("42"))

        assertEquals<Destination>(SetupDetail("42"), s.currentDestination)
        s.navigator.assertNavigatedTo<SetupDetail>()
        assertTrue(s.canNavigateUp)
    }

    @Test
    fun navigateUpPopsDestination() {
        val s = setup()
        s.navigate(SetupDetail("99"))
        s.navigateUp()

        assertEquals<Destination>(SetupHome, s.currentDestination)
        assertFalse(s.canNavigateUp)
    }

    @Test
    fun resetClearsRecordedCallsButKeepsBackStack() {
        val s = setup()
        s.navigate(SetupDetail("1"))
        s.reset()

        // Back stack unchanged (still at SetupDetail("1"))
        assertEquals<Destination>(SetupDetail("1"), s.currentDestination)
        // Recorded calls wiped
        assertTrue(s.navigator.navigateCalls.isEmpty(), "navigateCalls should be empty after reset")
    }

    @Test
    fun navigatorDelegationIsSameInstance() {
        val s = setup()
        s.navigate(SetupDetail("7"))

        // Both the property and the navigator.backStack reflect the same state
        assertEquals(s.navigator.backStack.last(), s.currentDestination)
    }
}


