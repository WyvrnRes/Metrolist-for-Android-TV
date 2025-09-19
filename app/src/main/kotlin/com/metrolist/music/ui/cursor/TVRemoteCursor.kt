package com.metrolist.music.ui.cursor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * Composable that displays the TV remote cursor overlay
 */
@Composable
fun TVRemoteCursor(
    controller: TVRemoteCursorController,
    modifier: Modifier = Modifier,
    cursorColor: Color = Color.White,
    cursorAlpha: Float = 0.9f
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    // Initialize cursor when screen configuration changes
    LaunchedEffect(configuration.screenWidthDp, configuration.screenHeightDp) {
        val screenSize = IntSize(
            with(density) { configuration.screenWidthDp.dp.toPx().toInt() },
            with(density) { configuration.screenHeightDp.dp.toPx().toInt() }
        )
        controller.initializeCursor(screenSize)
    }

    Box(
        modifier = modifier
            .offset(
                x = with(density) { controller.cursorPosition.x.toDp() },
                y = with(density) { controller.cursorPosition.y.toDp() }
            )
            .size(
                width = with(density) { controller.cursorSize.width.toDp() },
                height = with(density) { controller.cursorSize.height.toDp() }
            )
            .alpha(cursorAlpha)
            .background(
                color = cursorColor,
                shape = CircleShape
            )
    )
}