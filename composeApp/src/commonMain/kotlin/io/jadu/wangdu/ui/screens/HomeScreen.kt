package io.jadu.wangdu.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import io.jadu.wangdu.model.ConnectionState
import io.jadu.wangdu.ui.viewmodel.WhiteBoardViewModel
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
    val lastReceivedMessage by viewModel.lastReceivedMessage.collectAsStateWithLifecycle()

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
                Button(
                    onClick = viewModel::sendPing,
                    enabled = connectionState is ConnectionState.Connected
                ) {
                    Text("Ping")
                }
            }

            Text(
                text = "Last echo: $lastReceivedMessage",
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
            )


            WhiteBoardCanvas(
                state = state,
                onDragStart = viewModel::onDragStart,
                onDrag = viewModel::onDrag,
                onDragEnd = viewModel::onDragEnd,
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
    modifier: Modifier = Modifier
) {
    val (dotColor, label) = when (connectionState) {
        is ConnectionState.Disconnected -> Color.Gray to "Disconnected"
        is ConnectionState.Connecting -> Color(0xFFFFC107) to "Connecting..."
        is ConnectionState.Connected -> Color(0xFF4CAF50) to "Connected"
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