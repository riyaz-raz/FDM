package com.legozia.fdm.engine

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.TimeUnit

class ChunkDownloader(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) {

    suspend fun getRemoteFileInfo(url: String): Pair<Long, Boolean> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .head()
            .build()

        client.newCall(request).execute().use { response ->
            val contentLength = response.header("Content-Length")?.toLongOrNull() ?: -1L
            val acceptRanges = response.header("Accept-Ranges")?.contains("bytes", ignoreCase = true) == true
            contentLength to acceptRanges
        }
    }

    suspend fun downloadChunk(
        url: String,
        outputFile: File,
        startByte: Long,
        endByte: Long,
        onProgress: (bytesRead: Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        val requestBuilder = Request.Builder().url(url)
        if (startByte in 0..<endByte) {
            requestBuilder.addHeader("Range", "bytes=$startByte-$endByte")
        }

        client.newCall(requestBuilder.build()).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("Unexpected code $response")

            val body = response.body ?: throw IllegalStateException("Response body is null")
            val inputStream = body.byteStream()

            RandomAccessFile(outputFile, "rw").use { raf ->
                raf.seek(startByte)
                val buffer = ByteArray(8192)
                var read = 0
                while (coroutineContext.isActive && inputStream.read(buffer).also { read = it } != -1) {
                    raf.write(buffer, 0, read)
                    onProgress(read)
                }
            }
        }

        if (!coroutineContext.isActive) {
            throw CancellationException("Download chunk cancelled")
        }
    }
}
