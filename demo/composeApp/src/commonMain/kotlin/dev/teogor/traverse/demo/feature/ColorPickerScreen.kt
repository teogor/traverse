package dev.teogor.traverse.demo.feature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.teogor.traverse.demo.ui.CodeSnippet
import dev.teogor.traverse.demo.ui.ShowcaseScaffold

private val COLOR_OPTIONS = listOf(
    "Red" to Color(0xFFEF5350),
    "Orange" to Color(0xFFFF7043),
    "Amber" to Color(0xFFFFCA28),
    "Green" to Color(0xFF66BB6A),
    "Blue" to Color(0xFF42A5F5),
    "Purple" to Color(0xFFAB47BC),
)

/**
 * Color picker — the "producer" side of the navigation-results demo.
 *
 * Tapping a color calls `nav.setResult(RESULT_COLOR, colorName)` + `nav.navigateUp()`.
 */
@Composable
fun ColorPickerScreen(
    onColorPicked: (String) -> Unit,
) {
    ShowcaseScaffold(
        title = "Color Picker",
        apiBadge = "setResult",
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Pick a color to return as a result:",
                style = MaterialTheme.typography.bodyLarge,
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(COLOR_OPTIONS) { (name, color) ->
                    Button(
                        onClick = { onColorPicked(name) },
                        colors = ButtonDefaults.buttonColors(containerColor = color),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    ) {
                        Text(name, color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            CodeSnippet(
                code = """// This screen (producer):
nav.setResult(RESULT_COLOR, colorName)
nav.navigateUp()
// ↳ ResultDemoScreen collects the result""",
            )
        }
    }
}

