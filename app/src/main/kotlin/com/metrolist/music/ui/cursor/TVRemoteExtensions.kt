package com.metrolist.music.ui.cursor

import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role

/**
 * Extension function to make any Composable TV remote clickable
 * This is useful for retrofitting existing UI components
 */
@Composable
fun Modifier.tvRemoteClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
    indication: Indication? = null,
    interactionSource: MutableInteractionSource?,
): Modifier {
    val controller = LocalTVRemoteCursorController.current
    var bounds by remember { mutableStateOf(Rect.Zero) }

    val localIndication = LocalIndication.current
    val interactionSource =
        if (localIndication is IndicationNodeFactory) {
            // We can fast path here as it will be created inside clickable lazily
            null
        } else {
            // We need an interaction source to pass between the indication modifier and
            // clickable, so
            // by creating here we avoid another composed down the line
            remember { MutableInteractionSource() }
        }

    val modifier = this
        .onGloballyPositioned { layoutCoordinates ->
            bounds = layoutCoordinates.boundsInRoot()
        }
        .clickable(
            enabled = enabled,
            onClickLabel = onClickLabel,
            onClick = onClick,
            role = role,
            indication = localIndication,
            interactionSource = interactionSource,
        ) // Preserve normal click behavior

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

    return modifier
}

@Composable
fun Modifier.tvRemoteCombinedClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    hapticFeedbackEnabled: Boolean = true,
    onClick: () -> Unit,
    indication: Indication? = null,
    interactionSource: MutableInteractionSource?,
): Modifier {
    val controller = LocalTVRemoteCursorController.current
    var bounds by remember { mutableStateOf(Rect.Zero) }

    val localIndication = LocalIndication.current
    val interactionSource =
        if (localIndication is IndicationNodeFactory) {
            // We can fast path here as it will be created inside clickable lazily
            null
        } else {
            // We need an interaction source to pass between the indication modifier and
            // clickable, so
            // by creating here we avoid another composed down the line
            remember { MutableInteractionSource() }
        }

    val modifier = this
        .onGloballyPositioned { layoutCoordinates ->
            bounds = layoutCoordinates.boundsInRoot()
        }
        .combinedClickable(
            enabled = enabled,
            onClickLabel = onClickLabel,
            onLongClickLabel = onLongClickLabel,
            onLongClick = onLongClick,
            onDoubleClick = onDoubleClick,
            onClick = onClick,
            role = role,
            indication = localIndication,
            interactionSource = interactionSource,
            hapticFeedbackEnabled = hapticFeedbackEnabled,
        ) // Preserve normal click behavior

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

    return modifier
}

/**
 * Higher-order composable that wraps content with TV remote click functionality
 * Alternative to TVRemoteClickable for inline usage
 */
@Composable
fun WithTVRemoteClick(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource?,
    indication: Indication? = null,
    content: @Composable () -> Unit
) {
    val localIndication = LocalIndication.current
    val interactionSource =
        if (localIndication is IndicationNodeFactory) {
            // We can fast path here as it will be created inside clickable lazily
            null
        } else {
            // We need an interaction source to pass between the indication modifier and
            // clickable, so
            // by creating here we avoid another composed down the line
            remember { MutableInteractionSource() }
        }
    Box(
        modifier = Modifier.tvRemoteClickable(
            enabled = enabled,
            onClickLabel = onClickLabel,
            onClick = onClick,
            role = role,
            indication = localIndication,
            interactionSource = interactionSource
        )
    ) {
        content()
    }
}