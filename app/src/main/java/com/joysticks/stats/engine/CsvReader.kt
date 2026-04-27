package com.joysticks.stats.engine

import android.content.Context
import android.net.Uri

fun readCsvLines(context: Context, uri: Uri): List<String> {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: return emptyList()

    return inputStream.bufferedReader().readLines()
}
