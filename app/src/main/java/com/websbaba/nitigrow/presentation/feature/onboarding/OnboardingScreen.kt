package com.websbaba.nitigrow.presentation.feature.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.websbaba.nitigrow.ui.theme.Theme
import kotlinx.coroutines.launch

private data class Slide(val title: String, val body: String)

private val slides = listOf(
    Slide(
        title = "One inbox for every customer",
        body = "WhatsApp chats from all your customers, organised with tags, labels and team assignment."
    ),
    Slide(
        title = "Broadcasts that actually convert",
        body = "Send festival offers with approved templates and track delivery, reads and replies live."
    ),
    Slide(
        title = "Get paid inside the chat",
        body = "Create UPI payment links in seconds and watch them get paid in real time."
    )
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pager = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()
    val colors = Theme.colors

    Scaffold(containerColor = colors.paper) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Skip — top-right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { viewModel.finish(); onFinish() }) {
                    Text(
                        "Skip",
                        color = colors.muted,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            HorizontalPager(
                state = pager,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val slide = slides[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    SlideBadge(page)
                    Spacer(Modifier.height(32.dp))
                    Text(
                        slide.title,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 26.sp,
                            lineHeight = 32.sp
                        ),
                        color = colors.ink,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        slide.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.ink3,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 30.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                ) {
                    repeat(slides.size) { index ->
                        val active = pager.currentPage == index
                        val width by animateDpAsState(if (active) 22.dp else 7.dp, label = "dot-width")
                        val color by animateColorAsState(
                            if (active) colors.brand else colors.muted3,
                            label = "dot-color"
                        )
                        Box(
                            modifier = Modifier
                                .width(width)
                                .height(7.dp)
                                .background(color, RoundedCornerShape(4.dp))
                        )
                    }
                }
                Spacer(Modifier.height(22.dp))
                val isLast = pager.currentPage == slides.lastIndex
                Button(
                    onClick = {
                        if (isLast) {
                            viewModel.finish(); onFinish()
                        } else {
                            scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.brand,
                        contentColor = colors.paper
                    ),
                    contentPadding = PaddingValues(vertical = 15.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (isLast) "Get started" else "Next",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

/** 150dp soft-tint circle with the slide's glyph, per design slide order. */
@Composable
private fun SlideBadge(page: Int) {
    val colors = Theme.colors
    when (page) {
        0 -> Box(
            modifier = Modifier
                .size(150.dp)
                .background(colors.brandSoft, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.ChatBubble,
                contentDescription = null,
                tint = colors.brand,
                modifier = Modifier.size(62.dp)
            )
        }
        1 -> Box(
            modifier = Modifier
                .size(150.dp)
                .background(colors.accentSoft, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Campaign,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(62.dp)
            )
        }
        else -> Box(
            modifier = Modifier
                .size(150.dp)
                .background(colors.turmericSoft, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "₹",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 64.sp,
                    lineHeight = 72.sp,
                    letterSpacing = 0.sp
                ),
                color = colors.turmericInk
            )
        }
    }
}
