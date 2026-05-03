package com.joysticks.stats.engine

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class AlignementRepository {

    suspend fun uploadCSV(csvData: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                
                val formBody = FormBody.Builder()
                    .add("csvData", csvData)
                    .build()

                val request = Request.Builder()
                    .url("http://lesjoysticks.info/AlignStats.asmx/UploadCSV")
                    .post(formBody)
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun downloadRoster(context: Context): File? {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()

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

                    file
                } else {
                    null
                }

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}