package io.jadu.wangdu.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.jadu.wangdu.domain.model.ConnectionState
import io.jadu.wangdu.ui.viewmodel.WhiteBoardViewModel
import io.jadu.wangdu.utils.colorFromUserId
import org.koin.compose.viewmodel.koinViewModel

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
                    .fillMaxSize()
                    .padding(paddingValues)
            )
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