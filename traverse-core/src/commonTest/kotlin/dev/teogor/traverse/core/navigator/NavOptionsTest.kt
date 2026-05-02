package dev.teogor.traverse.core.navigator

import dev.teogor.traverse.core.Destination
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@Serializable private data object SomeDest : Destination

class NavOptionsTest {

    @Test
    fun defaultValues() {
        val opts = NavOptions()
        assertFalse(opts.launchSingleTop)
        assertNull(opts.popUpTo)
        assertFalse(opts.popUpToInclusive)
        assertFalse(opts.restoreState)
    }

    @Test
    fun copyPreservesValues() {
        val opts = NavOptions(
            launchSingleTop = true,
            popUpTo = SomeDest,
            popUpToInclusive = true,
            restoreState = true,
        )
        val copy = opts.copy(launchSingleTop = false)
        assertFalse(copy.launchSingleTop)
        assertEquals(SomeDest, copy.popUpTo)
    }

    @Test
    fun builderLambdaAppliesCorrectly() {
        val opts = NavOptions().apply {
            launchSingleTop = true
            popUpTo = SomeDest
            popUpToInclusive = true
        }
        assertEquals(true, opts.launchSingleTop)
        assertEquals(SomeDest, opts.popUpTo)
        assertEquals(true, opts.popUpToInclusive)
        assertFalse(opts.restoreState)
    }
}

