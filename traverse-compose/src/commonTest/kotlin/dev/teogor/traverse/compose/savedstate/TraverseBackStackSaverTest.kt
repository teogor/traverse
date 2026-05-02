package dev.teogor.traverse.compose.savedstate

import androidx.compose.runtime.mutableStateListOf
import dev.teogor.traverse.compose.graph.TraverseGraphBuilder
import dev.teogor.traverse.core.Destination
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

// ── Fixtures ──────────────────────────────────────────────────────────────────

@Serializable
private data object SavedHome : Destination

@Serializable
private data class SavedProfile(val userId: String) : Destination

@Serializable
private data class SavedSettings(val tab: Int = 0) : Destination

// Non-serializable destination — should be skipped gracefully.
private data class NonSerializable(val id: String) : Destination

class TraverseBackStackSaverTest {

    private fun graphBuilder(vararg block: TraverseGraphBuilder.() -> Unit): TraverseGraphBuilder =
        TraverseGraphBuilder().also { builder -> block.forEach { builder.it() } }

    @Test
    fun saverIsNullWhenNoSerializersRegistered() {
        val builder = TraverseGraphBuilder().also { b ->
            // Non-@Serializable — serializer() will throw, so spec.serializer == null
            b.screen<NonSerializable> { }
        }
        val saver = buildBackStackSaver(builder)
        assertNull(saver, "Expected null saver when no serializable types are registered")
    }

    @Test
    fun saverIsNotNullWhenSerializableTypesRegistered() {
        val builder = TraverseGraphBuilder().also { b ->
            b.screen<SavedHome> { }
            b.screen<SavedProfile> { }
        }
        val saver = buildBackStackSaver(builder)
        assertNotNull(saver, "Expected non-null saver when @Serializable types are registered")
    }

    @Test
    fun saveAndRestoreRoundTrip() {
        val builder = TraverseGraphBuilder().also { b ->
            b.screen<SavedHome> { }
            b.screen<SavedProfile> { }
            b.screen<SavedSettings> { }
        }
        val saver = buildBackStackSaver(builder)
        assertNotNull(saver, "Saver must not be null")

        val original = mutableStateListOf<Destination>(
            SavedHome,
            SavedProfile("42"),
            SavedSettings(tab = 2),
        )

        // Simulate save
        val encoded: String? = with(saver) {
            // SaverScope.save needs a scope; use a fake one
            object : androidx.compose.runtime.saveable.SaverScope {
                override fun canBeSaved(value: Any): Boolean = true
            }.save(original)
        }
        assertNotNull(encoded, "Expected save() to return non-null encoded string")

        // Simulate restore
        val restored = saver.restore(encoded)
        assertNotNull(restored, "Expected restore() to return non-null list")

        assertEquals(3, restored.size, "Restored list should have 3 entries")
        assertEquals<Destination>(SavedHome, restored[0])
        assertEquals<Destination>(SavedProfile("42"), restored[1])
        assertEquals<Destination>(SavedSettings(tab = 2), restored[2])
    }

    @Test
    fun restoreReturnsNullForCorruptJson() {
        val builder = TraverseGraphBuilder().also { b ->
            b.screen<SavedHome> { }
        }
        val saver = buildBackStackSaver(builder)
        assertNotNull(saver)

        val restored = saver.restore("{{not valid json}}")
        assertNull(restored, "Expected null when JSON is corrupt (graceful degradation)")
    }

    @Test
    fun saveSkipsNonSerializableEntriesGracefully() {
        val builder = TraverseGraphBuilder().also { b ->
            b.screen<SavedHome> { }
            // NonSerializable won't have a serializer — should be silently skipped
            b.screen<NonSerializable> { }
        }
        val saver = buildBackStackSaver(builder)
        assertNotNull(saver, "Saver should still be non-null if at least one type is serializable")

        // A back stack containing only SavedHome (which is serializable) should round-trip fine
        val list = mutableStateListOf<Destination>(SavedHome)
        val encoded = with(saver) {
            object : androidx.compose.runtime.saveable.SaverScope {
                override fun canBeSaved(value: Any): Boolean = true
            }.save(list)
        }
        assertNotNull(encoded)
        val restored = saver.restore(encoded)
        assertNotNull(restored)
        assertEquals<List<Destination>>(listOf(SavedHome), restored.toList())
    }
}

