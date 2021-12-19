package io.github.tsuyosh.tinyappmarket.marketapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.tsuyosh.tinyappmarket.file.ApkFileManager
import io.github.tsuyosh.tinyappmarket.marketapp.repository.MarketApplicationRepository
import io.github.tsuyosh.tinyappmarket.marketapp.usecase.InstallApkUseCase
import java.io.File

class MarketApplicationViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        MarketApplicationViewModel(
            MarketApplicationRepository(
                packageManager = context.packageManager,
                apkFileManager = ApkFileManager(
                    assetManager = context.assets,
                    internalStorageApkDir = File(context.filesDir, "apk")
                )
            ),
            InstallApkUseCase(context)
        ) as T
}