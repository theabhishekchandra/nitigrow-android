package com.websbaba.nitigrow.presentation.feature.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.websbaba.nitigrow.presentation.feature.inbox.list.components.Avatar
import com.websbaba.nitigrow.core.ui.theme.BubbleInShape
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiStatusBar
import com.websbaba.nitigrow.core.ui.theme.NitiTone
import com.websbaba.nitigrow.core.ui.theme.NitiType
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

/** Three-slide intro. "Skip" and "Get started" both mark onboarding done. */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pager = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()
    val colors = Niti.colors
    val finish = { viewModel.finish(); onFinish() }

    NitiStatusBar(color = colors.surface, darkIcons = colors.isLight)

    Column(modifier = Modifier.fillMaxSize().background(colors.surface).statusBarsPadding().navigationBarsPadding()) {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 12.dp)
        ) {
            Text(
                text = "Skip",
                style = NitiType.body.copy(fontWeight = FontWeight.SemiBold),
                color = colors.primary,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(role = Role.Button, onClick = finish)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        HorizontalPager(state = pager, modifier = Modifier.fillMaxWidth().weight(1f)) { page ->
            Column(
                verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)
            ) {
                SlideArt(page)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                    Text(
                        text = slides[page].title,
                        style = NitiType.display.copy(fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.8).sp),
                        color = colors.onSurface
                    )
                    Text(text = slides[page].body, style = NitiType.body.copy(fontSize = 16.sp, lineHeight = 24.sp), color = colors.onSurfaceVariant)
                }
            }
        }

        val isLast = pager.currentPage == slides.lastIndex
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 32.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(slides.size) { index ->
                    val active = pager.currentPage == index
                    val width by animateDpAsState(if (active) 28.dp else 8.dp, label = "dot-width")
                    val color by animateColorAsState(if (active) colors.primary else colors.outlineVariant, label = "dot-color")
                    Box(Modifier.width(width).height(8.dp).background(color, RoundedCornerShape(4.dp)))
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(colors.primary)
                    .clickable(role = Role.Button) {
                        if (isLast) finish() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                    }
                    .padding(horizontal = 28.dp)
            ) {
                Text(
                    text = if (isLast) "Get started" else "Next",
                    style = NitiType.body.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.onPrimary
                )
                if (!isLast) {
                    Icon(NitiIcons.ArrowRight, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ── Slide illustrations ──────────────────────────────────────────────────────
// Sample content only: these are decorative mock-ups of the app, not account data.

@Composable
private fun SlideArt(page: Int) {
    val colors = Niti.colors
    val tone = when (page) {
        0 -> colors.primaryTone
        1 -> colors.secondaryTone
        else -> colors.tertiaryTone
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(316.dp)
            .clip(RoundedCornerShape(36.dp))
            .background(tone.container)
    ) {
        when (page) {
            0 -> InboxArt()
            1 -> BroadcastArt(tone)
            else -> PaymentArt()
        }
    }
}

@Composable
private fun MockCard(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(20.dp), content: @Composable () -> Unit) {
    Box(modifier = modifier.shadow(2.dp, shape).background(Niti.colors.surface, shape)) { content() }
}

@Composable
private fun InboxArt() {
    val colors = Niti.colors
    val rows = listOf(
        Triple("Priya Sharma", "Can you deliver by Friday?", "10:42" to 2),
        Triple("Rahul Verma", "Payment done, please confirm", "10:15" to 0),
        Triple("Anita Desai", "Sharing the catalog now.", "Yesterday" to 0),
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEachIndexed { i, (name, msg, meta) ->
            MockCard(modifier = Modifier.padding(start = listOf(0, 20, 8)[i].dp).width(290.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Avatar(name = name, url = null, sizeDp = 40)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, style = NitiType.bodyCompact.copy(fontWeight = FontWeight.SemiBold), color = colors.onSurface, maxLines = 1)
                        Text(msg, style = NitiType.label.copy(fontWeight = FontWeight.Normal), color = colors.onSurfaceVariant, maxLines = 1)
                    }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(meta.first, style = NitiType.caption.copy(fontSize = 11.sp), color = colors.onSurfaceVariant)
                        if (meta.second > 0) {
                            Text(
                                meta.second.toString(),
                                style = NitiType.caption.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.onPrimary,
                                modifier = Modifier.clip(CircleShape).background(colors.primary).padding(horizontal = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BroadcastArt(tone: NitiTone) {
    val colors = Niti.colors
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.width(290.dp)) {
        MockCard(shape = RoundedCornerShape(24.dp, 24.dp, 24.dp, 8.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Diwali offer", style = NitiType.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp), color = tone.onContainer)
                Text(
                    "Namaste! Flat 20% off on all sweets this week. Reply YES to claim.",
                    style = NitiType.bodyCompact,
                    color = colors.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        MockCard(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
                listOf("Delivered" to 0.96f, "Read" to 0.71f, "Replied" to 0.18f).forEach { (label, value) ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(label, style = NitiType.caption.copy(fontWeight = FontWeight.SemiBold), color = tone.onContainer)
                            Text("${(value * 100).toInt()}%", style = NitiType.caption.copy(fontWeight = FontWeight.SemiBold), color = tone.onContainer)
                        }
                        Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(colors.track)) {
                            Box(Modifier.fillMaxWidth(value).height(8.dp).clip(RoundedCornerShape(4.dp)).background(colors.primary))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentArt() {
    val colors = Niti.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.width(290.dp)) {
        MockCard(shape = BubbleInShape) {
            Text(
                "Total for 5 kg kaju katli?",
                style = NitiType.bodyCompact,
                color = colors.onSurface,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
        MockCard(shape = RoundedCornerShape(24.dp, 24.dp, 6.dp, 24.dp), modifier = Modifier.width(250.dp).align(Alignment.End)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(36.dp).background(colors.primaryTone.container, CircleShape)
                    ) {
                        Icon(NitiIcons.Rupee, contentDescription = null, tint = colors.primaryTone.onContainer, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("Payment request", style = NitiType.label, color = colors.onSurfaceVariant)
                        Text("₹4,250", style = NitiType.title.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
                    }
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(22.dp)).background(colors.primary)
                ) {
                    Text("Pay with UPI", style = NitiType.bodyCompact.copy(fontWeight = FontWeight.SemiBold), color = colors.onPrimary)
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .align(Alignment.End)
                .heightIn(min = 24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.primaryTone.container)
                .padding(horizontal = 9.dp)
        ) {
            Icon(NitiIcons.Check, contentDescription = null, tint = colors.primaryTone.onContainer, modifier = Modifier.size(14.dp))
            Text("Paid", style = NitiType.caption.copy(fontWeight = FontWeight.SemiBold), color = colors.primaryTone.onContainer)
        }
    }
}
