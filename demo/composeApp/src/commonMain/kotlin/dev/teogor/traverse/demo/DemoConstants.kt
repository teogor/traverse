package dev.teogor.traverse.demo

/** Result key: selected color string from [ColorPicker] → [ResultDemo]. */
const val RESULT_COLOR = "result_color"

/** Result key: selected option string from [OptionSheet] → [SheetDemo]. */
const val RESULT_OPTION = "result_option"

/** Result key: Boolean confirmation from [ShowcaseDialog] → [DialogDemo]. */
const val RESULT_DIALOG_CONFIRMED = "result_dialog_confirmed"

/** Map of feature ID → (display title, description) used in [FeatureDetail]. */
val FEATURE_INFO: Map<String, Pair<String, String>> = mapOf(
    "kmp" to ("Kotlin Multiplatform" to "Traverse runs on Android, iOS, Desktop, and Web from a single codebase."),
    "back-stack" to ("Back Stack" to "A SnapshotStateList<Destination> that Traverse manages. Every push/pop triggers reactive recomposition."),
    "serialization" to ("Serialization" to "@Serializable destinations let Traverse safely restore the back stack across process death (planned feature)."),
    "compose" to ("Compose Multiplatform" to "Traverse's UI layer is built on AnimatedContent and Compose overlays — no external nav library runtime."),
)

