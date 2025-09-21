package dev.ansh.onboarding.onboarding.ui.onboarding.model

import androidx.compose.ui.graphics.Color

/**
 * UI friendly representation of an onboarding education card with pre-computed colors.
 */
data class OnboardingCardUiModel(
    val id: Int,
    val imageUrl: String,
    val collapsedTitle: String,
    val expandedTitle: String,
    val backgroundColor: Color,
    val strokeStartColor: Color,
    val strokeEndColor: Color,
    val gradientStart: Color,
    val gradientEnd: Color
)