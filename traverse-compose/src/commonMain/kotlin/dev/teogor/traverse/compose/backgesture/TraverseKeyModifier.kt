package dev.teogor.traverse.compose.backgesture

import androidx.compose.ui.Modifier

/**
 * Platform-specific Modifier extension that intercepts keyboard back-navigation shortcuts.
 *
 * Applied to the root container of [TraverseAnimatedHost] to intercept key events before
 * they reach any child composable.
 *
 * - **Android** — no-op; `TraverseBackHandler` via `BackHandler` handles system back already.
 * - **iOS** — no-op; the UIKit layer handles swipe-back natively.
 * - **Desktop (JVM)** — intercepts `Escape` (and optionally `Alt+Left`) via `onPreviewKeyEvent`.
 * - **Browser (JS / wasmJs)** — no-op stub; browser history API integration planned.
 *
 * @param enabled Whether back navigation is currently possible.
 * @param onBack  Called when the user triggers the keyboard back gesture.
 */
internal expect fun Modifier.traverseBackKeyModifier(
    enabled: Boolean,
    onBack: () -> Unit,
): Modifier

