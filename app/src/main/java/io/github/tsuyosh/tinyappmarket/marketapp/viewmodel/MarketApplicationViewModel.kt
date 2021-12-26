package io.github.tsuyosh.tinyappmarket.marketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.tsuyosh.tinyappmarket.marketapp.model.MarketApplication
import io.github.tsuyosh.tinyappmarket.marketapp.repository.MarketApplicationRepository
import io.github.tsuyosh.tinyappmarket.marketapp.usecase.InstallApkUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MarketApplicationViewModel(
    private val repository: MarketApplicationRepository,
    private val installApkUseCase: InstallApkUseCase
) : ViewModel() {
    val allApplications: Flow<List<MarketApplication>> = repository.allApplications

    fun install(application: MarketApplication) {
        viewModelScope.launch {
            installApkUseCase.execute(application)
        }
    }

    override fun onCleared() {
        super.onCleared()
        installApkUseCase.unregisterCallback()
    }
}