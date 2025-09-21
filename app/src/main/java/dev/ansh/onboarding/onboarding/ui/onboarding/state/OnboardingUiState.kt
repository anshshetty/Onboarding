package dev.ansh.onboarding.onboarding.ui.onboarding.state

import androidx.compose.ui.graphics.Color
import dev.ansh.onboarding.onboarding.data.model.SaveButtonCta
import dev.ansh.onboarding.onboarding.ui.onboarding.model.OnboardingCardUiModel

data class OnboardingUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val toolbarTitle: String = "Onboarding",
    val introTitle: String = "",
    val introSubtitle: String = "",
    val introSubtitleIcon: String? = null,
    val toolBarIcon: String? = null,
    val cards: List<OnboardingCardUiModel> = emptyList(),
    val revealedCardCount: Int = 0,
    val collapsedCardIndices: Set<Int> = emptySet(),
    val expandedCardIndex: Int? = null,
    val backgroundStart: Color = Color(0xFF1B1727),
    val backgroundEnd: Color = Color(0xFF1B1727),
    val showCta: Boolean = false,
    val cta: SaveButtonCta? = null,
    val ctaLottie: String? = null,
    val timing: Timing = Timing(),
    val error: String? = null,
    val autoplayCompleted: Boolean = false
)

data class Timing(
    val intro: Long = 400L,
    val enter: Long = 900L,
    val hold: Long = 2000L,
    val collapse: Long = 600L,
    val overlap: Long = 220L
)
