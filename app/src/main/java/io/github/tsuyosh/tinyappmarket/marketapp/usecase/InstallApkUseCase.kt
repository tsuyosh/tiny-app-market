package io.github.tsuyosh.tinyappmarket.marketapp.usecase

import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import io.github.tsuyosh.tinyappmarket.BuildConfig
import io.github.tsuyosh.tinyappmarket.marketapp.model.MarketApplication
import io.github.tsuyosh.tinyappmarket.ui.activity.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class InstallApkUseCase(
    private val context: Context,
    private val packageManager: PackageManager = context.packageManager,
    private val packageInstaller: PackageInstaller = packageManager.packageInstaller,
    private val fileProviderAuthority: String = BuildConfig.FILE_PROVIDER_AUTHORITY
) {
    private val callback: SessionCallbackImpl = SessionCallbackImpl()

    init {
        packageInstaller.registerSessionCallback(callback)
    }

    fun unregisterCallback() {
        packageInstaller.unregisterSessionCallback(callback)
    }

    suspend fun execute(application: MarketApplication) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                installWithPackageInstaller(application)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                createInstallIntentWithContentUri(application)?.let(this::launchInstaller)
            }
            else -> {
                createInstallIntentWithFileUri(application)?.let(this::launchInstaller)
            }
        }
    }

    private suspend fun installWithPackageInstaller(application: MarketApplication) =
        withContext(Dispatchers.IO) {
            try {
                val sessionId = packageInstaller.createSession(
                    createSessionParams(application.apkFile.length())
                )
                Log.d(TAG, "sessionId=$sessionId")

                val apkFile = application.apkFile
                packageInstaller.openSession(sessionId).use { session ->
                    try {
                        session.openWrite("apk", 0/*offset*/, apkFile.length())
                            .use { outputStream ->
                                apkFile.inputStream().use { inputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                                session.fsync(outputStream)
                            }
                        session.commit(createIntentSender(sessionId))
                        Log.d(TAG, "session committed")
                    } catch (e: RuntimeException) {
                        Log.e(TAG, "Failed to install apk", e)
                        session.abandon()
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Failed to create PackageInstaller session", e)
            } catch (e: SecurityException) {
                Log.e(TAG, "Failed to create PackageInstaller session", e)
            }
        }


    private fun createSessionParams(packageSize: Long): SessionParams =
        SessionParams(SessionParams.MODE_FULL_INSTALL).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setRequireUserAction(SessionParams.USER_ACTION_NOT_REQUIRED)
            }
            setSize(packageSize)
        }

    private fun createIntentSender(sessionId: Int): IntentSender {
        val broadcastIntent = Intent(MainActivity.PACKAGE_INSTALLED_ACTION)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            sessionId,
            broadcastIntent,
            flags
        )
        return pendingIntent.intentSender
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

    private class SessionCallbackImpl : PackageInstaller.SessionCallback() {
        override fun onCreated(sessionId: Int) {
            Log.d(TAG, "onCreated: sessionId=$sessionId")
        }

        override fun onBadgingChanged(sessionId: Int) {
            Log.d(TAG, "onBadgingChanged: sessionId=$sessionId")
        }

        override fun onActiveChanged(sessionId: Int, active: Boolean) {
            Log.d(TAG, "onActiveChanged: sessionId=$sessionId, active=$active")
        }

        override fun onProgressChanged(sessionId: Int, progress: Float) {
            Log.d(TAG, "onProgressChanged: sessionId=$sessionId, progress=$progress")
        }

        override fun onFinished(sessionId: Int, success: Boolean) {
            Log.d(TAG, "onFinished: sessionId=$sessionId, success=$success")
        }
    }

    companion object {
        private const val TAG = "InstallApkUseCase"
    }
}