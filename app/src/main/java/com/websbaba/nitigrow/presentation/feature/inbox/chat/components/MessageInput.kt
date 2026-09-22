package com.websbaba.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.presentation.components.NitiIconButton
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiType

/**
 * Chat composer — a tonal pill holding the text field and attach button, with
 * a 52dp round send button beside it. Send is muted until there is text.
 *
 * [enabled] false means a confirmed-closed 24h window: WhatsApp will not deliver a
 * free-text message here, so typing and attaching are blocked rather than letting the
 * user compose something that looks sent but was never going anywhere.
 */
@Composable
fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = Niti.colors
    val canSend = enabled && text.isNotBlank()
    val textStyle = NitiType.body.copy(fontSize = 16.sp, color = colors.onSurface)

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .navigationBarsPadding()
            .imePadding()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(colors.surfaceContainer)
                .padding(start = 18.dp, end = 4.dp)
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                enabled = enabled,
                maxLines = 5,
                textStyle = textStyle,
                cursorBrush = SolidColor(colors.primary),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (text.isEmpty()) {
                            Text(
                                text = if (enabled) "Message" else "Send a template to reply",
                                style = textStyle,
                                color = colors.outline,
                                maxLines = 1
                            )
                        }
                        inner()
                    }
                },
                modifier = Modifier.weight(1f).padding(vertical = 14.dp)
            )
            NitiIconButton(
                icon = NitiIcons.Attach,
                contentDescription = "Attach file",
                onClick = onAttachClick,
                enabled = enabled,
                tint = if (enabled) colors.onSurfaceVariant else colors.outline
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (canSend) colors.primary else colors.surfaceHigh)
                .clickable(enabled = canSend, role = Role.Button, onClick = onSend)
        ) {
            Icon(
                imageVector = NitiIcons.Send,
                contentDescription = "Send message",
                tint = if (canSend) colors.onPrimary else colors.outline,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
