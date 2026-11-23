// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.avenox

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AvenoxPromptEngineTest {
    @Test
    fun prefixPreservesUserTextAndIsIdempotent() {
        val prefixed = "SYSTEM\n\nhello world"
        assertEquals(
            prefixed,
            AvenoxPromptEngine.apply("hello world", "SYSTEM", AvenoxPromptEngine.POSITION_PREFIX),
        )
        assertEquals(
            prefixed,
            AvenoxPromptEngine.apply(prefixed, "SYSTEM", AvenoxPromptEngine.POSITION_PREFIX),
        )
    }

    @Test
    fun legacySuffixAndWrapperPositionsAreDisabled() {
        assertEquals(AvenoxPromptEngine.POSITION_OFF, AvenoxPromptEngine.normalizePosition("suffix"))
        assertEquals(AvenoxPromptEngine.POSITION_OFF, AvenoxPromptEngine.normalizePosition("wrapper"))
        assertEquals("hello", AvenoxPromptEngine.apply("hello", "SYSTEM", "suffix"))
        assertEquals("hello", AvenoxPromptEngine.apply("hello", "SYSTEM", "wrapper"))
    }

    @Test
    fun livePrefixRendersBeforeTypingAndDropsInputSlot() {
        assertEquals(
            "SYSTEM demo\n\n",
            AvenoxPromptEngine.renderLivePrefix(
                "SYSTEM {app} {input}",
                AvenoxPromptEngine.TemplateVariables(appPackage = "demo", input = "ignored"),
            )
        )
    }

    @Test
    fun templateVariablesRenderForLocalNodes() {
        val vars = AvenoxPromptEngine.TemplateVariables(
            input = "draft",
            selectedText = "selected",
            clipboard = "clip",
            appPackage = "pkg",
            date = "2026-11-23",
        )
        assertEquals(
            "draft|selected|clip|pkg|2026-11-23",
            AvenoxPromptEngine.renderTemplate("{input}|{selected_text}|{clipboard}|{app}|{date}", vars),
        )
    }

    @Test
    fun modeScopeAndDefaultsWork() {
        assertEquals(AvenoxPromptEngine.MODE_BEYIN, AvenoxPromptEngine.defaultModeForPackage("com.openai.chatgpt"))
        assertEquals(AvenoxPromptEngine.MODE_GITHUB, AvenoxPromptEngine.defaultModeForPackage("com.github.android"))
        assertEquals(AvenoxPromptEngine.MODE_NORMAL, AvenoxPromptEngine.defaultModeForPackage("com.example.app"))
        assertTrue(AvenoxPromptEngine.isNodeVisibleInMode("", AvenoxPromptEngine.MODE_CODING))
        assertTrue(AvenoxPromptEngine.isNodeVisibleInMode("coding,github", AvenoxPromptEngine.MODE_GITHUB))
        assertFalse(AvenoxPromptEngine.isNodeVisibleInMode("coding,github", AvenoxPromptEngine.MODE_BEYIN))
        assertEquals(AvenoxPromptEngine.MODE_BEYIN, AvenoxPromptEngine.nextMode(AvenoxPromptEngine.MODE_NORMAL))
    }

    @Test
    fun appScopesSupportExactAndPrefixWildcard() {
        assertTrue(AvenoxPromptEngine.isNodeVisibleInApp("", "com.example.app"))
        assertTrue(AvenoxPromptEngine.isNodeVisibleInApp("com.openai.chatgpt", "com.openai.chatgpt"))
        assertTrue(AvenoxPromptEngine.isNodeVisibleInApp("com.google.*", "com.google.android.apps.gemini"))
        assertFalse(AvenoxPromptEngine.isNodeVisibleInApp("com.openai.chatgpt", "com.github.android"))
    }

    @Test
    fun localCommandsAreDetectedAndRemoved() {
        assertEquals(AvenoxPromptEngine.COMMAND_PREFIX, AvenoxPromptEngine.detectLocalCommand("Context #prefix"))
        assertEquals(AvenoxPromptEngine.COMMAND_RAW_SEND, AvenoxPromptEngine.detectLocalCommand("#rawsend"))
        assertEquals(AvenoxPromptEngine.COMMAND_LOCAL_SEND, AvenoxPromptEngine.detectLocalCommand("Devam et #localsend"))
        assertEquals("Context", AvenoxPromptEngine.removeBehaviorTokens("Context #prefix"))
        assertEquals("Devam et", AvenoxPromptEngine.removeBehaviorTokens("Devam et #localsend #outputonly"))
    }

    @Test
    fun offOrBlankPromptLeavesTextUnchanged() {
        assertEquals("hello", AvenoxPromptEngine.apply("hello", "SYSTEM", AvenoxPromptEngine.POSITION_OFF))
        assertEquals("hello", AvenoxPromptEngine.apply("hello", " ", AvenoxPromptEngine.POSITION_PREFIX))
        assertFalse(AvenoxPromptEngine.isEnabled("SYSTEM", "invalid"))
        assertTrue(AvenoxPromptEngine.isEnabled("SYSTEM", AvenoxPromptEngine.POSITION_PREFIX))
    }
}
