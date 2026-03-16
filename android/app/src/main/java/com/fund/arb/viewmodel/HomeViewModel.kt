package com.fund.arb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fund.arb.data.local.entity.FundDataEntity
import com.fund.arb.data.repository.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val funds: List<FundDataEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val minPremium: Double = 0.0,
    val onlyOpenPurchase: Boolean = false,
    val refreshingCode: String? = null,
    val lastUpdateTime: Long? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FundRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadFunds()
    }
    
    private fun loadFunds() {
        viewModelScope.launch {
            repository.getAllFunds().collect { funds ->
                val time = repository.getLastUpdateTime()
                _uiState.update { state ->
                    val filteredFunds = filterFunds(funds, state.minPremium, state.onlyOpenPurchase)
                    state.copy(
                        funds = filteredFunds,
                        lastUpdateTime = time
                    )
                }
            }
        }
    }
    
    private fun filterFunds(
        funds: List<FundDataEntity>,
        minPremium: Double,
        onlyOpenPurchase: Boolean
    ): List<FundDataEntity> {
        return funds.filter { fund ->
            (fund.premiumRate ?: Double.MIN_VALUE) >= minPremium &&
            (!onlyOpenPurchase || fund.purchaseStatus == "开放")
        }.sortedByDescending { it.premiumRate ?: Double.MIN_VALUE }
    }
    
    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = repository.refreshAllData()
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    error = if (result.isFailure) result.exceptionOrNull()?.message else null
                )
            }
        }
    }
    
    fun refreshFund(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(refreshingCode = code) }
            
            repository.refreshFund(code)
            
            _uiState.update { it.copy(refreshingCode = null) }
        }
    }
    
    fun setFilter(minPremium: Double, onlyOpenPurchase: Boolean) {
        viewModelScope.launch {
            val currentFunds = repository.getAllFundsSync()
            _uiState.update { state ->
                val filteredFunds = filterFunds(currentFunds, minPremium, onlyOpenPurchase)
                state.copy(
                    minPremium = minPremium, 
                    onlyOpenPurchase = onlyOpenPurchase,
                    funds = filteredFunds
                )
            }
        }
    }
}
