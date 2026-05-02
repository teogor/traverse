package dev.teogor.traverse.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.teogor.traverse.compose.TraverseHost
import dev.teogor.traverse.compose.navigator.LocalTraverseNavigator
import dev.teogor.traverse.compose.result.CollectTraverseResultOnce
import dev.teogor.traverse.compose.transition.TraverseTransitionSpec
import dev.teogor.traverse.core.navigator.launchAsNewRoot
import dev.teogor.traverse.demo.dialog.ConfirmDeleteDialog
import dev.teogor.traverse.demo.onboarding.OnboardingFeaturesScreen
import dev.teogor.traverse.demo.onboarding.OnboardingReadyScreen
import dev.teogor.traverse.demo.onboarding.OnboardingWelcomeScreen
import dev.teogor.traverse.demo.screen.EntryDetailScreen
import dev.teogor.traverse.demo.screen.HomeScreen
import dev.teogor.traverse.demo.screen.NewEntryScreen
import dev.teogor.traverse.demo.screen.RESULT_DELETE_CONFIRMED
import dev.teogor.traverse.demo.screen.RESULT_NEW_ENTRY_TITLE
import dev.teogor.traverse.demo.screen.RESULT_SELECTED_TAG
import dev.teogor.traverse.demo.screen.SettingsScreen
import dev.teogor.traverse.demo.sheet.TagPickerSheet

@Composable
fun App() {
    MaterialTheme {
        TraverseHost(
            startDestination = Onboarding,
            transitions = TraverseTransitionSpec.horizontalSlide(),
        ) {
            nested(startDestination = OnboardingWelcome, graphKey = Onboarding) {
                screen<OnboardingWelcome> {
                    val nav = LocalTraverseNavigator.current
                    OnboardingWelcomeScreen(onNext = { nav.navigate(OnboardingFeatures) })
                }
                screen<OnboardingFeatures> {
                    val nav = LocalTraverseNavigator.current
                    OnboardingFeaturesScreen(
                        onNext = { nav.navigate(OnboardingReady) },
                        onBack = { nav.navigateUp() },
                    )
                }
                screen<OnboardingReady> {
                    val nav = LocalTraverseNavigator.current
                    OnboardingReadyScreen(
                        onGetStarted = { nav.launchAsNewRoot<Onboarding>(Home) },
                        onBack = { nav.navigateUp() },
                    )
                }
            }

            screen<Home> {
                val nav = LocalTraverseNavigator.current
                var newTitle by remember { mutableStateOf<String?>(null) }
                CollectTraverseResultOnce<String>(RESULT_NEW_ENTRY_TITLE) { newTitle = it }
                HomeScreen(
                    onOpenEntry = { id -> nav.navigate(EntryDetail(id)) },
                    onNewEntry = { nav.navigate(NewEntry) },
                    onSettings = { nav.navigate(Settings) },
                    newEntryTitle = newTitle,
                )
            }

            screen<EntryDetail> { dest ->
                val nav = LocalTraverseNavigator.current
                var tag by remember { mutableStateOf<String?>(null) }
                CollectTraverseResultOnce<String>(RESULT_SELECTED_TAG) { tag = it }
                CollectTraverseResultOnce<Boolean>(RESULT_DELETE_CONFIRMED) { if (it) nav.navigateUp() }
                EntryDetailScreen(
                    entryId = dest.entryId,
                    onBack = { nav.navigateUp() },
                    onDeleteRequest = { nav.navigate(ConfirmDelete(dest.entryId)) },
                    onPickTag = { nav.navigate(TagPicker) },
                    currentTag = tag,
                )
            }

            screen<NewEntry> {
                val nav = LocalTraverseNavigator.current
                NewEntryScreen(
                    onSave = { title ->
                        nav.setResult(RESULT_NEW_ENTRY_TITLE, title)
                        nav.navigateUp()
                    },
                    onCancel = { nav.navigateUp() },
                )
            }

            screen<Settings> {
                val nav = LocalTraverseNavigator.current
                SettingsScreen(
                    onBack = { nav.navigateUp() },
                    onGoHome = { nav.popTo(Home, inclusive = false) },
                )
            }

            dialog<ConfirmDelete> { dest ->
                val nav = LocalTraverseNavigator.current
                ConfirmDeleteDialog(
                    entryId = dest.entryId,
                    onConfirm = {
                        nav.setResult(RESULT_DELETE_CONFIRMED, true)
                        nav.navigateUp()
                    },
                    onDismiss = {
                        nav.setResult(RESULT_DELETE_CONFIRMED, false)
                        nav.navigateUp()
                    },
                )
            }

            bottomSheet<TagPicker> {
                val nav = LocalTraverseNavigator.current
                TagPickerSheet(
                    onTagSelected = { selectedTag ->
                        nav.setResult(RESULT_SELECTED_TAG, selectedTag)
                        nav.navigateUp()
                    },
                    onDismiss = { nav.navigateUp() },
                )
            }
        }
    }
}

