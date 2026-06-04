package io.github.bengidev.openzone.home.presenter

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@Composable
fun rememberClearTextInputFocus(): () -> Unit {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    return remember(focusManager, keyboardController) {
        {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }
}

/** Clears text-field focus and hides the IME when the user taps non-interactive chrome. */
fun Modifier.clearFocusOnTapOutside(): Modifier = composed {
    val clearFocus = rememberClearTextInputFocus()
    pointerInput(clearFocus) {
        detectTapGestures(onTap = { clearFocus() })
    }
}
