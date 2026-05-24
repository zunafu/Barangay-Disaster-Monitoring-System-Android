package com.example.disastermanagement.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object FileUtil {
    fun createImageFile(context: Context): Uri {
        val imageFile = File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            context.externalCacheDir
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    }
}
