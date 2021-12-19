package io.github.tsuyosh.tinyappmarket.marketapp.model

import java.io.File

data class MarketApplication(
    val packageId: String,
    val name: String,
    val apkFile: File
)

