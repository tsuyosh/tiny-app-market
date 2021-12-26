package io.github.tsuyosh.tinyappmarket.marketapp.model

import android.graphics.drawable.Drawable
import java.io.File

data class MarketApplication(
    val packageId: String,
    val name: String,
    val apkFile: File,
    val apkFileByteSize: Long,
    val iconDrawable: Drawable?
)

