package dev.teogor.traverse.test

import dev.teogor.traverse.core.Destination
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// ── Test destinations (local to this file) ────────────────────────────────────

private data object Home : Destination
private data object Feed : Destination
private data class Profile(val userId: String) : Destination
private data object Settings : Destination

// ── FakeTraverseNavigator — deep link calls ───────────────────────────────────

class FakeTraverseNavigatorDeepLinkTest {

    @Test
    fun navigateToDeepLink_recordsUri() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigateToDeepLink("traverse://demo/target/42")
        assertEquals(listOf("traverse://demo/target/42"), fake.deepLinkCalls)
    }

    @Test
    fun navigateToDeepLink_returnsFalse_sinceNoRegistryInFake() {
        val fake = FakeTraverseNavigator(Home)
        val result = fake.navigateToDeepLink("traverse://demo/target/42")
        assertFalse(result)
    }

    @Test
    fun navigateToDeepLink_multipleCalls_allRecorded() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigateToDeepLink("traverse://demo/a")
        fake.navigateToDeepLink("traverse://demo/b")
        assertEquals(2, fake.deepLinkCalls.size)
    }

    @Test
    fun assertDeepLinkNavigatedTo_passesWhenUriMatches() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigateToDeepLink("traverse://demo/target/hello")
        fake.assertDeepLinkNavigatedTo("traverse://demo/target/hello")
    }

    @Test
    fun assertDeepLinkNavigated_passesWhenAtLeastOneCall() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigateToDeepLink("traverse://any/uri")
        fake.assertDeepLinkNavigated()
    }

    @Test
    fun reset_clearsDeepLinkCalls() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigateToDeepLink("traverse://demo/target/1")
        fake.reset()
        assertEquals(emptyList(), fake.deepLinkCalls)
    }
}

class FakeTraverseNavigatorTest {

    // ── navigate() ────────────────────────────────────────────────────────────

    @Test
    fun navigate_pushesDestinationOntoStack() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigate(Feed)
        assertEquals(listOf(Home, Feed), fake.backStack)
    }

    @Test
    fun navigate_recordsCall() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigate(Profile("42"))
        assertEquals(1, fake.navigateCalls.size)
        assertTrue(fake.navigateCalls.first().destination is Profile)
    }

    @Test
    fun navigate_withPopUpTo_removesEntriesAboveTarget() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigate(Feed)
        fake.navigate(Profile("1"))
        // popUpTo = Feed (inclusive = false) → removes Profile, then pushes Settings
        fake.navigate(Settings) { popUpTo = Feed }
        assertEquals(listOf(Home, Feed, Settings), fake.backStack)
    }

    @Test
    fun navigate_withPopUpToInclusive_removesTargetToo() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigate(Feed)
        fake.navigate(Profile("1"))
        fake.navigate(Settings) { popUpTo = Feed; popUpToInclusive = true }
        assertEquals(listOf(Home, Settings), fake.backStack)
    }

    @Test
    fun navigate_withLaunchSingleTop_doesNotDuplicateTopDestination() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigate(Feed)
        fake.navigate(Feed) { launchSingleTop = true }
        // Feed should not be pushed a second time
        assertEquals(listOf(Home, Feed), fake.backStack)
    }

    @Test
    fun navigate_withLaunchSingleTop_pushesWhenTopIsDifferent() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigate(Feed)
        // Top is Feed, not Profile → should push
        fake.navigate(Profile("1")) { launchSingleTop = true }
        assertEquals(listOf(Home, Feed, Profile("1")), fake.backStack)
    }

    // ── navigateUp() ──────────────────────────────────────────────────────────

    @Test
    fun navigateUp_popsTopEntry_returnsTrue() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigate(Feed)
        val result = fake.navigateUp()
        assertTrue(result)
        assertEquals(listOf(Home), fake.backStack)
        assertEquals(1, fake.navigateUpCount)
    }

    @Test
    fun navigateUp_atRoot_returnsFalse_doesNotModifyStack() {
        val fake = FakeTraverseNavigator(Home)
        val result = fake.navigateUp()
        assertFalse(result)
        assertEquals(listOf(Home), fake.backStack)
        assertEquals(0, fake.navigateUpCount)
    }

    // ── popTo() ───────────────────────────────────────────────────────────────

    @Test
    fun popTo_removesEntriesAboveTarget() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigate(Feed)
        fake.navigate(Profile("1"))
        fake.navigate(Settings)
        fake.popTo(Feed)
        assertEquals(listOf(Home, Feed), fake.backStack)
    }

    @Test
    fun popTo_inclusive_removesTargetAndAbove() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigate(Feed)
        fake.navigate(Profile("1"))
        fake.popTo(Feed, inclusive = true)
        assertEquals(listOf(Home), fake.backStack)
    }

    @Test
    fun popTo_recordsCall() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigate(Feed)
        fake.popTo(Home)
        assertEquals(1, fake.popToCalls.size)
        assertEquals(Home, fake.popToCalls.first().destination)
        assertEquals(false, fake.popToCalls.first().inclusive)
    }

    @Test
    fun popTo_destinationNotOnStack_returnsFalse() {
        val fake = FakeTraverseNavigator(Home)
        val result = fake.popTo(Settings) // Settings is not on stack
        assertFalse(result)
        assertTrue(fake.popToCalls.isEmpty())
    }

    // ── setResult / clearResult ───────────────────────────────────────────────

    @Test
    fun setResult_storesValue() {
        val fake = FakeTraverseNavigator(Home)
        fake.setResult("key", "hello")
        assertEquals("hello", fake.results["key"])
    }

    @Test
    fun clearResult_removesValue() {
        val fake = FakeTraverseNavigator(Home)
        fake.setResult("key", "hello")
        fake.clearResult("key")
        assertFalse(fake.results.containsKey("key"))
    }

    // ── reset() ───────────────────────────────────────────────────────────────

    @Test
    fun reset_clearsNavigationHistoryButNotBackStack() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigate(Feed)
        fake.navigateUp()
        fake.navigate(Settings)
        fake.setResult("k", "v")

        fake.reset()

        assertTrue(fake.navigateCalls.isEmpty())
        assertEquals(0, fake.navigateUpCount)
        assertTrue(fake.popToCalls.isEmpty())
        assertTrue(fake.results.isEmpty())
        // Back stack is NOT cleared by reset — still has Home + Settings
        assertEquals(listOf(Home, Settings), fake.backStack)
    }

    // ── canNavigateUp / currentDestination ───────────────────────────────────

    @Test
    fun canNavigateUp_falseAtRoot() {
        assertFalse(FakeTraverseNavigator(Home).canNavigateUp)
    }

    @Test
    fun canNavigateUp_trueWithMultipleEntries() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigate(Feed)
        assertTrue(fake.canNavigateUp)
    }

    @Test
    fun currentDestination_returnsTopOfStack() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigate(Profile("99"))
        assertTrue(fake.currentDestination is Profile)
        assertEquals("99", (fake.currentDestination as Profile).userId)
    }
}

// ── TraverseAssertions ────────────────────────────────────────────────────────

class TraverseAssertionsTest {

    @Test
    fun assertNavigatedTo_passesWhenMatches() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigate(Profile("1"))
        fake.assertNavigatedTo<Profile>()
    }

    @Test
    fun assertLastNavigatedTo_passesForLastCall() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigate(Feed)
        fake.navigate(Profile("1"))
        fake.assertLastNavigatedTo<Profile>()
    }

    @Test
    fun assertCurrentDestination_passesWhenTopMatches() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigate(Settings)
        fake.assertCurrentDestination<Settings>()
    }

    @Test
    fun assertNavigatedUp_passesWithCorrectCount() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigate(Feed)
        fake.navigate(Settings)
        fake.navigateUp()
        fake.navigateUp()
        fake.assertNavigatedUp(times = 2)
    }

    @Test
    fun assertPoppedTo_passesWhenMatch() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigate(Feed)
        fake.navigate(Settings)
        fake.popTo(Home)
        fake.assertPoppedTo<Home>()
    }

    @Test
    fun assertPoppedTo_inclusive_passesWhenMatch() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigate(Feed)
        fake.popTo(Home, inclusive = true)
        fake.assertPoppedTo<Home>(inclusive = true)
    }

    @Test
    fun assertResultSet_passesWhenKeyAndValueMatch() {
        val fake = FakeTraverseNavigator(Home)
        fake.setResult("color", "Red")
        fake.assertResultSet("color", "Red")
    }

    @Test
    fun assertResultSet_noValueArg_passesWhenKeyExists() {
        val fake = FakeTraverseNavigator(Home)
        fake.setResult("color", null)
        fake.assertResultSet("color")
    }

    @Test
    fun assertBackStack_passesWhenExactMatch() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigate(Feed)
        fake.navigate(Profile("7"))
        fake.assertBackStack(Home, Feed, Profile("7"))
    }

    @Test
    fun assertNoNavigation_passesOnFreshNavigator() {
        FakeTraverseNavigator(Home).assertNoNavigation()
    }

    @Test
    fun assertNoNavigation_passesAfterReset() {
        val fake = FakeTraverseNavigator(Home)
        fake.navigate(Feed)
        fake.navigateUp()
        fake.reset()
        fake.assertNoNavigation()
    }
}


