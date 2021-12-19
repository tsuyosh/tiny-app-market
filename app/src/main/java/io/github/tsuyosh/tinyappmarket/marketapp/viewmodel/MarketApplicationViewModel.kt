package io.github.tsuyosh.tinyappmarket.marketapp.viewmodel

import androidx.lifecycle.ViewModel
import io.github.tsuyosh.tinyappmarket.marketapp.model.MarketApplication
import io.github.tsuyosh.tinyappmarket.marketapp.repository.MarketApplicationRepository
import io.github.tsuyosh.tinyappmarket.marketapp.usecase.InstallApkUseCase
import kotlinx.coroutines.flow.Flow

class MarketApplicationViewModel(
    private val repository: MarketApplicationRepository,
    private val installApkUseCase: InstallApkUseCase
) : ViewModel() {
    val allApplications: Flow<List<MarketApplication>> = repository.allApplications

    fun install(application: MarketApplication) {
        installApkUseCase.execute(application)
    }
}