// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.avenox

object AvenoxPromptEngine {
    const val POSITION_OFF = "off"
    const val POSITION_PREFIX = "prefix"

    const val MODE_NORMAL = "normal"
    const val MODE_BEYIN = "beyin"
    const val MODE_CODING = "coding"
    const val MODE_RESEARCH = "research"
    const val MODE_IMAGE = "image"
    const val MODE_GITHUB = "github"
    const val MODE_CUSTOM = "custom"

    val MODES = listOf(
        MODE_NORMAL,
        MODE_BEYIN,
        MODE_CODING,
        MODE_RESEARCH,
        MODE_IMAGE,
        MODE_GITHUB,
        MODE_CUSTOM,
    )

    const val COMMAND_INSERT = "#insert"
    const val COMMAND_PREFIX = "#prefix"
    const val COMMAND_REPLACE = "#replace"
    const val COMMAND_LOCAL_SEND = "#localsend"
    const val COMMAND_RAW_SEND = "#rawsend"
    const val COMMAND_MODE_PREFIX = "#modeprefix"
    const val COMMAND_MODE_OFF = "#modeoff"

    data class TemplateVariables(
        val input: String = "",
        val selectedText: String = "",
        val clipboard: String = "",
        val appPackage: String = "",
        val date: String = "",
    )

    private val positions = setOf(POSITION_OFF, POSITION_PREFIX)
    private val localCommands = listOf(
        COMMAND_LOCAL_SEND,
        COMMAND_RAW_SEND,
        COMMAND_INSERT,
        COMMAND_PREFIX,
        COMMAND_REPLACE,
        COMMAND_MODE_PREFIX,
        COMMAND_MODE_OFF,
    )
    private val allBehaviorTokens = setOf(
        "#editor", "#outputonly", "#proofread", "#paraphrase", "#summarize",
        "#expand", "#toneshift", "#generate", "#append", "#showthought",
    ) + localCommands

    fun normalizePosition(position: String?): String =
        position?.lowercase()?.takeIf { it in positions } ?: POSITION_OFF

    fun normalizeMode(mode: String?): String =
        mode?.lowercase()?.takeIf { it in MODES } ?: MODE_NORMAL

    fun nextMode(mode: String?): String {
        val current = normalizeMode(mode)
        val index = MODES.indexOf(current)
        return MODES[(index + 1) % MODES.size]
    }

    fun modeLabel(mode: String?): String = when (normalizeMode(mode)) {
        MODE_BEYIN -> "BEYİN"
        MODE_CODING -> "KOD"
        MODE_RESEARCH -> "ARAŞ"
        MODE_IMAGE -> "GÖRSEL"
        MODE_GITHUB -> "GITHUB"
        MODE_CUSTOM -> "ÖZEL"
        else -> "NORMAL"
    }

    fun defaultModeForPackage(packageName: String?): String {
        val pkg = packageName?.lowercase().orEmpty()
        return when {
            pkg == "com.openai.chatgpt" -> MODE_BEYIN
            pkg == "com.anthropic.claude" -> MODE_BEYIN
            pkg == "com.google.android.apps.bard" -> MODE_BEYIN
            pkg == "com.google.android.apps.gemini" -> MODE_BEYIN
            pkg == "ai.perplexity.app.android" -> MODE_RESEARCH
            pkg == "com.github.android" -> MODE_GITHUB
            else -> MODE_NORMAL
        }
    }

    fun parseModeScope(raw: String?): Set<String> =
        raw.orEmpty()
            .split(',')
            .map { it.trim().lowercase() }
            .filter { it in MODES }
            .toSet()

    fun isNodeVisibleInMode(rawScope: String?, currentMode: String?): Boolean {
        val scope = rawScope.orEmpty().trim()
        if (scope.isEmpty()) return true
        return normalizeMode(currentMode) in parseModeScope(scope)
    }

    fun parseAppScope(raw: String?): List<String> =
        raw.orEmpty()
            .split(',', ';', '\n')
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }

    fun isNodeVisibleInApp(rawScope: String?, packageName: String?): Boolean {
        val scope = parseAppScope(rawScope)
        if (scope.isEmpty()) return true
        val pkg = packageName?.trim()?.lowercase().orEmpty()
        if (pkg.isEmpty()) return false
        return scope.any { pattern ->
            pattern == "*" ||
                pattern == pkg ||
                (pattern.endsWith("*") && pkg.startsWith(pattern.dropLast(1)))
        }
    }

    fun isEnabled(prompt: String?, position: String?): Boolean =
        !prompt.isNullOrBlank() && normalizePosition(position) != POSITION_OFF

    fun renderTemplate(template: String, variables: TemplateVariables): String =
        template
            .replace("{input}", variables.input)
            .replace("{selected_text}", variables.selectedText)
            .replace("{clipboard}", variables.clipboard)
            .replace("{app}", variables.appPackage)
            .replace("{date}", variables.date)

    fun renderLivePrefix(
        prompt: String,
        variables: TemplateVariables = TemplateVariables(),
    ): String {
        val rendered = renderTemplate(prompt, variables.copy(input = "", selectedText = "")).trim()
        return if (rendered.isEmpty()) "" else "$rendered\n\n"
    }

    fun apply(
        userText: String,
        prompt: String,
        position: String,
        variables: TemplateVariables = TemplateVariables(),
    ): String {
        if (prompt.isBlank() || normalizePosition(position) != POSITION_PREFIX) return userText
        val cleanPrompt = renderTemplate(prompt, variables.copy(input = "")).trim()
        if (cleanPrompt.isEmpty()) return userText
        return when {
            userText == cleanPrompt || userText.startsWith("$cleanPrompt\n\n") -> userText
            userText.isBlank() -> cleanPrompt
            else -> "$cleanPrompt\n\n$userText"
        }
    }

    fun detectLocalCommand(prompt: String): String? {
        val tokens = prompt.split(Regex("\\s+")).toSet()
        return localCommands.firstOrNull { it in tokens }
    }

    fun toolbarLabel(prompt: String, explicitTag: String? = null): String {
        val explicit = explicitTag?.trim().orEmpty()
        if (explicit.isNotEmpty()) return explicit.take(6)

        return when (detectLocalCommand(prompt)) {
            COMMAND_MODE_PREFIX -> "BAŞA"
            COMMAND_MODE_OFF -> "KAPAT"
            COMMAND_INSERT -> removeBehaviorTokens(prompt).firstWordOr("EKLE")
            COMMAND_PREFIX -> removeBehaviorTokens(prompt).firstWordOr("BAŞA")
            COMMAND_REPLACE -> removeBehaviorTokens(prompt).firstWordOr("DEĞİŞ")
            COMMAND_LOCAL_SEND -> removeBehaviorTokens(prompt).firstWordOr("GÖNDER")
            COMMAND_RAW_SEND -> "HAM"
            else -> ""
        }
    }

    private fun String.firstWordOr(fallback: String): String =
        trim().split(Regex("\\s+")).firstOrNull { it.isNotBlank() }?.take(6) ?: fallback.take(6)

    fun removeBehaviorTokens(prompt: String): String =
        prompt.split(Regex("\\s+"))
            .filterNot { it in allBehaviorTokens }
            .joinToString(" ")
            .trim()
}
