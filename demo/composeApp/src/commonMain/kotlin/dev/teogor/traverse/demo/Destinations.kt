package dev.teogor.traverse.demo

import dev.teogor.traverse.core.Destination
import kotlinx.serialization.Serializable

// ── Onboarding graph ─────────────────────────────────────────────────────────

/** Key for the onboarding nested graph. Navigate here to start onboarding. */
@Serializable data object Onboarding : Destination

/** First onboarding screen — welcome message and app introduction. */
@Serializable data object OnboardingWelcome : Destination

/** Second onboarding screen — highlights Traverse's key features. */
@Serializable data object OnboardingFeatures : Destination

/** Third onboarding screen — "Get started" action clears the stack and launches the main app. */
@Serializable data object OnboardingReady : Destination

// ── Main app ─────────────────────────────────────────────────────────────────

/** Home screen — shows all journal entries. The app's root after onboarding. */
@Serializable data object Home : Destination

/**
 * Entry detail screen — displays the full content of a single journal entry.
 *
 * Demonstrates typed navigation arguments: [entryId] is carried directly
 * on the destination without any extra argument holder.
 */
@Serializable data class EntryDetail(val entryId: String) : Destination

/**
 * New entry screen — a form for creating a journal entry.
 *
 * Demonstrates navigation results: when the user saves, this screen calls
 * `navigator.setResultAndNavigateUp("new_entry_title", title)` and the
 * Home screen collects it via `CollectTraverseResultOnce`.
 */
@Serializable data object NewEntry : Destination

/** Settings screen — app preferences. */
@Serializable data object Settings : Destination

// ── Overlay destinations ──────────────────────────────────────────────────────

/**
 * Confirm-delete dialog — asks the user to confirm deleting a journal entry.
 *
 * Demonstrates `dialog<T>` destinations: renders inside an `AlertDialog`
 * without a full back-stack push. Returns a Boolean result via
 * `setResultAndNavigateUp("delete_confirmed", true/false)`.
 */
@Serializable data class ConfirmDelete(val entryId: String) : Destination

/**
 * Tag picker bottom sheet — lets the user pick a tag for a journal entry.
 *
 * Demonstrates `bottomSheet<T>` destinations: renders inside a `ModalBottomSheet`.
 * Returns the selected tag string via `setResultAndNavigateUp("selected_tag", tag)`.
 */
@Serializable data object TagPicker : Destination

