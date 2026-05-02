package dev.teogor.traverse.core.navigator

import dev.teogor.traverse.core.Destination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// ── Fixtures ──────────────────────────────────────────────────────────────────

@Serializable private data object Home : Destination
@Serializable private data object Feed : Destination
@Serializable private data class Profile(val userId: String) : Destination
@Serializable private data object Settings : Destination
@Serializable private data object Root : Destination

/** Minimal in-test fake — records navigate calls and maintains a real back stack. */
private class TestNavigator(vararg initial: Destination) : TraverseNavigator {

    private val stack: MutableList<Destination> = initial.toMutableList()
    val navigateCalls = mutableListOf<Pair<Destination, NavOptions>>()

    override val backStack: List<Destination> get() = stack.toList()

    override fun navigate(destination: Destination, builder: NavOptions.() -> Unit) {
        val options = NavOptions().apply(builder)
        navigateCalls += destination to options

        // Honour popUpTo
        val target = options.popUpTo
        if (target != null) {
            val idx = stack.indexOfLast { it == target }
            if (idx >= 0) {
                val removeFrom = if (options.popUpToInclusive) idx else idx + 1
                while (stack.size > removeFrom) stack.removeLastOrNull()
            } else {
                stack.clear()
            }
        }
        // Honour launchSingleTop
        if (options.launchSingleTop && stack.lastOrNull() == destination) return
        stack.add(destination)
    }

    override fun navigateUp(): Boolean {
        if (stack.size <= 1) return false
        stack.removeLastOrNull()
        return true
    }

    override fun popTo(destination: Destination, inclusive: Boolean): Boolean {
        val idx = stack.indexOfLast { it == destination }
        if (idx < 0) return false
        val removeFrom = if (inclusive) idx else idx + 1
        while (stack.size > removeFrom) stack.removeLastOrNull()
        return true
    }

    override fun <T> setResult(key: String, value: T) = Unit
    override fun clearResult(key: String) = Unit
    override fun <T> observeResult(key: String): Flow<T> = emptyFlow()
}

// ── Tests ─────────────────────────────────────────────────────────────────────

class TraverseNavigatorExtensionsTest {

    @Test
    fun navigateAndClearUpTo_removesEntriesUpToAndIncludingT_thenPushesDestination() {
        val nav = TestNavigator(Home, Feed, Profile("1"))
        nav.navigateAndClearUpTo<Home>(Settings)
        // Home popped (inclusive), Settings pushed
        assertEquals(listOf(Settings), nav.backStack)
    }

    @Test
    fun navigateAndClearUpTo_whenTNotOnStack_justNavigatesWithoutPopping() {
        val nav = TestNavigator(Feed, Profile("1"))
        nav.navigateAndClearUpTo<Home>(Settings)
        // Home not found → popUpTo = null → no pop, Settings simply pushed on top
        assertEquals(listOf(Feed, Profile("1"), Settings), nav.backStack)
    }

    @Test
    fun launchAsNewRoot_clearsStackAndPushesDestination() {
        val nav = TestNavigator(Home, Feed, Profile("1"), Settings)
        nav.launchAsNewRoot<Root>(Home)
        assertEquals(listOf(Home), nav.backStack)
    }

    @Test
    fun isOnBackStack_returnsTrueWhenTypePresent() {
        val nav = TestNavigator(Home, Feed, Profile("42"))
        assertTrue(nav.isOnBackStack<Profile>())
    }

    @Test
    fun isOnBackStack_returnsFalseWhenTypeAbsent() {
        val nav = TestNavigator(Home, Feed)
        assertFalse(nav.isOnBackStack<Profile>())
    }

    @Test
    fun entriesOf_returnsAllMatchingEntries() {
        val nav = TestNavigator(Home, Profile("1"), Feed, Profile("2"))
        val profiles = nav.entriesOf<Profile>()
        assertEquals(listOf(Profile("1"), Profile("2")), profiles)
    }

    @Test
    fun entriesOf_returnsEmptyWhenNoneMatch() {
        val nav = TestNavigator(Home, Feed)
        assertEquals(emptyList(), nav.entriesOf<Profile>())
    }

    @Test
    fun canNavigateUp_falseAtRoot() {
        val nav = TestNavigator(Home)
        assertFalse(nav.canNavigateUp)
    }

    @Test
    fun canNavigateUp_trueWithMultipleEntries() {
        val nav = TestNavigator(Home, Feed)
        assertTrue(nav.canNavigateUp)
    }

    @Test
    fun currentDestination_returnsLastEntry() {
        val nav = TestNavigator(Home, Feed, Profile("99"))
        assertEquals(Profile("99"), nav.currentDestination)
    }

    @Test
    fun navigateUp_popsTopEntry() {
        val nav = TestNavigator(Home, Feed)
        val result = nav.navigateUp()
        assertTrue(result)
        assertEquals(listOf(Home), nav.backStack)
    }

    @Test
    fun navigateUp_returnsFalseAtRoot() {
        val nav = TestNavigator(Home)
        val result = nav.navigateUp()
        assertFalse(result)
        assertEquals(listOf(Home), nav.backStack)
    }
}


