package com.pocketcode.core.ui.components.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.tokens.TypographyTokens

enum class PocketToastStyle {
    Info,
    Success,
    Warning,
    Error
}

enum class PocketToastDuration(val durationMillis: Long) {
    Short(2_500L),
    Extended(5_000L)
}

data class PocketToastData(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val style: PocketToastStyle = PocketToastStyle.Info,
    val duration: PocketToastDuration = PocketToastDuration.Short,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)

@Stable
class PocketToastState internal constructor(
    private val scope: CoroutineScope
) {
    private val toasts: SnapshotStateList<PocketToastData> = mutableStateListOf()

    fun currentToasts(): List<PocketToastData> = toasts

    fun show(data: PocketToastData) {
        toasts += data
        scope.launch {
            delay(data.duration.durationMillis)
            dismiss(data.id)
        }
    }

    fun show(
        message: String,
        style: PocketToastStyle = PocketToastStyle.Info,
    duration: PocketToastDuration = PocketToastDuration.Short,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        show(
            PocketToastData(
                message = message,
                style = style,
                duration = duration,
                actionLabel = actionLabel,
                onAction = onAction
            )
        )
    }

    fun dismiss(id: String) {
        toasts.removeAll { it.id == id }
    }
}

@Composable
fun rememberPocketToastState(): PocketToastState {
    val scope = rememberCoroutineScope()
    return remember(scope) { PocketToastState(scope) }
}

@Composable
fun PocketToastHost(
    state: PocketToastState,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.BottomCenter
) {
    val toasts = state.currentToasts()
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingTokens.Semantic.screenPaddingHorizontal, vertical = SpacingTokens.large),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.small),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            toasts.forEach { toast ->
                key(toast.id) {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) {
                        PocketToast(
                            data = toast,
                            onDismiss = { state.dismiss(toast.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PocketToast(
    data: PocketToastData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = data.style.accentColor()
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent),
        tonalElevation = 4.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = SpacingTokens.medium, vertical = SpacingTokens.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small)
        ) {
            Icon(
                imageVector = data.style.icon(),
                contentDescription = null,
                tint = accentColor
            )
            Text(
                text = data.message,
                style = TypographyTokens.Body.medium,
                fontWeight = FontWeight.Medium,
                color = ColorTokens.onSurface,
                modifier = Modifier.weight(1f, fill = true)
            )
            data.actionLabel?.let { label ->
                TextButton(
                    onClick = {
                        data.onAction?.invoke()
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = accentColor)
                ) {
                    Text(text = label)
                }
            }
        }
    }
}

@Composable
private fun PocketToastStyle.accentColor(): Color = when (this) {
    PocketToastStyle.Info -> ColorTokens.info
    PocketToastStyle.Success -> ColorTokens.Semantic.success500
    PocketToastStyle.Warning -> ColorTokens.Semantic.warning500
    PocketToastStyle.Error -> MaterialTheme.colorScheme.error
}

@Composable
private fun PocketToastStyle.icon(): ImageVector = when (this) {
    PocketToastStyle.Info -> Icons.Outlined.Info
    PocketToastStyle.Success -> Icons.Outlined.CheckCircle
    PocketToastStyle.Warning -> Icons.Outlined.WarningAmber
    PocketToastStyle.Error -> Icons.Outlined.ErrorOutline
}
