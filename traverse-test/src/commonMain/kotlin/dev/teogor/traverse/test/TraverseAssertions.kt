package dev.teogor.traverse.test

import dev.teogor.traverse.core.Destination
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Asserts that [FakeTraverseNavigator.navigate] was called at least once with a destination
 * of type [T] since the last [FakeTraverseNavigator.reset].
 *
 * ```kotlin
 * fake.assertNavigatedTo<Profile>()
 * ```
 */
public inline fun <reified T : Destination> FakeTraverseNavigator.assertNavigatedTo() {
    val match = navigateCalls.any { it.destination is T }
    assertTrue(
        match,
        "Expected at least one navigate() to ${T::class.simpleName} but got: " +
            navigateCalls.map { it.destination::class.simpleName }.ifEmpty { listOf("<none>") },
    )
}

/**
 * Asserts that the last [FakeTraverseNavigator.navigate] call was to a destination of type [T].
 *
 * ```kotlin
 * fake.assertLastNavigatedTo<Profile>()
 * ```
 */
public inline fun <reified T : Destination> FakeTraverseNavigator.assertLastNavigatedTo() {
    val last = navigateCalls.lastOrNull()
    assertTrue(
        last?.destination is T,
        "Expected last navigate() to be ${T::class.simpleName} but was " +
            (last?.destination?.let { it::class.simpleName } ?: "<none>"),
    )
}

/**
 * Asserts that the current top of the back stack is a destination of type [T].
 *
 * ```kotlin
 * fake.assertCurrentDestination<Home>()
 * ```
 */
public inline fun <reified T : Destination> FakeTraverseNavigator.assertCurrentDestination() {
    assertTrue(
        currentDestination is T,
        "Expected current destination to be ${T::class.simpleName} " +
            "but was ${currentDestination::class.simpleName}",
    )
}

/**
 * Asserts that [FakeTraverseNavigator.navigateUp] was called exactly [times] time(s).
 *
 * @param times Expected call count. Defaults to `1`.
 *
 * ```kotlin
 * fake.assertNavigatedUp()          // called once
 * fake.assertNavigatedUp(times = 3) // called three times
 * ```
 */
public fun FakeTraverseNavigator.assertNavigatedUp(times: Int = 1) {
    assertEquals(
        expected = times,
        actual = navigateUpCount,
        message = "Expected navigateUp() to be called $times time(s) but was $navigateUpCount",
    )
}

/**
 * Asserts that [FakeTraverseNavigator.popTo] was called with a destination of type [T]
 * and the given [inclusive] flag.
 *
 * @param inclusive Expected value of the `inclusive` parameter. Defaults to `false`.
 *
 * ```kotlin
 * fake.assertPoppedTo<Home>()
 * fake.assertPoppedTo<Home>(inclusive = true)
 * ```
 */
public inline fun <reified T : Destination> FakeTraverseNavigator.assertPoppedTo(
    inclusive: Boolean = false,
) {
    val match = popToCalls.any { it.destination is T && it.inclusive == inclusive }
    assertTrue(
        match,
        "Expected popTo(${T::class.simpleName}, inclusive=$inclusive) but actual calls: " +
            popToCalls
                .map { "${it.destination::class.simpleName}(inclusive=${it.inclusive})" }
                .ifEmpty { listOf("<none>") },
    )
}

/**
 * Asserts that [FakeTraverseNavigator.setResult] was called with [key] and [value].
 *
 * @param key   The result key.
 * @param value The expected value. Pass `null` to assert the key exists with a null value.
 *
 * ```kotlin
 * fake.assertResultSet("selected_color", "Red")
 * ```
 */
public fun FakeTraverseNavigator.assertResultSet(key: String, value: Any? = null) {
    assertTrue(
        results.containsKey(key),
        "Expected result key \"$key\" to be set. Currently set keys: " +
            results.keys.toList().ifEmpty { listOf("<none>") },
    )
    assertEquals(
        expected = value,
        actual = results[key],
        message = "Expected results[\"$key\"] = $value but was ${results[key]}",
    )
}

/**
 * Asserts that the back stack matches [expected] exactly, in order.
 *
 * ```kotlin
 * fake.assertBackStack(Home, Profile("42"))
 * ```
 */
public fun FakeTraverseNavigator.assertBackStack(vararg expected: Destination) {
    assertEquals(
        expected = expected.toList(),
        actual = backStack,
        message = "Back stack mismatch.\n  Expected: ${expected.map { it::class.simpleName }}" +
            "\n  Actual:   ${backStack.map { it::class.simpleName }}",
    )
}

/**
 * Asserts that no navigation calls ([navigate], [navigateUp], or [popTo]) were made.
 *
 * ```kotlin
 * fake.assertNoNavigation()
 * ```
 */
public fun FakeTraverseNavigator.assertNoNavigation() {
    assertTrue(
        navigateCalls.isEmpty(),
        "Expected no navigate() calls but got: " +
            navigateCalls.map { it.destination::class.simpleName },
    )
    assertEquals(
        expected = 0,
        actual = navigateUpCount,
        message = "Expected no navigateUp() calls but was called $navigateUpCount time(s)",
    )
    assertTrue(
        popToCalls.isEmpty(),
        "Expected no popTo() calls but got: " +
            popToCalls.map { "${it.destination::class.simpleName}(inclusive=${it.inclusive})" },
    )
}

/**
 * Asserts that [navigateToDeepLink] was called with exactly [expectedUri].
 *
 * ```kotlin
 * fake.assertDeepLinkNavigatedTo("traverse://demo/target/42")
 * ```
 */
public fun FakeTraverseNavigator.assertDeepLinkNavigatedTo(expectedUri: String) {
    assertTrue(
        deepLinkCalls.contains(expectedUri),
        "Expected navigateToDeepLink(\"$expectedUri\") to be called. " +
            "Actual calls: ${deepLinkCalls.ifEmpty { listOf("<none>") }}",
    )
}

/**
 * Asserts that [navigateToDeepLink] was called at least once.
 *
 * ```kotlin
 * fake.assertDeepLinkNavigated()
 * ```
 */
public fun FakeTraverseNavigator.assertDeepLinkNavigated() {
    assertTrue(
        deepLinkCalls.isNotEmpty(),
        "Expected at least one navigateToDeepLink() call but none were recorded.",
    )
}

