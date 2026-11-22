// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.avenox

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AvenoxPromptEngineTest {
    @Test
    fun prefixAndSuffixPreserveUserText() {
        assertEquals("SYSTEM\n\nhello world", AvenoxPromptEngine.apply("hello world", "SYSTEM", AvenoxPromptEngine.POSITION_PREFIX))
        assertEquals("hello world\n\nSYSTEM", AvenoxPromptEngine.apply("hello world", "SYSTEM", AvenoxPromptEngine.POSITION_SUFFIX))
    }

    @Test
    fun injectionIsIdempotentAtConfiguredEdge() {
        val prefixed = "SYSTEM\n\nhello"
        val suffixed = "hello\n\nSYSTEM"
        assertEquals(prefixed, AvenoxPromptEngine.apply(prefixed, "SYSTEM", AvenoxPromptEngine.POSITION_PREFIX))
        assertEquals(suffixed, AvenoxPromptEngine.apply(suffixed, "SYSTEM", AvenoxPromptEngine.POSITION_SUFFIX))
    }

    @Test
    fun offOrBlankPromptLeavesTextUnchanged() {
        assertEquals("hello", AvenoxPromptEngine.apply("hello", "SYSTEM", AvenoxPromptEngine.POSITION_OFF))
        assertEquals("hello", AvenoxPromptEngine.apply("hello", " ", AvenoxPromptEngine.POSITION_PREFIX))
        assertFalse(AvenoxPromptEngine.isEnabled("SYSTEM", "invalid"))
    }

    @Test
    fun localCommandsAreDetectedAndRemoved() {
        assertEquals(AvenoxPromptEngine.COMMAND_LOCAL_SEND, AvenoxPromptEngine.detectLocalCommand("Devam et #localsend"))
        assertEquals("Devam et", AvenoxPromptEngine.removeBehaviorTokens("Devam et #localsend #outputonly"))
        assertTrue(AvenoxPromptEngine.isEnabled("SYSTEM", AvenoxPromptEngine.POSITION_PREFIX))
    }
}
