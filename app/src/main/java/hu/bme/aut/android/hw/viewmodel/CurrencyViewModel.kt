package hu.bme.aut.android.hw.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.hw.data.local.repository.SettingsRepository
import hu.bme.aut.android.hw.data.repository.ExchangeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val repo: SettingsRepository
): ViewModel() {

    val lastFetchTime: StateFlow<Long?> = repo.lastFetchTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    val defaultCurrency: StateFlow<String> = repo.defaultCurrency.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "EUR"
    )

    fun setDefaultCurrency(code: String) {
        viewModelScope.launch {
            repo.setDefaultCurrency(code)
        }
    }

    private val _rates = MutableStateFlow<Map<String, Double>>(emptyMap())
    val rates: StateFlow<Map<String, Double>> = _rates


    init {
        viewModelScope.launch {
            _rates.value = repo.getRates()
        }
    }
}
