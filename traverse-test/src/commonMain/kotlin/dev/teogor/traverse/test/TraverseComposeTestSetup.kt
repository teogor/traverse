package dev.teogor.traverse.test

import androidx.compose.runtime.Composable
import dev.teogor.traverse.compose.graph.TraverseGraphBuilder
import dev.teogor.traverse.compose.TraverseHost
import dev.teogor.traverse.core.Destination
import dev.teogor.traverse.core.navigator.NavOptions

/**
 * Self-contained Compose-navigation harness for UI tests.
 *
 * Wraps a [TraverseHost] with an injected [FakeTraverseNavigator] so you can drive navigation
 * programmatically and assert on the rendered result within any Compose test environment.
 *
 * ### Usage with `runComposeUiTest` (JetBrains Compose Multiplatform):
 * ```kotlin
 * @Test
 * fun homeScreen_navigatesToProfile() = runComposeUiTest {
 *     val setup = TraverseComposeTestSetup(Home) {
 *         screen<Home> { HomeScreen() }
 *         screen<Profile> { dest -> ProfileScreen(dest.userId) }
 *     }
 *     setContent { setup.Content() }
 *
 *     setup.navigate(Profile("42"))
 *     onNodeWithText("Profile 42").assertIsDisplayed()
 * }
 * ```
 *
 * ### Usage with Compose Android `createComposeRule()`:
 * ```kotlin
 * @get:Rule val composeRule = createComposeRule()
 *
 * @Test
 * fun homeScreen_navigatesToProfile() {
 *     val setup = TraverseComposeTestSetup(Home) {
 *         screen<Home> { HomeScreen() }
 *         screen<Profile> { dest -> ProfileScreen(dest.userId) }
 *     }
 *     composeRule.setContent { setup.Content() }
 *
 *     setup.navigate(Profile("42"))
 *     composeRule.onNodeWithText("Profile 42").assertIsDisplayed()
 * }
 * ```
 *
 * @param startDestination The initial route pushed onto the back stack.
 * @param graphBuilder     Declares the screens available to the navigator (same DSL as [TraverseHost]).
 */
public class TraverseComposeTestSetup(
    startDestination: Destination,
    private val graphBuilder: TraverseGraphBuilder.() -> Unit,
) {
    /**
     * The fake navigator driving this setup.
     * Use it for assertion helpers such as [assertNavigatedTo], [assertLastNavigatedTo], etc.
     */
    public val navigator: FakeTraverseNavigator = FakeTraverseNavigator(startDestination)

    /**
     * The current destination on the fake navigator's back stack.
     */
    public val currentDestination: Destination
        get() = navigator.currentDestination

    /**
     * Whether the navigator can pop back further than the start destination.
     */
    public val canNavigateUp: Boolean
        get() = navigator.canNavigateUp

    /**
     * Renders [TraverseHost] with the injected [FakeTraverseNavigator].
     *
     * Call this inside a `setContent { }` block of your Compose test rule.
     */
    @Composable
    public fun Content() {
        TraverseHost(
            startDestination = navigator.currentDestination,
            navigator = navigator,
            builder = graphBuilder,
        )
    }

    /**
     * Navigate to [destination].
     *
     * Delegates to [FakeTraverseNavigator.navigate] so the call is recorded and can be
     * asserted via [assertNavigatedTo] / [assertLastNavigatedTo].
     */
    public fun navigate(destination: Destination, options: NavOptions.() -> Unit = {}) {
        navigator.navigate(destination, options)
    }

    /**
     * Pop back up one level.
     *
     * Delegates to [FakeTraverseNavigator.navigateUp].
     */
    public fun navigateUp() {
        navigator.navigateUp()
    }

    /**
     * Reset all recorded navigate / navigateUp calls.
     *
     * Equivalent to `navigator.reset()` — useful between test steps.
     */
    public fun reset() {
        navigator.reset()
    }
}


