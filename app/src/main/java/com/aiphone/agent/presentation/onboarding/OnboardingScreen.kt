package com.aiphone.agent.presentation.onboarding
import androidx.compose.animation.*; import androidx.compose.animation.core.*
import androidx.compose.foundation.*; import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*; import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons; import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*; import androidx.compose.runtime.*
import androidx.compose.ui.*; import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.*; import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight; import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.aiphone.agent.presentation.theme.*

data class OnboardingPage(val icon: ImageVector, val color: Color, val title: String, val description: String)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit, viewModel: OnboardingViewModel = hiltViewModel()) {
    val pages = listOf(
        OnboardingPage(Icons.Filled.AutoAwesome, BrandPrimary, "Control your phone with words", "AI Phone Agent understands natural language and executes complex multi-step tasks on your device."),
        OnboardingPage(Icons.Filled.VpnKey, BrandSecondary, "Bring your own AI key", "Connect your OpenAI, Anthropic, Gemini, or OpenRouter API key. It stays on-device, encrypted."),
        OnboardingPage(Icons.Filled.Security, BrandTertiary, "Stay in full control", "Sandbox mode restricts file access to a chosen folder. Accessibility service can be revoked anytime.")
    )
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize().background(Background)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { index ->
            OnboardingPage(page = pages[index])
        }
        Column(Modifier.align(Alignment.BottomCenter).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size) { i ->
                    Box(Modifier.height(4.dp).width(if (pagerState.currentPage == i) 24.dp else 8.dp)
                        .background(if (pagerState.currentPage == i) BrandPrimary else OnSurfaceVariant.copy(alpha = 0.4f), CircleShape))
                }
            }
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else { viewModel.completeOnboarding(); onComplete() }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
            ) {
                Text(if (pagerState.currentPage < pages.size - 1) "Next" else "Get Started",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun OnboardingPage(page: OnboardingPage) {
    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(120.dp).background(Brush.radialGradient(listOf(page.color.copy(alpha = 0.25f), Color.Transparent)), CircleShape),
            contentAlignment = Alignment.Center) {
            Icon(page.icon, null, tint = page.color, modifier = Modifier.size(60.dp))
        }
        Spacer(Modifier.height(40.dp))
        Text(page.title, style = MaterialTheme.typography.headlineMedium, color = OnSurface, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(page.description, style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant, textAlign = TextAlign.Center)
    }
}