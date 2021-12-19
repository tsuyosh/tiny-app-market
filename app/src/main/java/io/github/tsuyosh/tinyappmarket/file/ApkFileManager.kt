package io.github.tsuyosh.tinyappmarket.file

import android.content.res.AssetManager
import android.util.Log
import java.io.File
import java.io.IOException

class ApkFileManager(
    private val assetManager: AssetManager,
    private val apkDirName: String = "apk/",
    private val internalStorageApkDir: File
) {
    fun listApkFiles(): List<File> {
        val apkFiles = assetManager.list(apkDirName) ?: return emptyList()
        Log.d(TAG, "apkFiles=$apkFiles")
        return apkFiles.mapNotNull(this::toInternalStorageFile)
    }

    private fun toInternalStorageFile(fileName: String): File? {
        val apkFile = File(internalStorageApkDir, fileName)
        if (apkFile.exists()) {
            Log.d(TAG, "Apk file already exists: $apkFile")
            return apkFile
        }

        return try {
            if (!internalStorageApkDir.exists()) {
                internalStorageApkDir.mkdirs()
            }
            assetManager.open(apkDirName + fileName).use { inputStream ->
                inputStream.copyTo(apkFile.outputStream())
            }
            apkFile
        } catch (exception: IOException) {
            Log.e(TAG, "Failed to copy apk file: $apkFile", exception)
            apkFile.delete()
            null
        }
    }

    companion object {
        private const val TAG = "ApkFileManager"
    }
}