package com.fund.arb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fund.arb.data.local.entity.FundDataEntity
import com.fund.arb.data.repository.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QDIIUiState(
    val funds: List<FundDataEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class QDIIViewModel @Inject constructor(
    private val repository: FundRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(QDIIUiState())
    val uiState: StateFlow<QDIIUiState> = _uiState.asStateFlow()
    
    init {
        loadFunds()
    }
    
    private fun loadFunds() {
        viewModelScope.launch {
            repository.getFundsByType("QDII")
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { funds ->
                    _uiState.update { it.copy(funds = funds.sortedByDescending { item -> item.premiumRate ?: Double.MIN_VALUE }) }
                }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.refreshAllData()
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
