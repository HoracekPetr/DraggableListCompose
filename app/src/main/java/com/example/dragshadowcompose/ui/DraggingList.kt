package com.example.dragshadowcompose.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun <T> DragList(
    modifier: Modifier,
    onDragStop: (info: T?) -> Unit,
    autoScrollThreshold: Dp = 75.dp,
    dragListItems: List<T>,
    indicatorVerticalPadding: Dp = 20.dp,
    indicatorContent: @Composable (dragInfo: DragInfo<T>) -> Unit,
    dragListItem: @Composable (item: T) -> Unit
) {
    val lazyListState = rememberLazyListState()
    var autoScrollSpeed by remember {
        mutableFloatStateOf(0f)
    }

    Box(
        modifier = modifier, contentAlignment = Alignment.Center
    ) {

        var dragIndicatorY by remember {
            mutableFloatStateOf(0f)
        }

        var isDragIndicatorVisible by remember {
            mutableStateOf(false)
        }

        var currentDragInfo by remember {
            mutableStateOf(DragInfo<T>())
        }

        LaunchedEffect(key1 = autoScrollSpeed) {
            if (autoScrollSpeed != 0f) {
                while (isActive) {
                    lazyListState.scrollBy(autoScrollSpeed)
                    delay(10)
                }
            }
        }

        DraggingList(
            dragListItems = dragListItems,
            state = lazyListState,
            onLongPress = {
                dragIndicatorY = it.y
                isDragIndicatorVisible = true
            },
            onDrag = {
                dragIndicatorY += it
            },
            onDragStop = {
                isDragIndicatorVisible = false
                onDragStop(currentDragInfo.data)
            },
            onItemHover = { info ->
                info?.let {
                    currentDragInfo = info
                }
            },
            setAutoScrollSpeed = {
                autoScrollSpeed = it
            },
            autoScrollThreshold = with(LocalDensity.current) { autoScrollThreshold.toPx() },
            dragListItem = dragListItem,
        )

        DragIndicator(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = indicatorVerticalPadding),
            isVisible = isDragIndicatorVisible,
            yPos = dragIndicatorY
        ) {
            indicatorContent(currentDragInfo)
        }
    }
}

@Composable
fun <T> DraggingList(
    modifier: Modifier = Modifier,
    state: LazyListState,
    dragListItems: List<T>,
    onItemHover: (DragInfo<T>?) -> Unit,
    onLongPress: (Offset) -> Unit,
    onDrag: (Float) -> Unit,
    onDragStop: () -> Unit,
    itemSpacing: Dp = 12.dp,
    setAutoScrollSpeed: (Float) -> Unit = { },
    autoScrollThreshold: Float,
    dragListItem: @Composable (item: T) -> Unit
) {

    LazyColumn(
        modifier = modifier
            .padding(16.dp)
            .pointerInput(autoScrollThreshold, setAutoScrollSpeed) {

                fun itemAtOffset(hitPoint: Offset) =
                    state.layoutInfo.visibleItemsInfo.find { itemInfo ->
                        hitPoint.y.toInt() in (itemInfo.offset..itemInfo.offset + itemInfo.size + itemSpacing.roundToPx())
                    }?.index


                detectDragGesturesAfterLongPress(onDragStart = {
                    onItemHover(
                        itemAtOffset(it)?.let { index ->
                            DragInfo(
                                data = dragListItems.getOrNull(
                                    index
                                )
                            )
                        }
                    )
                    onLongPress(it)
                }, onDrag = { change, drag ->
                    onDrag(drag.y)
                    onItemHover(
                        itemAtOffset(change.position)?.let { index ->
                            DragInfo(data = dragListItems.getOrNull(index))
                        }
                    )

                    val distFromBottom = state.layoutInfo.viewportSize.height - change.position.y
                    val distFromTop = change.position.y

                    setAutoScrollSpeed(
                        when {
                            distFromBottom < autoScrollThreshold -> autoScrollThreshold - distFromBottom
                            distFromTop < autoScrollThreshold -> -(autoScrollThreshold - distFromTop)
                            else -> 0f
                        }
                    )
                }, onDragCancel = {
                    setAutoScrollSpeed(0f)
                }, onDragEnd = {
                    setAutoScrollSpeed(0f)
                    onDragStop()
                })
            }, verticalArrangement = Arrangement.spacedBy(itemSpacing), state = state
    ) {
        items(dragListItems) { item ->
            dragListItem(item)
        }
    }
}

@Composable
fun DragIndicator(
    modifier: Modifier,
    isVisible: Boolean,
    yPos: Float,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier
        .wrapContentSize()
        .graphicsLayer { translationY = yPos }
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(200)) + scaleIn(tween(200)),
            exit = fadeOut(tween(200)) + scaleOut(tween(200))
        ) {
            content()
        }
    }
}

data class DragInfo<T>(
    val data: T? = null
)