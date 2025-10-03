package com.pocketcode.features.onboarding.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pocketcode.core.ui.components.button.PocketButton
import com.pocketcode.core.ui.components.card.PocketCard
import com.pocketcode.core.ui.components.layout.PocketScaffold
import com.pocketcode.core.ui.components.layout.PocketScaffoldConfig
import com.pocketcode.core.ui.components.layout.ResponsiveLayout
import com.pocketcode.core.ui.components.navigation.PocketTopBar
import com.pocketcode.core.ui.components.navigation.StepperIndicator
import com.pocketcode.core.ui.components.navigation.TopBarAction
import com.pocketcode.core.ui.tokens.ComponentTokens
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.tokens.TypographyTokens
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit,
    onSkip: () -> Unit
) {
    val steps = onboardingSteps
    val pagerState = rememberPagerState(pageCount = { steps.size })
    val coroutineScope = rememberCoroutineScope()

    val handleNext: () -> Unit = {
        val nextPage = pagerState.currentPage + 1
        if (nextPage < steps.size) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(nextPage)
            }
        } else {
            onNavigateToLogin()
        }
    }

    PocketScaffold(
        config = PocketScaffoldConfig(
            hasTopBar = true,
            isScrollable = false,
            paddingValues = PaddingValues(
                horizontal = SpacingTokens.Semantic.screenPaddingHorizontal,
                vertical = SpacingTokens.Semantic.screenPaddingVertical
            )
        ),
        topBar = {
            PocketTopBar(
                title = "PocketCode",
                subtitle = "Descubre lo que puedes crear",
                actions = listOf(
                    TopBarAction(
                        icon = Icons.Default.Close,
                        contentDescription = "Saltar introducción",
                        onClick = onSkip
                    )
                )
            )
        }
    ) { padding ->
        ResponsiveLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            compactContent = {
                OnboardingPagerContent(
                    pagerState = pagerState,
                    steps = steps,
                    onNext = handleNext,
                    onSkip = onSkip,
                    modifier = Modifier.fillMaxSize()
                )
            },
            mediumContent = {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = SpacingTokens.Semantic.layoutMarginMedium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    PocketCard(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .widthIn(max = 540.dp),
                        variant = ComponentTokens.CardVariant.Elevated
                    ) {
                        OnboardingPagerContent(
                            pagerState = pagerState,
                            steps = steps,
                            onNext = handleNext,
                            onSkip = onSkip,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            expandedContent = {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = SpacingTokens.Semantic.layoutMarginLarge),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    PocketCard(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .widthIn(max = 680.dp),
                        variant = ComponentTokens.CardVariant.Elevated
                    ) {
                        OnboardingPagerContent(
                            pagerState = pagerState,
                            steps = steps,
                            onNext = handleNext,
                            onSkip = onSkip,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OnboardingPagerContent(
    pagerState: PagerState,
    steps: List<OnboardingStep>,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentPage = pagerState.currentPage
    val isLastPage = currentPage == steps.lastIndex

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingLoose),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                OnboardingPage(step = steps[page])
            }
        }

        StepperIndicator(
            totalSteps = steps.size,
            currentStep = currentPage
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PocketButton(
                text = if (isLastPage) "Comenzar" else "Siguiente",
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            )
            PocketButton(
                text = "Saltar por ahora",
                onClick = onSkip,
                variant = ComponentTokens.ButtonVariant.Text
            )
        }
    }
}

@Composable
private fun OnboardingPage(
    step: OnboardingStep,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.Semantic.contentSpacingRelaxed),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
    ) {
        Text(
            text = step.emoji,
            style = TypographyTokens.Display.medium,
            textAlign = TextAlign.Center
        )

        Text(
            text = step.title,
            style = TypographyTokens.Headline.large,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = step.description,
            style = TypographyTokens.Body.medium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private data class OnboardingStep(
    val emoji: String,
    val title: String,
    val description: String
)

private val onboardingSteps = listOf(
    OnboardingStep(
        emoji = "🚀",
        title = "Bienvenido a PocketCode",
        description = "Tu IDE móvil completo para crear, editar y publicar proyectos en cualquier lugar."
    ),
    OnboardingStep(
        emoji = "🤖",
        title = "Asistente IA Integrado",
        description = "Genera código, recibe sugerencias contextuales y colabora con IA en tiempo real."
    ),
    OnboardingStep(
        emoji = "🛒",
        title = "Marketplace de Componentes",
        description = "Descarga componentes, plantillas y recursos para acelerar tu desarrollo."
    )
)