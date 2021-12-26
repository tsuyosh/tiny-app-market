package io.github.tsuyosh.tinyappmarket.marketapp.repository

import android.content.pm.PackageManager
import android.util.Log
import io.github.tsuyosh.tinyappmarket.file.ApkFileManager
import io.github.tsuyosh.tinyappmarket.marketapp.model.MarketApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

class MarketApplicationRepository(
    private val packageManager: PackageManager,
    private val apkFileManager: ApkFileManager
) {
    val allApplications: Flow<List<MarketApplication>> = flow {
        emit(queryAll())
    }

    private suspend fun queryAll(): List<MarketApplication> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Start queryAll")
        val apkFiles = apkFileManager.listApkFiles()
        Log.d(TAG, "apkFiles = $apkFiles")

        apkFiles.mapNotNull(this@MarketApplicationRepository::toMarketApplication)
    }

    private fun toMarketApplication(apkFile: File): MarketApplication? {
        Log.d(TAG, "apkFile = $apkFile")
        val packageInfo = packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
        Log.d(TAG, "packageInfo=$packageInfo")
        if (packageInfo == null) {
            return null
        }
        val name = packageInfo.applicationInfo.loadLabel(packageManager)
        val icon = packageInfo.applicationInfo.loadIcon(packageManager)
        return MarketApplication(
            packageId = packageInfo.applicationInfo.packageName,
            name = name.toString(),
            apkFile = apkFile,
            apkFileByteSize = apkFile.length(),
            iconDrawable = icon
        )
    }

    companion object {
        private const val TAG = "MarketApplicationRepository"
    }
}