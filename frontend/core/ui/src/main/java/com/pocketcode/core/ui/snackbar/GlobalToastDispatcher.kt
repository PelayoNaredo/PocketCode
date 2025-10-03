package com.pocketcode.core.ui.snackbar

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.pocketcode.core.ui.components.feedback.PocketToastDuration
import com.pocketcode.core.ui.components.feedback.PocketToastStyle
import java.util.UUID

@Immutable
data class GlobalToastEvent(
    val message: String,
    val style: PocketToastStyle = PocketToastStyle.Info,
    val origin: GlobalSnackbarOrigin = GlobalSnackbarOrigin.SHELL,
    val duration: PocketToastDuration = PocketToastDuration.Short,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val analyticsId: String = UUID.randomUUID().toString()
)

class GlobalToastDispatcher internal constructor(
    private val onDispatch: (GlobalToastEvent) -> Unit
) {
    fun dispatch(event: GlobalToastEvent) {
        onDispatch(event)
    }

    fun showMessage(
        message: String,
        style: PocketToastStyle = PocketToastStyle.Info,
        origin: GlobalSnackbarOrigin = GlobalSnackbarOrigin.SHELL,
        duration: PocketToastDuration = PocketToastDuration.Short,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        dispatch(
            GlobalToastEvent(
                message = message,
                style = style,
                origin = origin,
                duration = duration,
                actionLabel = actionLabel,
                onAction = onAction
            )
        )
    }

    companion object {
        val NoOp = GlobalToastDispatcher { _ -> }

        fun from(onDispatch: (GlobalToastEvent) -> Unit): GlobalToastDispatcher {
            return GlobalToastDispatcher(onDispatch)
        }
    }
}

val LocalGlobalToastDispatcher = staticCompositionLocalOf {
    GlobalToastDispatcher.NoOp
}
