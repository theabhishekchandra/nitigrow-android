package com.websbaba.nitigrow.presentation.feature.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.websbaba.nitigrow.R
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiStatusBar
import com.websbaba.nitigrow.core.ui.theme.NitiType
import kotlinx.coroutines.flow.collectLatest

/**
 * Brand reveal while the session is validated: soft tonal blobs, the logo tile,
 * wordmark and tagline, and a looping progress sliver above the byline.
 */
@Composable
fun SplashScreen(
    onNavigate: (String) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is SplashEffect.NavigateTo -> onNavigate(effect.route)
            }
        }
    }
    val colors = Niti.colors
    NitiStatusBar(color = colors.surface, darkIcons = colors.isLight)

    val transition = rememberInfiniteTransition(label = "splash")
    val slide by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "progress-slide"
    )

    Box(modifier = Modifier.fillMaxSize().background(colors.surface)) {
        // Decorative tonal blobs, top-left, bottom-right and a small accent.
        Box(
            Modifier
                .align(Alignment.TopStart)
                .offset(x = (-120).dp, y = (-100).dp)
                .size(380.dp)
                .alpha(0.55f)
                .background(colors.primaryTone.container, CircleShape)
        )
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 140.dp, y = 60.dp)
                .size(400.dp)
                .alpha(0.6f)
                .background(colors.secondaryTone.container, CircleShape)
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-40).dp, y = 120.dp)
                .size(64.dp)
                .alpha(0.9f)
                .background(colors.tertiaryTone.container, CircleShape)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            val tile = RoundedCornerShape(44.dp)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(148.dp)
                    .shadow(elevation = 8.dp, shape = tile)
                    .background(colors.logoTile, tile)
            ) {
                Image(
                    painter = painterResource(R.mipmap.ic_launcher_foreground),
                    contentDescription = "NitiGrow logo",
                    modifier = Modifier.size(112.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "NitiGrow",
                    style = NitiType.display.copy(fontSize = 44.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp),
                    color = colors.onSurface
                )
                Text(
                    text = "WHATSAPP BUSINESS CRM",
                    style = NitiType.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 2.4.sp),
                    color = colors.onSurfaceVariant
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 44.dp)
        ) {
            Box(Modifier.width(120.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(colors.track)) {
                Box(
                    Modifier
                        .offset(x = (56 * slide).dp)
                        .width(64.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.primary)
                )
            }
            Text(text = "by Websbaba Technologies", style = NitiType.label, color = colors.onSurfaceVariant)
        }
    }
}
