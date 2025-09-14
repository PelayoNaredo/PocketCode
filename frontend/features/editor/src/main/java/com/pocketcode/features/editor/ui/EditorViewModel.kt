package com.pocketcode.features.editor.ui

// import androidx.lifecycle.ViewModel
// import dagger.hilt.android.lifecycle.HiltViewModel
// import javax.inject.Inject
// import com.pocketcode.domain.ide.usecase.GenerateCodeUseCase

/**
 * This is the ViewModel for the Editor screen. It follows the MVI (Model-View-Intent) pattern.
 *
 * Responsibilities:
 * - Hold and manage the UI state for the `EditorScreen`.
 * - Expose the state as a stream of data (e.g., using StateFlow).
 * - Contain the presentation logic for handling user intents (events) from the UI.
 * - Call use cases from the `domain` layer to perform business logic.
 * - Annotated with `@HiltViewModel` so Hilt can create and inject it with its dependencies.
 *
 * Interacts with:
 * - `EditorScreen` (the View): The screen observes state from the ViewModel and sends events to it.
 * - Use Cases from the `:domain` layer (e.g., `GenerateCodeUseCase`): The ViewModel calls
 *   use cases to execute business operations.
 * - Hilt: For dependency injection.
 */
// @HiltViewModel
class EditorViewModel /*@Inject constructor(
    private val generateCodeUseCase: GenerateCodeUseCase
) : ViewModel()*/ {
    // val state = MutableStateFlow<EditorState>(EditorState.Initial)
    //
    // fun onEvent(event: EditorEvent) {
    //     when (event) {
    //         is EditorEvent.GenerateCode -> {
    //             // viewModelScope.launch {
    //             //     val result = generateCodeUseCase(event.prompt)
    //             //     // update state with result
    //             // }
    //         }
    //     }
    // }
}
