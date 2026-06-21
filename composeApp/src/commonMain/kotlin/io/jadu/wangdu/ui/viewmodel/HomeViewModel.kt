package io.jadu.wangdu.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.jadu.wangdu.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val apiService: ApiService? = null
) : ViewModel() {

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = HomeUiState.Success(
                message = "Welcome to wangdu!",
                items = listOf(
                    "Compose Multiplatform",
                    "Koin DI",
                    "Navigation3",
                    "Ktor Networking",
                    "Ktor Server"
                )
            )
        }
    }

}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val message: String,
        val items: List<String>
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}