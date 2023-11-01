package com.example.dragshadowcompose.ui

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toIntRect
import androidx.compose.ui.unit.toOffset
import com.example.dragshadowcompose.texts
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun DragList(
    onNavigate: (String) -> Unit
) {

    val lazyListState = rememberLazyListState()
    var autoScrollSpeed by remember {
        mutableFloatStateOf(0f)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        var dragIndicatorY by remember {
            mutableFloatStateOf(0f)
        }

        var isDragIndicatorVisible by remember {
            mutableStateOf(false)
        }

        var currentDraggedName by remember {
            mutableStateOf("")
        }

        var currentHoveredIndex by remember {
            mutableIntStateOf(0)
        }


        LaunchedEffect(key1 = autoScrollSpeed) {
            if (autoScrollSpeed != 0f) {
                println("Scrolling")
                while (isActive) {
                    lazyListState.scrollBy(autoScrollSpeed)
                    delay(10)
                }
            }
        }

        DraggingList(
            texts = texts,
            state = lazyListState,
            onLongPress = {
                isDragIndicatorVisible = true
                dragIndicatorY = it.y
            },
            onDrag = {
                dragIndicatorY += it
            },
            onDragStop = {
                isDragIndicatorVisible = false
                onNavigate(currentHoveredIndex.toString())
            },
            onItemHover = { text, index ->
                currentHoveredIndex = index ?: 0
                currentDraggedName = text.orEmpty()
            },
            setAutoScrollSpeed = {
                autoScrollSpeed = it
            },
            autoScrollThreshold = with(LocalDensity.current) { 100.dp.toPx() },
        )

        if (isDragIndicatorVisible) {
            DragIndicator(
                modifier = Modifier.align(Alignment.TopCenter),
                yPos = dragIndicatorY
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(modifier = Modifier.padding(12.dp), text = currentDraggedName)
                }
            }
        }
    }
}

@Composable
fun DraggingList(
    modifier: Modifier = Modifier,
    state: LazyListState,
    texts: List<String>,
    onItemHover: (String?, Int?) -> Unit,
    onLongPress: (Offset) -> Unit,
    onDrag: (Float) -> Unit,
    onDragStop: () -> Unit,
    itemSpacing: Dp = 12.dp,
    setAutoScrollSpeed: (Float) -> Unit = { },
    autoScrollThreshold: Float
) {

    LazyColumn(
        modifier = modifier
            .padding(16.dp)
            .pointerInput(autoScrollThreshold, setAutoScrollSpeed) {

                fun itemAtOffset(hitPoint: Offset) =
                    state.layoutInfo.visibleItemsInfo.find { itemInfo ->
                        hitPoint.y.toInt() in (itemInfo.offset..itemInfo.offset + itemInfo.size + itemSpacing.roundToPx())
                    }?.index


                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        onItemHover(
                            itemAtOffset(it)?.let { index -> texts.getOrNull(index) },
                            itemAtOffset(it)
                        )
                        onLongPress(it)
                    },
                    onDrag = { change, drag ->
                        onDrag(drag.y)
                        onItemHover(
                            itemAtOffset(change.position)?.let { index ->
                                texts.getOrNull(
                                    index
                                )
                            }, itemAtOffset(change.position)
                        )

                        val distFromBottom =
                            state.layoutInfo.viewportSize.height - change.position.y
                        val distFromTop = change.position.y

                        setAutoScrollSpeed(
                            when {
                                distFromBottom < autoScrollThreshold -> autoScrollThreshold - distFromBottom
                                distFromTop < autoScrollThreshold -> -(autoScrollThreshold - distFromTop)
                                else -> 0f
                            }
                        )
                    },
                    onDragCancel = {
                        setAutoScrollSpeed(0f)
                    },
                    onDragEnd = {
                        setAutoScrollSpeed(0f)
                        onDragStop()
                    }
                )
            },
        verticalArrangement = Arrangement.spacedBy(itemSpacing),
        state = state
    ) {
        items(texts) { text ->
            Card(
                modifier = modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(horizontal = 16.dp, vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = text, fontSize = 24.sp)
                }
            }
        }
    }
}

@Composable
fun DragIndicator(modifier: Modifier, yPos: Float, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .wrapContentSize()
            .graphicsLayer { translationY = yPos }
    ) {
        content()
    }
}