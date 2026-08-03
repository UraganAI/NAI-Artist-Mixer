package com.example.util

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.mouseWheelScroll(scrollState: ScrollState): Modifier = this.pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val delta = event.changes.fold(0f) { acc, change -> acc + change.scrollDelta.y }
            if (delta != 0f) {
                scrollState.dispatchRawDelta(delta * 80f)
                event.changes.forEach { it.consume() }
            }
        }
    }
}

fun Modifier.mouseWheelScrollGrid(gridState: LazyGridState): Modifier = this.pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val delta = event.changes.fold(0f) { acc, change -> acc + change.scrollDelta.y }
            if (delta != 0f) {
                gridState.dispatchRawDelta(delta * 80f)
                event.changes.forEach { it.consume() }
            }
        }
    }
}

fun Modifier.mouseWheelScrollList(listState: LazyListState): Modifier = this.pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val delta = event.changes.fold(0f) { acc, change -> acc + change.scrollDelta.y }
            if (delta != 0f) {
                listState.dispatchRawDelta(delta * 80f)
                event.changes.forEach { it.consume() }
            }
        }
    }
}

