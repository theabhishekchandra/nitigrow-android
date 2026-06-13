package com.websbaba.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.websbaba.nitigrow.ui.theme.Theme

/**
 * Chat composer — paper surface with a top hairline, attachment button,
 * pill text field and a 42dp brand circular send button.
 */
@Composable
fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canSend = text.isNotBlank()
    val pillShape = RoundedCornerShape(999.dp)

    Column(modifier = modifier.fillMaxWidth().background(Theme.colors.paper).navigationBarsPadding().imePadding()) {
        HorizontalDivider(thickness = 1.dp, color = Theme.colors.border)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp)
        ) {
            IconButton(onClick = onAttachClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Filled.AttachFile,
                    contentDescription = "Attach",
                    tint = Theme.colors.muted,
                    modifier = Modifier.size(21.dp)
                )
            }
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                maxLines = 5,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Theme.colors.ink),
                cursorBrush = SolidColor(Theme.colors.brand),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (text.isEmpty()) {
                            Text(
                                text = "Message",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Theme.colors.muted2,
                                maxLines = 1
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .clip(pillShape)
                    .background(Theme.colors.card)
                    .border(1.dp, Theme.colors.border, pillShape)
                    .padding(horizontal = 16.dp, vertical = 11.dp)
            )
            val sendShadow = if (canSend) {
                Modifier.shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    ambientColor = Theme.colors.brand,
                    spotColor = Theme.colors.brand
                )
            } else {
                Modifier
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .then(sendShadow)
                    .clip(CircleShape)
                    .background(if (canSend) Theme.colors.brand else Theme.colors.paper2)
                    .clickable(enabled = canSend, onClick = onSend),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (canSend) Theme.colors.paper else Theme.colors.muted2,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
