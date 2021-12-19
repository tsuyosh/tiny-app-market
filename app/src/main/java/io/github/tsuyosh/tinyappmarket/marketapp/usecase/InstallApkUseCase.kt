package io.github.tsuyosh.tinyappmarket.marketapp.usecase

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import io.github.tsuyosh.tinyappmarket.BuildConfig
import io.github.tsuyosh.tinyappmarket.marketapp.model.MarketApplication

class InstallApkUseCase(
    private val context: Context,
    private val fileProviderAuthority: String = BuildConfig.FILE_PROVIDER_AUTHORITY
) {
    fun execute(application: MarketApplication) {
        val apkFile = application.apkFile
        val apkFileUri = FileProvider.getUriForFile(context, fileProviderAuthority, apkFile)
        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            data = apkFileUri
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }
}