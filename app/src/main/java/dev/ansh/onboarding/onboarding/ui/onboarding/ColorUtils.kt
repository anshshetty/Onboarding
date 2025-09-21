package dev.ansh.onboarding.onboarding.ui.onboarding

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import java.util.Locale
import androidx.core.graphics.toColorInt

internal fun String?.asColorOr(default: Color): Color {
    if (this.isNullOrBlank()) return default
    return try {
        val normalized = if (startsWith("#")) {
            this
        } else {
            val lower = lowercase(Locale.ROOT).removePrefix("0x")
            "#${lower}"
        }
        Color(normalized.toColorInt())
    } catch (_: IllegalArgumentException) {
        default
    }
}
