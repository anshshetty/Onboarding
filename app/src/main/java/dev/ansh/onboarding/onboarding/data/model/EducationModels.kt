package dev.ansh.onboarding.onboarding.data.model

import kotlinx.serialization.Serializable

/**
 * Root response model for education metadata API
 */
@Serializable
data class EducationResponse(
    val success: Boolean,
    val data: EducationData
)

/**
 * Education data wrapper
 */
@Serializable
data class EducationData(
    val manualBuyEducationData: ManualBuyEducationData
)

/**
 * Main education content with timing configuration
 */
@Serializable
data class ManualBuyEducationData(
    val toolBarText: String,
    val introTitle: String,
    val introSubtitle: String,
    val educationCardList: List<EducationCard>,
    val saveButtonCta: SaveButtonCta,
    val ctaLottie: String? = null,
    val screenType: String? = null,
    val collapseCardTiltInterval: Long = 1000,
    val collapseExpandIntroInterval: Long = 500,
    val bottomToCenterTranslationInterval: Long = 1500,
    val expandCardStayInterval: Long = 3000,
    val introSubtitleIcon: String? = null,
    val toolBarIcon: String? = null,
    val cohort: String? = null,
    val combination: String? = null,
    val seenCount: String? = null,
    val actionText: String? = null,
    val shouldShowOnLandingPage: Boolean? = null,
    val shouldShowBeforeNavigating: Boolean? = null
)

/**
 * Individual education card with visual styling
 */
@Serializable
data class EducationCard(
    val image: String,
    val collapsedStateText: String,
    val expandStateText: String,
    val backGroundColor: String,
    val strokeStartColor: String,
    val strokeEndColor: String,
    val startGradient: String,
    val endGradient: String
)

/**
 * Call-to-action button configuration
 */
@Serializable
data class SaveButtonCta(
    val text: String,
    val deeplink: String? = null,
    val backgroundColor: String,
    val textColor: String,
    val strokeColor: String,
    val icon: String? = null,
    val order: String? = null
)
