package com.rymin.autotouch

import androidx.lifecycle.ViewModel
import com.rymin.autotouch.service.AutoTouchService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AutoTouchUiState(
    val targetText: String = "",
    val isSearching: Boolean = false,
    val lastFoundTime: Long = 0
)

class AutoTouchViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AutoTouchUiState())
    val uiState: StateFlow<AutoTouchUiState> = _uiState.asStateFlow()

    fun updateTargetText(text: String) {
        _uiState.value = _uiState.value.copy(targetText = text)
    }

    fun startSearching() {
        _uiState.value = _uiState.value.copy(isSearching = true)
        AutoTouchService.getInstance()?.let { service ->
            service.setTargetText(_uiState.value.targetText)
            service.startSearching()
        }
    }

    fun stopSearching() {
        _uiState.value = _uiState.value.copy(isSearching = false)
        AutoTouchService.getInstance()?.stopSearching()
    }

    fun updateSearchingState(isSearching: Boolean) {
        _uiState.value = _uiState.value.copy(isSearching = isSearching)
    }

    fun updateLastFoundTime() {
        _uiState.value = _uiState.value.copy(lastFoundTime = System.currentTimeMillis())
    }
}