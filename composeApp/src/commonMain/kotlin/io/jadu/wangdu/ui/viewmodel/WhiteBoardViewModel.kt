package io.jadu.wangdu.ui.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.jadu.wangdu.domain.model.DrawPath
import io.jadu.wangdu.domain.model.WhiteBoardState
import io.jadu.wangdu.domain.repository.WhiteBoardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class WhiteBoardViewModel(
    private val repository: WhiteBoardRepository
) : ViewModel() {
    private val _state = MutableStateFlow(WhiteBoardState())
    val state: StateFlow<WhiteBoardState> = _state.asStateFlow()

    @OptIn(ExperimentalUuidApi::class)
    private val userId : String = Uuid.random().toString()

    val connectionState = repository.connectionState
    val lastReceivedMessage = repository.lastReceivedMessage

    init {
        viewModelScope.launch {
            repository.incomingStrokes.collect { path ->
                _state.update { it.copy(paths = it.paths + path) }
            }
        }
    }

    fun connect(host: String, port: Int){
       viewModelScope.launch { repository.connect(host, port) }
    }

    fun sendPing(){
        viewModelScope.launch { repository.sendPing() }
    }

    fun onDragStart(offset: Offset) {
        _state.update { currentState ->
            currentState.copy(
                currentPath = DrawPath(
                    points = listOf(offset),
                    color = DefaultStrokeColor,
                    strokeWidth = DEFAULTSTROKEWIDTH
                )
            )
        }
    }

    fun onDrag(offest: Offset) {
        _state.update {currentState ->
            val activePath = currentState.currentPath ?: return@update currentState
            currentState.copy(
                currentPath = activePath.copy(
                    points = activePath.points + offest
                )
            )
        }
    }

    fun onDragEnd() {
        val activePath = _state.value.currentPath ?: return
        _state.update { it.copy(currentPath = null) }
        if(activePath.points.isEmpty()) return
        viewModelScope.launch { repository.send(activePath,userId) }
    }

    fun clearBoard() {
        _state.update { WhiteBoardState() }
    }

    private companion object {
        val DefaultStrokeColor = Color.Black
        const val DEFAULTSTROKEWIDTH = 8f
    }
}


