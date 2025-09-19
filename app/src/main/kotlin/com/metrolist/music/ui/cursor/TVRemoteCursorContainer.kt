package com.metrolist.music.ui.cursor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize

/**
 * Composition local for accessing the cursor controller from child composables
 */
val LocalTVRemoteCursorController = compositionLocalOf<TVRemoteCursorController?> { null }

/**
 * Container that provides TV remote cursor functionality to its content.
 * This composable handles key events and displays the cursor overlay.
 */
@Composable
fun TVRemoteCursorContainer(
    modifier: Modifier = Modifier,
    cursorColor: Color = Color.White,
    cursorAlpha: Float = 0.9f,
    stepSize: Float = 40f,
    onCursorClick: ((Offset) -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val controller = remember { TVRemoteCursorController() }
    val focusRequester = remember { FocusRequester() }

    // Configure controller
    controller.stepSize = stepSize
    controller.onCursorClick = { position ->
        // First check if any registered clickable areas contain this position
        val clickableArea = controller.findClickableAreaAt(position)
        if (clickableArea != null) {
            clickableArea.onClick()
        } else {
            // Fall back to custom click handler
            onCursorClick?.invoke(position)
        }
    }

    Box(
        modifier = modifier
            .focusRequester(focusRequester)
            .onKeyEvent { keyEvent ->
                controller.handleKeyEvent(keyEvent)
            }
            .onGloballyPositioned { layoutCoordinates ->
                val screenSize = IntSize(
                    layoutCoordinates.size.width,
                    layoutCoordinates.size.height
                )
                controller.initializeCursor(screenSize)
            }
    ) {
        // Provide the controller to child composables
        CompositionLocalProvider(LocalTVRemoteCursorController provides controller) {
            content()
        }

        // Cursor overlay
        TVRemoteCursor(
            controller = controller,
            cursorColor = cursorColor,
            cursorAlpha = cursorAlpha
        )
    }

    // Request focus when the composable is first composed
    androidx.compose.runtime.LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}