package io.github.tsuyosh.tinyappmarket.marketapp.usecase

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import io.github.tsuyosh.tinyappmarket.BuildConfig
import io.github.tsuyosh.tinyappmarket.marketapp.model.MarketApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class InstallApkUseCase(
    private val context: Context,
    private val fileProviderAuthority: String = BuildConfig.FILE_PROVIDER_AUTHORITY
) {
    suspend fun execute(application: MarketApplication) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                installApkAboveQ(application)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                createInstallIntentWithContentUri(application)?.let(this::launchInstaller)
            }
            else -> {
                createInstallIntentWithFileUri(application)?.let(this::launchInstaller)
            }
        }
    }

    private fun installApkAboveS(application: MarketApplication) {
        // TODO
    }

    private fun installApkAboveQ(application: MarketApplication) {
        // TODO
    }

    private fun launchInstaller(intent: Intent) = try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Log.e(TAG, "Failed to launch apk installer", e)
    }

    private fun createInstallIntentWithContentUri(application: MarketApplication): Intent? {
        val apkFile = application.apkFile
        val apkFileUri = FileProvider.getUriForFile(context, fileProviderAuthority, apkFile)
        return Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            data = apkFileUri
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
    }

    private suspend fun createInstallIntentWithFileUri(application: MarketApplication): Intent? =
        withContext(Dispatchers.IO) {
            val externalApkFile =
                File(context.getExternalFilesDir(null), "apk").resolve(application.apkFile.name)
            if (!externalApkFile.exists()) {
                try {
                    application.apkFile.copyTo(externalApkFile)
                } catch (e: IOException) {
                    Log.w(TAG, "Failed to copy apk file", e)
                    return@withContext null
                }
            }

            Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                setDataAndType(
                    Uri.fromFile(externalApkFile),
                    "application/vnd.android.package-archive"
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

    companion object {
        private const val TAG = "InstallApkUseCase"
    }
}