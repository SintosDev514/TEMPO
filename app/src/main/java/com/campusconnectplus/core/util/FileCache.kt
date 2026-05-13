package com.campusconnectplus.core.util

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cacheDir = File(context.filesDir, "media_cache").apply { 
        if (!exists()) mkdirs() 
    }

    suspend fun cacheFile(uri: Uri): File = withContext(Dispatchers.IO) {
        val fileName = "${UUID.randomUUID()}_${uri.lastPathSegment ?: "file"}"
        val destFile = File(cacheDir, fileName)
        
        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw Exception("Could not open input stream for $uri")
        
        destFile
    }

    fun getFile(fileName: String): File? {
        val file = File(cacheDir, fileName)
        return if (file.exists()) file else null
    }

    fun deleteFile(fileName: String) {
        val file = File(cacheDir, fileName)
        if (file.exists()) file.delete()
    }
}
