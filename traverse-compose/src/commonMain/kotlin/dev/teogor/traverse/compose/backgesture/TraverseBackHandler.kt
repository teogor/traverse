package dev.teogor.traverse.compose.backgesture

import androidx.compose.runtime.Composable

/**
 * Platform-specific back-gesture handler.
 *
 * - **Android**: wires `BackHandler` from `activity-compose`.
 * - **iOS**: no-op (Compose iOS handles swipe-back natively via UINavigationController).
 * - **Desktop (JVM)**: no-op in alpha — keyboard shortcut support planned.
 * - **Web (wasmJs)**: no-op in alpha — browser back support planned.
 */
@Composable
internal expect fun TraverseBackHandler(enabled: Boolean, onBack: () -> Unit)

