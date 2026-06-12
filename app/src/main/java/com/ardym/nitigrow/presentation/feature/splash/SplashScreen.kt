package com.ardym.nitigrow.presentation.feature.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ardym.nitigrow.R
import com.ardym.nitigrow.ui.theme.Theme
import kotlinx.coroutines.flow.collectLatest

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
    val colors = Theme.colors
    val logoShape = RoundedCornerShape(28.dp)
    val pulse by rememberInfiniteTransition(label = "splash-pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo-scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.paper)
    ) {
        // Soft radial glows — turmeric top-right, brand bottom-left.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-120).dp)
                .size(320.dp)
                .background(
                    Brush.radialGradient(
                        0f to colors.turmeric.copy(alpha = 0.18f),
                        0.65f to Color.Transparent
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-110).dp, y = 80.dp)
                .size(300.dp)
                .background(
                    Brush.radialGradient(
                        0f to colors.brand.copy(alpha = 0.12f),
                        0.65f to Color.Transparent
                    )
                )
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                    }
                    .size(100.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = logoShape,
                        ambientColor = colors.ink.copy(alpha = 0.08f),
                        spotColor = colors.ink.copy(alpha = 0.08f)
                    )
                    .background(colors.card, logoShape)
                    .border(1.dp, colors.border2, logoShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.mipmap.ic_launcher_foreground),
                    contentDescription = "NitiGrow logo",
                    modifier = Modifier.size(80.dp)
                )
            }
            Spacer(Modifier.height(22.dp))
            Text(
                text = "NitiGrow",
                color = colors.ink,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 34.sp,
                    lineHeight = 40.sp,
                    letterSpacing = (-0.5).sp
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "WHATSAPP BUSINESS CRM",
                color = colors.muted,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.2.sp)
            )
        }

        Text(
            text = "by Websbaba Technologies",
            color = colors.muted2,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 34.dp)
        )
    }
}
