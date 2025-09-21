package dev.ansh.onboarding.onboarding.ui.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ansh.onboarding.onboarding.data.model.ManualBuyEducationData
import dev.ansh.onboarding.onboarding.domain.EducationRepository
import dev.ansh.onboarding.onboarding.ui.onboarding.model.OnboardingCardUiModel
import dev.ansh.onboarding.onboarding.ui.onboarding.state.OnboardingUiState
import dev.ansh.onboarding.onboarding.ui.onboarding.state.Timing
import dev.ansh.onboarding.onboarding.ui.onboarding.util.DefaultPalette
import dev.ansh.onboarding.onboarding.ui.onboarding.util.asColorOr
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToLong

/**
 * Coordinates onboarding data loading and animation timeline orchestration.
 */
class OnboardingViewModel(
    private val educationRepository: EducationRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private var autoplayJob: Job? = null

    init {
        refresh()
    }

    /**
     * Public entry point to re-fetch onboarding education data.
     */
    fun refresh() {
        autoplayJob?.cancel()
        viewModelScope.launch {
            _uiState.update { state ->
                val hasContent = state.cards.isNotEmpty()
                state.copy(
                    isLoading = !hasContent,
                    isRefreshing = hasContent,
                    error = null
                )
            }

            val result = runCatching {
                withContext(ioDispatcher) { educationRepository.loadEducation() }
            }

            result
                .onSuccess { data -> handleEducationData(data) }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = throwable.localizedMessage ?: "Something went wrong"
                        )
                    }
                }
        }
    }

    /**
     * Collapses all cards except the selected one and keeps it expanded.
     */
    fun onCardSelected(index: Int) {
        val cards = _uiState.value.cards
        if (index !in cards.indices) return

        autoplayJob?.cancel()
        _uiState.update { state ->
            state.copy(
                backgroundStart = cards[index].gradientStart,
                backgroundEnd = cards[index].gradientEnd,
                collapsedCardIndices = cards.indices.filter { it != index }.toSet(),
                expandedCardIndex = index,
                revealedCardCount = cards.size,
                autoplayCompleted = true,
                isLoading = false,
                isRefreshing = false
            )
        }
    }

    /**
     * Called when autoplay animation completes on the compose side.
     */
    fun onAutoPlayFinished() {
        _uiState.update { it.copy(autoplayCompleted = true) }
    }

    private fun handleEducationData(data: ManualBuyEducationData) {
        val cards = data.educationCardList.mapIndexed { index, card ->
            OnboardingCardUiModel(
                id = index,
                imageUrl = card.image,
                collapsedTitle = card.collapsedStateText.trim(),
                expandedTitle = card.expandStateText.trim(),
                backgroundColor = card.backGroundColor.asColorOr(DefaultPalette.CardBackground),
                strokeStartColor = card.strokeStartColor.asColorOr(DefaultPalette.StrokeStart),
                strokeEndColor = card.strokeEndColor.asColorOr(DefaultPalette.StrokeEnd),
                gradientStart = card.startGradient.asColorOr(DefaultPalette.GradientStart),
                gradientEnd = card.endGradient.asColorOr(DefaultPalette.GradientEnd)
            )
        }

        val timing = data.toTiming()
        val backgroundStart = cards.firstOrNull()?.gradientStart ?: DefaultPalette.ScreenGradientStart
        val backgroundEnd = cards.firstOrNull()?.gradientEnd ?: DefaultPalette.ScreenGradientEnd

        _uiState.update { state ->
            state.copy(
                isLoading = false,
                isRefreshing = false,
                toolbarTitle = data.toolBarText.ifBlank { state.toolbarTitle },
                introTitle = data.introTitle,
                introSubtitle = data.introSubtitle,
                introSubtitleIcon = data.introSubtitleIcon,
                toolBarIcon = data.toolBarIcon,
                cards = cards,
                revealedCardCount = 0,
                collapsedCardIndices = emptySet(),
                expandedCardIndex = null,
                backgroundStart = backgroundStart,
                backgroundEnd = backgroundEnd,
                showCta = data.saveButtonCta.text.isNotBlank(),
                cta = data.saveButtonCta,
                ctaLottie = data.ctaLottie,
                timing = timing,
                error = null,
                autoplayCompleted = false
            )
        }

        startAutoplay(cards.size, timing)
    }

    private fun startAutoplay(cardCount: Int, timing: Timing) {
        autoplayJob?.cancel()
        if (cardCount == 0) {
            _uiState.update { it.copy(autoplayCompleted = true) }
            return
        }

        autoplayJob = viewModelScope.launch {
            if (timing.intro > 0) {
                delay(timing.intro)
            }

            repeat(cardCount) { index ->
                _uiState.update { state ->
                    state.copy(
                        backgroundStart = state.cards[index].gradientStart,
                        backgroundEnd = state.cards[index].gradientEnd,
                        revealedCardCount = max(state.revealedCardCount, index + 1),
                        collapsedCardIndices = state.collapsedCardIndices - index,
                        expandedCardIndex = index
                    )
                }

                val holdDuration = timing.enter + timing.hold
                if (holdDuration > 0) {
                    delay(holdDuration)
                }

                val isLast = index == cardCount - 1
                if (isLast) {
                    _uiState.update { state ->
                        state.copy(
                            collapsedCardIndices = state.collapsedCardIndices - index,
                            expandedCardIndex = index,
                            autoplayCompleted = true
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            collapsedCardIndices = state.collapsedCardIndices + index,
                            expandedCardIndex = index
                        )
                    }

                    val overlapDelay = timing.collapse - timing.overlap
                    if (timing.collapse > 0) {
                        val wait = if (overlapDelay > 0) overlapDelay else 0L
                        if (wait > 0) {
                            delay(wait)
                        }
                    }
                }
            }
        }
    }

    private fun ManualBuyEducationData.toTiming(): Timing {
        val collapse = collapseCardTiltInterval.coerceAtLeast(0L)
        val computedOverlap = if (collapse <= 0) 0L else max(120L, (collapse * 0.35f).roundToLong())
        return Timing(
            intro = collapseExpandIntroInterval.coerceAtLeast(0L),
            enter = bottomToCenterTranslationInterval.coerceAtLeast(0L),
            hold = expandCardStayInterval.coerceAtLeast(0L),
            collapse = collapse,
            overlap = computedOverlap.coerceAtMost(collapse)
        )
    }
}