package com.fund.arb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fund.arb.data.local.entity.*
import com.fund.arb.data.repository.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val fund: FundDataEntity? = null,
    val premiumHistory: List<PremiumHistoryEntity> = emptyList(),
    val navHistory: List<NavHistoryEntity> = emptyList(),
    val isWatched: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: FundRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()
    
    fun loadFund(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val fund = repository.getFundByCode(code)
            val isWatched = repository.isInWatchlist(code)
            
            _uiState.update { 
                it.copy(
                    fund = fund,
                    isWatched = isWatched,
                    isLoading = false
                )
            }
            
            repository.getPremiumHistory(code)
                .catch { e -> _uiState.update { state -> state.copy(error = e.message) } }
                .collect { history ->
                    _uiState.update { it.copy(premiumHistory = history) }
                }
        }
    }
    
    fun toggleWatchlist() {
        viewModelScope.launch {
            val fund = _uiState.value.fund ?: return@launch
            val code = fund.code
            
            if (_uiState.value.isWatched) {
                repository.removeFromWatchlist(code)
                _uiState.update { it.copy(isWatched = false) }
            } else {
                repository.addToWatchlist(code, fund.name)
                _uiState.update { it.copy(isWatched = true) }
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            val code = _uiState.value.fund?.code ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            
            repository.refreshFund(code)
            
            val fund = repository.getFundByCode(code)
            _uiState.update { it.copy(fund = fund, isLoading = false) }
        }
    }
}
