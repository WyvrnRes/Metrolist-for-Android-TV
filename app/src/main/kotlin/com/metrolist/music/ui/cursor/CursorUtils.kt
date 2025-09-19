package com.metrolist.music.ui.cursor

import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * Utility functions for cursor hit testing and interaction
 */
object CursorUtils {

    /**
     * Simulate a touch event at the given position on a view
     */
    fun simulateTouch(view: View, position: Offset) {
        val downTime = System.currentTimeMillis()
        val eventTime = System.currentTimeMillis()

        val downEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_DOWN,
            position.x,
            position.y,
            0
        )

        val upEvent = MotionEvent.obtain(
            downTime,
            eventTime + 50,
            MotionEvent.ACTION_UP,
            position.x,
            position.y,
            0
        )

        view.dispatchTouchEvent(downEvent)
        view.dispatchTouchEvent(upEvent)

        downEvent.recycle()
        upEvent.recycle()
    }
}

/**
 * A clickable area that can be detected by the TV remote cursor
 */
@Composable
fun TVRemoteClickable(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var bounds by remember { mutableStateOf(Rect.Zero) }
    val controller = LocalTVRemoteCursorController.current

    Box(
        modifier = modifier.onGloballyPositioned { layoutCoordinates ->
            bounds = layoutCoordinates.boundsInRoot()
        }
    ) {
        content()
    }

    DisposableEffect(bounds, controller) {
        if (controller != null && bounds != Rect.Zero) {
            val clickableArea = ClickableArea(bounds, onClick)
            controller.registerClickableArea(clickableArea)

            onDispose {
                controller.unregisterClickableArea(clickableArea)
            }
        } else {
            onDispose { }
        }
    }
}
