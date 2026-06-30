package io.jadu.wangdu.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.jadu.wangdu.domain.model.ConnectionState
import io.jadu.wangdu.domain.model.DrawingTool
import io.jadu.wangdu.ui.theme.PenSwatchColors
import io.jadu.wangdu.ui.viewmodel.WhiteBoardViewModel
import io.jadu.wangdu.utils.colorFromUserId
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WhiteBoardViewModel = koinViewModel(),
    serverHost: String,
    serverPort: Int
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wangdu") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                actions = {
                    Button(
                        onClick = viewModel::clearBoard
                    ){
                        Text("boom boom")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ConnectionStatusRow(
                connectionState = connectionState,
                userCount = state.connectedUsers.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ConnectedUsersRow(
                connectedUsers = state.connectedUsers,
                selfId = viewModel.selfId,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.connect(serverHost, serverPort)
                    },
                    enabled = connectionState is ConnectionState.Disconnected ||
                            connectionState is ConnectionState.Error
                ) {
                    Text("Connect")
                }
            }

            WhiteBoardCanvas(
                state = state,
                onDragStart = viewModel::onDragStart,
                onDrag = viewModel::onDrag,
                onDragEnd = viewModel::onDragEnd,
                onPointerMove = viewModel::onPointerMove,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            DrawingToolBar(
                activeTool = state.activeTool,
                onSelectTool = viewModel::selectTool,
                modifier = Modifier.fillMaxWidth()
            )
        }


    }
}

@Composable
private fun DrawingToolBar(
    activeTool: DrawingTool,
    onSelectTool: (DrawingTool) -> Unit,
    modifier: Modifier = Modifier
) {
    var lastUserColor by remember { mutableStateOf(Color.Black) }
    var lastUsedWidth by remember { mutableStateOf(8f) }
    var eraserWidth by remember { mutableStateOf(20f) }

    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ToolToggle(
                activeTool = activeTool,
                onPickPen = {
                    onSelectTool(DrawingTool.Pen(width = lastUsedWidth, color = lastUserColor))
                },
                onPickEraser = {onSelectTool(DrawingTool.Eraser(eraserWidth))}
            )

            ColorSwatches(
                activeTool = activeTool,
                onPickColor = { color ->
                    lastUserColor = color
                    onSelectTool(DrawingTool.Pen(color = color, width = lastUsedWidth))
                }
            )

            WidthSlider(
                activeTool = activeTool,
                onWidthChange = {newWidth ->
                    when(val tool = activeTool){
                        is DrawingTool.Pen -> {
                            lastUsedWidth = newWidth
                            onSelectTool(tool.copy(width = newWidth))
                        }

                        is DrawingTool.Eraser -> {
                            eraserWidth = newWidth
                            onSelectTool(tool.copy(width = newWidth))
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun WidthSlider(
    activeTool: DrawingTool,
    onWidthChange: (Float) -> Unit
) {
    val currentWidth = activeTool.width
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Slider(
            value = currentWidth,
            onValueChange = onWidthChange,
            valueRange = 2f..24f,
            modifier = Modifier.weight(1f)
        )
        Text(text = currentWidth.roundToInt().toString())
    }
}

@Composable
private fun ColorSwatches(
    activeTool: DrawingTool,
    onPickColor: (Color) -> Unit
){
    val selectedColor = (activeTool as? DrawingTool.Pen)?.color
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PenSwatchColors.forEach { color->
            ColorSwatch(
                color = color,
                selected = color == selectedColor,
                onClick = { onPickColor(color) }
            )
        }
    }
}

@Composable
private fun ColorSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .border(width = 3.dp, color = borderColor, shape = CircleShape)
            .clickable(onClick = onClick),
    )
}

@Composable
private fun ToolToggle(
    activeTool: DrawingTool,
    onPickPen:() -> Unit,
    onPickEraser:() -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ToolButton(
            label = "Pen",
            selected = activeTool is DrawingTool.Pen,
            onClick = onPickPen
        )
        ToolButton(
            label = "Eraser",
            selected = activeTool is DrawingTool.Eraser,
            onClick = onPickEraser
        )
    }
}

@Composable
private fun ToolButton(
    label: String,
    selected: Boolean,
    onClick:() -> Unit
) {
    if(selected){
        Button(
            onClick = onClick
        ){
            Text(label)
        }
    } else {
        OutlinedButton(
            onClick = onClick
        ){
            Text(label)
        }
    }
}

@Composable
private fun ConnectionStatusRow (
    connectionState: ConnectionState,
    userCount: Int,
    modifier: Modifier = Modifier
) {
    val (dotColor, label) = when (connectionState) {
        is ConnectionState.Disconnected -> Color.Gray to "Disconnected"
        is ConnectionState.Connecting -> Color(0xFFFFC107) to "Connecting..."
        is ConnectionState.Connected -> Color(0xFF4CAF50) to "Connected · $userCount users"
        is ConnectionState.Error -> Color.Red to connectionState.message.take(40)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = dotColor)
        }
        Text(text = label)
    }
}

@Composable
private fun ConnectedUsersRow(
    connectedUsers: Map<String, String>,
    selfId : String,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(connectedUsers.entries.toList()){(userId, displayName) ->
            ConnectedUserItem(
                displayName = if(userId == selfId) "$displayName (you)" else displayName,
                color = colorFromUserId(userId)
            )
        }
    }
}

@Composable
private fun ConnectedUserItem(
    displayName: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(modifier = Modifier.size(10.dp)){
            drawCircle(color = color)
        }
        Text(text = displayName)
    }
}