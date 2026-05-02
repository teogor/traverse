package dev.teogor.traverse.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/**
 * Entry point for the Traverse Journal demo app.
 *
 * ## M3 TODO — wire TraverseHost here:
 *
 * ```kotlin
 * @Composable
 * fun App() {
 *     MaterialTheme {
 *         TraverseHost(
 *             startDestination = Onboarding,
 *             transitions = TraverseTransitionSpec.horizontalSlide(),
 *         ) {
 *             // ── Onboarding (nested graph) ─────────────────────────────
 *             nested(startDestination = OnboardingWelcome, graphKey = Onboarding) {
 *                 screen<OnboardingWelcome> { OnboardingWelcomeScreen(onNext = { navigate(OnboardingFeatures) }) }
 *                 screen<OnboardingFeatures> { OnboardingFeaturesScreen(onNext = { navigate(OnboardingReady) }, onBack = { navigateUp() }) }
 *                 screen<OnboardingReady>   { OnboardingReadyScreen(onGetStarted = { launchAsNewRoot<Onboarding>(Home) }, onBack = { navigateUp() }) }
 *             }
 *             // ── Main screens ──────────────────────────────────────────
 *             screen<Home> {
 *                 CollectTraverseResultOnce<String>(RESULT_NEW_ENTRY_TITLE) { title -> /* add entry */ }
 *                 HomeScreen(onOpenEntry = { navigate(EntryDetail(it)) }, onNewEntry = { navigate(NewEntry) }, onSettings = { navigate(Settings) })
 *             }
 *             screen<EntryDetail> { dest ->
 *                 CollectTraverseResultOnce<String>(RESULT_SELECTED_TAG)  { tag -> /* apply tag */ }
 *                 CollectTraverseResultOnce<Boolean>(RESULT_DELETE_CONFIRMED) { confirmed -> if (confirmed) { /* delete + navigateUp */ } }
 *                 EntryDetailScreen(entryId = dest.entryId, onBack = { navigateUp() }, onDeleteRequest = { navigate(ConfirmDelete(dest.entryId)) }, onPickTag = { navigate(TagPicker) })
 *             }
 *             screen<NewEntry>  { NewEntryScreen(onSave = { title -> setResultAndNavigateUp(RESULT_NEW_ENTRY_TITLE, title) }, onCancel = { navigateUp() }) }
 *             screen<Settings> { SettingsScreen(onBack = { navigateUp() }, onGoHome = { popTo(Home) }) }
 *             // ── Overlays ──────────────────────────────────────────────
 *             dialog<ConfirmDelete> { dest -> ConfirmDeleteDialog(entryId = dest.entryId, onConfirm = { setResultAndNavigateUp(RESULT_DELETE_CONFIRMED, true) }, onDismiss = { setResultAndNavigateUp(RESULT_DELETE_CONFIRMED, false) }) }
 *             bottomSheet<TagPicker> { TagPickerSheet(onTagSelected = { tag -> setResultAndNavigateUp(RESULT_SELECTED_TAG, tag) }, onDismiss = { navigateUp() }) }
 *         }
 *     }
 * }
 * ```
 *
 * Until M3 ships, this renders a placeholder so the project still compiles across all platforms.
 */
@Composable
@Preview
fun App() {
    MaterialTheme {
        Text("Traverse Journal — navigation wiring coming in M3")
    }
}