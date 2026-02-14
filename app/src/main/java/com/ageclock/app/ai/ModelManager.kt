package com.ageclock.app.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object ModelManager {
    private const val MODEL_FILENAME = "qwen2-0_5b-instruct-q4_0.gguf"
    private const val MODEL_URL = "https://huggingface.co/Qwen/Qwen2-0.5B-Instruct-GGUF/resolve/main/qwen2-0_5b-instruct-q4_0.gguf"
    const val MODEL_SIZE_MB = 395

    private fun getModelDir(context: Context): File {
        return File(context.filesDir, "models").also { it.mkdirs() }
    }

    fun getModelFile(context: Context): File {
        return File(getModelDir(context), MODEL_FILENAME)
    }

    fun isModelDownloaded(context: Context): Boolean {
        val modelFile = getModelFile(context)
        return modelFile.exists() && modelFile.length() > 100_000_000 // At least 100MB
    }

    fun getDownloadedModelSizeMB(context: Context): Int {
        val modelFile = getModelFile(context)
        return if (modelFile.exists()) {
            (modelFile.length() / (1024 * 1024)).toInt()
        } else {
            0
        }
    }

    suspend fun downloadModel(
        context: Context,
        onProgress: (progress: Float, downloadedMB: Int, totalMB: Int) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val modelFile = getModelFile(context)
            val tempFile = File(getModelDir(context), "$MODEL_FILENAME.tmp")

            // Delete any existing temp file
            tempFile.delete()

            val url = URL(MODEL_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            connection.setRequestProperty("User-Agent", "TimeKeeper-Android/1.0")

            val totalBytes = connection.contentLength.toLong()
            val totalMB = (totalBytes / (1024 * 1024)).toInt()

            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytesRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        val progress = if (totalBytes > 0) {
                            totalBytesRead.toFloat() / totalBytes
                        } else {
                            0f
                        }
                        val downloadedMB = (totalBytesRead / (1024 * 1024)).toInt()

                        withContext(Dispatchers.Main) {
                            onProgress(progress, downloadedMB, totalMB)
                        }
                    }
                }
            }

            // Rename temp file to final file
            tempFile.renameTo(modelFile)

            Result.success(modelFile)
        } catch (e: Exception) {
            // Clean up temp file on error
            File(getModelDir(context), "$MODEL_FILENAME.tmp").delete()
            Result.failure(e)
        }
    }

    suspend fun deleteModel(context: Context): Boolean = withContext(Dispatchers.IO) {
        val modelFile = getModelFile(context)
        val tempFile = File(getModelDir(context), "$MODEL_FILENAME.tmp")
        tempFile.delete()
        modelFile.delete()
    }

    fun getAvailableStorageMB(context: Context): Long {
        val path = context.filesDir
        val stat = android.os.StatFs(path.absolutePath)
        return (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024)
    }

    fun hasEnoughStorage(context: Context): Boolean {
        return getAvailableStorageMB(context) > MODEL_SIZE_MB + 100 // Extra 100MB buffer
    }
}
