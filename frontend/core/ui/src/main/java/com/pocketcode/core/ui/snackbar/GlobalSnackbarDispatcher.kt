package com.pocketcode.core.ui.snackbar

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.UUID

/**
 * Represents the severity/visual style of a global snackbar message.
 */
@Immutable
enum class GlobalSnackbarSeverity {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

/**
 * Indicates where a snackbar originated from so analytics and telemetry can
 * aggregate correctly.
 */
@Immutable
enum class GlobalSnackbarOrigin {
    SHELL,
    PROJECTS,
    AI_ASSISTANT,
    AUTH,
    DESIGNER,
    SETTINGS,
    MARKETPLACE,
    EDITOR
}

/**
 * Supported durations for global snackbars. These map directly to
 * [androidx.compose.material3.SnackbarDuration].
 */
@Immutable
enum class GlobalSnackbarDuration {
    SHORT,
    LONG
}

/**
 * Immutable payload describing a snackbar request dispatched from any module.
 */
@Immutable
data class GlobalSnackbarEvent(
    val message: String,
    val supportingText: String? = null,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val onDismiss: (() -> Unit)? = null,
    val severity: GlobalSnackbarSeverity = GlobalSnackbarSeverity.INFO,
    val origin: GlobalSnackbarOrigin = GlobalSnackbarOrigin.SHELL,
    val duration: GlobalSnackbarDuration = GlobalSnackbarDuration.SHORT,
    val analyticsId: String = UUID.randomUUID().toString()
)

/**
 * Dispatches snackbar events to the shell. Feature modules obtain the
 * dispatcher via [LocalGlobalSnackbarDispatcher] and simply call
 * [dispatch] without having to depend on the concrete implementation.
 */
class GlobalSnackbarDispatcher internal constructor(
    private val onDispatch: (GlobalSnackbarEvent) -> Unit
) {
    fun dispatch(event: GlobalSnackbarEvent) {
        onDispatch(event)
    }

    companion object {
        val NoOp = GlobalSnackbarDispatcher { _ -> }

        fun from(onDispatch: (GlobalSnackbarEvent) -> Unit): GlobalSnackbarDispatcher {
            return GlobalSnackbarDispatcher(onDispatch)
        }
    }
}

/**
 * CompositionLocal exposing the current [GlobalSnackbarDispatcher].
 */
val LocalGlobalSnackbarDispatcher = staticCompositionLocalOf {
    GlobalSnackbarDispatcher.NoOp
}
