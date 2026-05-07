package com.joysticks.stats.engine

import android.content.Context
import com.joysticks.stats.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

object AlignementRepository {

    private val client = OkHttpClient()

    suspend fun uploadCSV(context: Context, csvData: String): Result<Boolean> {
        if (!NetworkUtils.isTargetWifiConnected(context)) {
            return Result.failure(Exception("Veuillez vous connecter au WiFi 'mmetara'"))
        }

        return withContext(Dispatchers.IO) {
            try {
                val formBody = FormBody.Builder()
                    .add("csvData", csvData)
                    .build()

                val request = Request.Builder()
                    .url("http://lesjoysticks.info/AlignStats.asmx/UploadCSV")
                    .post(formBody)
                    .build()

                val response = client.newCall(request).execute()
                Result.success(response.isSuccessful)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun downloadRoster(context: Context): Result<File> {
        if (!NetworkUtils.isTargetWifiConnected(context)) {
            return Result.failure(Exception("Veuillez vous connecter au WiFi 'mmetara'"))
        }

        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("http://lesjoysticks.info/db/alignements/out/alignement.csv")
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val file = File(context.cacheDir, "alignement.csv")
                    response.body?.byteStream()?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    Result.success(file)
                } else {
                    Result.failure(Exception("Erreur serveur: ${response.code}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
