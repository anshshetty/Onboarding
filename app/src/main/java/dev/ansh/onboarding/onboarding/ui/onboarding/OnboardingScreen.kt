package dev.ansh.onboarding.onboarding.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import dev.ansh.onboarding.onboarding.data.model.SaveButtonCta
import dev.ansh.onboarding.onboarding.ui.theme.JarOnboardingTheme

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLanding: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gradient = remember(uiState.backgroundStart, uiState.backgroundEnd) {
        Brush.verticalGradient(listOf(uiState.backgroundStart, uiState.backgroundEnd))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        when {
            uiState.isLoading && uiState.cards.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.error != null && uiState.cards.isEmpty() -> {
                ErrorContent(
                    message = uiState.error, onRetry = { viewModel.refresh() })
            }

            else -> {
                SharedTransitionLayout(
                    modifier = Modifier.fillMaxSize()
                ) {
                    OnboardingBody(
                        state = uiState,
                        onBackClick = onNavigateBack,
                        onCardClick = viewModel::onCardSelected,
                        onCtaClick = {
                            viewModel.onAutoPlayFinished()
                            onNavigateToLanding()
                        })
                }
            }
        }

        if (uiState.isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )
        }

        if (uiState.error != null && uiState.cards.isNotEmpty()) {
            InlineErrorBanner(
                message = uiState.error,
                onRetry = { viewModel.refresh() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SharedTransitionScope.OnboardingBody(
    state: OnboardingUiState,
    onBackClick: () -> Unit,
    onCardClick: (Int) -> Unit,
    onCtaClick: () -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent, topBar = {
            OnboardingTopBar(
                title = state.toolbarTitle, iconUrl = state.toolBarIcon, onBackClick = onBackClick
            )
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            IntroHeader(state)

            Spacer(modifier = Modifier.height(16.dp))

            CardCarousel(
                state = state,
                onCardClick = onCardClick,
                modifier = Modifier
                    .weight(1f, fill = true)
                    .padding(bottom = 16.dp)
            )

            if (state.showCta && state.cta != null) {
                CtaButton(
                    cta = state.cta,
                    ctaLottie = state.ctaLottie,
                    enabled = state.autoplayCompleted,
                    onClick = onCtaClick,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .navigationBarsPadding()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingTopBar(
    title: String,
    iconUrl: String? = null,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Show toolbar icon if available
                iconUrl?.let { iconUrl ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(iconUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }, navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate back",
                    tint = Color.White
                )
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun IntroHeader(state: OnboardingUiState) {
    // Intro section with icon
    AnimatedVisibility(
        visible = state.revealedCardCount <= 0,
        enter = fadeIn(tween(100)),
        exit = fadeOut(tween(100))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.introTitle.isNotBlank()) {
                Text(
                    text = state.introTitle, style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White, fontWeight = FontWeight.SemiBold
                    ), textAlign = TextAlign.Center
                )
            }

            if (state.introSubtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.introSubtitle, style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White.copy(alpha = 0.85f), lineHeight = 20.sp
                    ), textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.CardCarousel(
    state: OnboardingUiState, onCardClick: (Int) -> Unit, modifier: Modifier = Modifier
) {
    val cards = remember(state.cards, state.revealedCardCount) {
        state.cards.take(state.revealedCardCount.coerceIn(0, state.cards.size))
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(
            items = cards, key = { it.id }) { card ->
            val stage = when {
                card.id >= state.revealedCardCount -> CardStage.Hidden
                state.collapsedCardIndices.contains(card.id) -> CardStage.Collapsed
                else -> CardStage.Expanded
            }

            CardItem(
                card = card,
                stage = stage,
                index = card.id,
                timing = state.timing,
                onClick = { onCardClick(card.id) })
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.CardItem(
    card: OnboardingCardUiModel, stage: CardStage, index: Int, timing: Timing, onClick: () -> Unit
) {
    val visible = stage != CardStage.Hidden
    val enterDuration = timing.enter.toAnimationDuration()

    AnimatedVisibility(
        visible = visible, enter = slideInVertically(
            animationSpec = tween(durationMillis = enterDuration, easing = EaseOutCubic)
        ) { fullHeight -> fullHeight / 2 } + fadeIn(
            animationSpec = tween(durationMillis = enterDuration / 2)
        ), modifier = Modifier
            .fillMaxWidth()
            .zIndexFor(stage)) {
        CardSurface(
            card = card,
            stage = stage,
            timing = timing,
            onClick = onClick,
            index = index,
            animatedVisibilityScope = this
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.CardSurface(
    card: OnboardingCardUiModel,
    stage: CardStage,
    timing: Timing,
    onClick: () -> Unit,
    index: Int,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val transition = updateTransition(stage, label = "card-stage-$index")

    val corner by transition.animateDp(label = "corner-$index") { target ->
        when (target) {
            CardStage.Collapsed -> 38.dp
            CardStage.Expanded -> 32.dp
            CardStage.Hidden -> 32.dp
        }
    }

    val elevation by transition.animateDp(label = "shadow-$index") { target ->
        when (target) {
            CardStage.Expanded -> 18.dp
            else -> 0.dp
        }
    }

    val contentAlpha by transition.animateFloat(label = "content-alpha-$index") { target ->
        when (target) {
            CardStage.Collapsed -> 0.0f
            else -> 1f
        }
    }

    val collapseDuration = timing.collapse.toAnimationDuration()

    val paddingValues = when (stage) {
        CardStage.Collapsed -> PaddingValues(horizontal = 20.dp, vertical = 18.dp)
        else -> PaddingValues(all = 24.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .sharedBounds(
                rememberSharedContentState(key = "card-$index"),
                animatedVisibilityScope = animatedVisibilityScope
            )
            .shadow(elevation = elevation, shape = RoundedCornerShape(corner))
            .clip(RoundedCornerShape(corner))
            .background(if (stage == CardStage.Collapsed) Color.Black.copy(alpha = 0.3f) else card.gradientStart)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(card.strokeStartColor, card.strokeEndColor)),
                shape = RoundedCornerShape(corner)
            )
            .clickable(onClick = onClick)
            .padding(paddingValues)
            .animateContentSize(
                animationSpec = tween(durationMillis = collapseDuration)
            )
    ) {
        if (stage == CardStage.Collapsed) {
            PillContent(card)
        } else {
            ExpandedContent(
                card = card, contentAlpha = contentAlpha
            )
        }
    }
}

@Composable
private fun ExpandedContent(
    card: OnboardingCardUiModel, contentAlpha: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(alpha = contentAlpha.coerceIn(0f, 1f)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = card.imageUrl,
            contentDescription = card.expandedTitle,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black.copy(alpha = 0.05f)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = card.expandedTitle, style = MaterialTheme.typography.headlineSmall.copy(
                color = Color.White, fontWeight = FontWeight.Bold
            ), textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PillContent(card: OnboardingCardUiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(card.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Text
        Text(
            text = card.collapsedTitle,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        // Chevron icon
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun CtaButton(
    modifier: Modifier = Modifier,
    cta: SaveButtonCta,
    ctaLottie: String? = null,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = remember(cta.backgroundColor) {
        cta.backgroundColor.asColorOr(Color.White)
    }
    val textColor = remember(cta.textColor) {
        cta.textColor.asColorOr(Color.Black)
    }
    val strokeColor = remember(cta.strokeColor) {
        cta.strokeColor.asColorOr(Color.Transparent)
    }

    AnimatedVisibility(
        visible = enabled,
        enter = fadeIn(tween(500)) + scaleIn(tween(500)),
        exit = fadeOut(tween(300)) + scaleOut(tween(300)),
        modifier = modifier
    ) {
        cta.let { buttonCta ->

            Button(
                onClick = onClick,
                modifier = Modifier
                    .width(165.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = backgroundColor
                ),
                border = BorderStroke(1.dp, strokeColor),
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = buttonCta.text,
                        color = textColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    ctaLottie?.let { lottieUrl ->
                        Spacer(modifier = Modifier.width(8.dp))
                        val composition by rememberLottieComposition(
                            LottieCompositionSpec.Url(lottieUrl)
                        )
                        LottieAnimation(
                            composition = composition,
                            iterations = Int.MAX_VALUE,
                            modifier = Modifier
                                .fillMaxHeight()
                                .rotate(180f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String?, onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message ?: "Something went wrong",
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun InlineErrorBanner(
    message: String?, onRetry: () -> Unit, modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onRetry)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = message ?: "Tap to retry",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Text(
            text = "Retry",
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

private enum class CardStage { Hidden, Expanded, Collapsed }

private fun CardStage.zIndexFor(): Float = when (this) {
    CardStage.Expanded -> 1f
    CardStage.Collapsed -> 0f
    CardStage.Hidden -> -1f
}

private fun Modifier.zIndexFor(stage: CardStage) = this.zIndex(stage.zIndexFor())

private fun Long.toAnimationDuration(): Int = coerceIn(60L, 3_000L).toInt()

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF6A1B9A)
@Composable
private fun OnboardingScreenPreview() {
    JarOnboardingTheme {
        val cards = listOf(
            OnboardingCardUiModel(
                id = 0,
                imageUrl = "",
                collapsedTitle = "Earn rewards",
                expandedTitle = "Earn rewards while you stack digital gold.",
                backgroundColor = Color(0xFF4C1D95),
                strokeStartColor = Color.White.copy(alpha = 0.3f),
                strokeEndColor = Color.White.copy(alpha = 0.05f),
                gradientStart = Color(0xFF6A1B9A),
                gradientEnd = Color(0xFFB5179E)
            ), OnboardingCardUiModel(
                id = 1,
                imageUrl = "",
                collapsedTitle = "Track progress",
                expandedTitle = "Track your milestones and grow savings effortlessly.",
                backgroundColor = Color(0xFF4C1D95),
                strokeStartColor = Color.White.copy(alpha = 0.3f),
                strokeEndColor = Color.White.copy(alpha = 0.05f),
                gradientStart = Color(0xFF7B4397),
                gradientEnd = Color(0xFFDC2430)
            )
        )

        val previewState = OnboardingUiState(
            isLoading = false,
            toolbarTitle = "Onboarding",
            introTitle = "Buy gold anytime, anywhere",
            introSubtitle = "Stay on top of your investments with simple reminders.",
            cards = cards,
            revealedCardCount = cards.size,
            collapsedCardIndices = setOf(0),
            expandedCardIndex = 1,
            backgroundStart = Color(0xFF4C1D95),
            backgroundEnd = Color(0xFFB5179E),
            showCta = true,
            cta = SaveButtonCta(
                text = "Continue",
                backgroundColor = "#FFFFFF",
                textColor = "#000000",
                strokeColor = "#33FFFFFF"
            ),
            autoplayCompleted = true
        )

        SharedTransitionLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            OnboardingBody(
                state = previewState,
                onBackClick = {},
                onCardClick = {},
                onCtaClick = {})
        }
    }
}
