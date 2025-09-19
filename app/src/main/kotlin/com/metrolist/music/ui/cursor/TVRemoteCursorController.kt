package com.metrolist.music.ui.cursor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.IntSize

/**
 * Data class representing a clickable area that can be detected by the cursor
 */
data class ClickableArea(
    val bounds: Rect,
    val onClick: () -> Unit,
    val enabled: Boolean = true
)

/**
 * Controller for managing TV remote cursor state and behavior
 */
class TVRemoteCursorController {

    /**
     * Current cursor position
     */
    var cursorPosition by mutableStateOf(Offset.Zero)
        private set

    /**
     * Movement step size for D-pad navigation
     */
    var stepSize by mutableStateOf(40f)

    /**
     * Screen bounds for cursor movement
     */
    var screenBounds by mutableStateOf(Size.Zero)

    /**
     * Cursor size for bounds calculation
     */
    var cursorSize by mutableStateOf(Size(28f, 28f))

    /**
     * Callback for when cursor performs a click action
     */
    var onCursorClick: ((Offset) -> Unit)? = null

    /**
     * List of registered clickable areas
     */
    private val clickableAreas = mutableListOf<ClickableArea>()

    /**
     * Initialize cursor position to center of screen
     */
    fun initializeCursor(screenSize: IntSize) {
        screenBounds = Size(screenSize.width.toFloat(), screenSize.height.toFloat())
        cursorPosition = Offset(
            (screenBounds.width / 2f) - (cursorSize.width / 2f),
            (screenBounds.height / 2f) - (cursorSize.height / 2f)
        )
    }

    /**
     * Register a clickable area
     */
    fun registerClickableArea(area: ClickableArea) {
        clickableAreas.add(area)
    }

    /**
     * Unregister a clickable area
     */
    fun unregisterClickableArea(area: ClickableArea) {
        clickableAreas.remove(area)
    }

    /**
     * Find clickable area at the given position
     */
    fun findClickableAreaAt(position: Offset): ClickableArea? {
        return clickableAreas.firstOrNull { area ->
            area.bounds.contains(position)
        }
    }

    /**
     * Clear all registered clickable areas
     */
    fun clearClickableAreas() {
        clickableAreas.clear()
    }

    /**
     * Handle key events from TV remote
     * @param keyEvent The key event to process
     * @return true if the event was consumed
     */
    fun handleKeyEvent(keyEvent: KeyEvent): Boolean {
        if (keyEvent.type != KeyEventType.KeyDown) return false

        return when (keyEvent.key.keyCode.toInt()) {
            android.view.KeyEvent.KEYCODE_DPAD_LEFT -> {
                moveCursor(-stepSize, 0f)
                true
            }

            android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                moveCursor(stepSize, 0f)
                true
            }

            android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                moveCursor(0f, -stepSize)
                true
            }

            android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                moveCursor(0f, stepSize)
                true
            }

            android.view.KeyEvent.KEYCODE_DPAD_CENTER,
            android.view.KeyEvent.KEYCODE_ENTER -> {
                performClick()
                true
            }

            else -> false
        }
    }

    /**
     * Move cursor by the specified delta, respecting screen bounds
     */
    private fun moveCursor(dx: Float, dy: Float) {
        val maxX = (screenBounds.width - cursorSize.width).coerceAtLeast(0f)
        val maxY = (screenBounds.height - cursorSize.height).coerceAtLeast(0f)

        cursorPosition = Offset(
            (cursorPosition.x + dx).coerceIn(0f, maxX),
            (cursorPosition.y + dy).coerceIn(0f, maxY)
        )
    }

    /**
     * Perform click action at current cursor position
     */
    private fun performClick() {
        val clickPosition = Offset(
            cursorPosition.x + (cursorSize.width / 2f),
            cursorPosition.y + (cursorSize.height / 2f)
        )
        onCursorClick?.invoke(clickPosition)
    }

    /**
     * Set cursor position programmatically
     */
    fun updateCursorPosition(newPosition: Offset) {
        val maxX = (screenBounds.width - cursorSize.width).coerceAtLeast(0f)
        val maxY = (screenBounds.height - cursorSize.height).coerceAtLeast(0f)

        cursorPosition = Offset(
            newPosition.x.coerceIn(0f, maxX),
            newPosition.y.coerceIn(0f, maxY)
        )
    }
}