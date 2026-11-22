// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.avenox

object AvenoxPromptEngine {
    const val POSITION_OFF = "off"
    const val POSITION_PREFIX = "prefix"
    const val POSITION_SUFFIX = "suffix"

    const val COMMAND_INSERT = "#insert"
    const val COMMAND_LOCAL_SEND = "#localsend"
    const val COMMAND_MODE_PREFIX = "#modeprefix"
    const val COMMAND_MODE_SUFFIX = "#modesuffix"
    const val COMMAND_MODE_OFF = "#modeoff"

    private val positions = setOf(POSITION_OFF, POSITION_PREFIX, POSITION_SUFFIX)
    private val localCommands = listOf(
        COMMAND_LOCAL_SEND,
        COMMAND_INSERT,
        COMMAND_MODE_PREFIX,
        COMMAND_MODE_SUFFIX,
        COMMAND_MODE_OFF,
    )
    private val allBehaviorTokens = setOf(
        "#editor", "#outputonly", "#proofread", "#paraphrase", "#summarize",
        "#expand", "#toneshift", "#generate", "#append", "#showthought",
    ) + localCommands

    fun normalizePosition(position: String?): String =
        position?.lowercase()?.takeIf { it in positions } ?: POSITION_OFF

    fun isEnabled(prompt: String?, position: String?): Boolean =
        !prompt.isNullOrBlank() && normalizePosition(position) != POSITION_OFF

    fun apply(userText: String, prompt: String, position: String): String {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isEmpty()) return userText
        return when (normalizePosition(position)) {
            POSITION_PREFIX -> when {
                userText == cleanPrompt || userText.startsWith("$cleanPrompt\n\n") -> userText
                userText.isBlank() -> cleanPrompt
                else -> "$cleanPrompt\n\n$userText"
            }
            POSITION_SUFFIX -> when {
                userText == cleanPrompt || userText.endsWith("\n\n$cleanPrompt") -> userText
                userText.isBlank() -> cleanPrompt
                else -> "$userText\n\n$cleanPrompt"
            }
            else -> userText
        }
    }

    fun detectLocalCommand(prompt: String): String? {
        val tokens = prompt.split(Regex("\\s+")).toSet()
        return localCommands.firstOrNull { it in tokens }
    }

    fun removeBehaviorTokens(prompt: String): String =
        prompt.split(Regex("\\s+"))
            .filterNot { it in allBehaviorTokens }
            .joinToString(" ")
            .trim()
}
