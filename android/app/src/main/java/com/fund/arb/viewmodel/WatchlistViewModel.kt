package com.fund.arb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fund.arb.data.local.entity.*
import com.fund.arb.data.repository.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WatchlistUiState(
    val watchlist: List<WatchlistEntity> = emptyList(),
    val funds: List<FundDataEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val repository: FundRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()
    
    init {
        loadWatchlist()
    }
    
    private fun loadWatchlist() {
        viewModelScope.launch {
            repository.getWatchlist()
                .collect { watchlist ->
                    _uiState.update { it.copy(watchlist = watchlist) }
                    
                    val fundCodes = watchlist.map { item -> item.code }
                    val funds = mutableListOf<FundDataEntity>()
                    for (code in fundCodes) {
                        repository.getFundByCode(code)?.let { funds.add(it) }
                    }
                    _uiState.update { it.copy(funds = funds.sortedByDescending { item -> item.premiumRate ?: Double.MIN_VALUE }) }
                }
        }
    }
    
    fun remove(code: String) {
        viewModelScope.launch {
            repository.removeFromWatchlist(code)
        }
    }
}
