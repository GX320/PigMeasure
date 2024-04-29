package cn.edu.cidp.www.myapplication

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LifecycleCoroutineScope
import java.io.File

interface FileUtils {
    fun createDirectoryIfNotExist(context: Context)
    fun createFile(context: Context): File
    open fun openFile(context: Context, fileName: String?): Bitmap?
    fun delete(fileName: String)
//    fun update(fileName: String, lifecycleScope: LifecycleCoroutineScope)
    fun update(image: ByteArray, imageName: String, lifecycleScope: LifecycleCoroutineScope,callback: (String) -> Unit)
    fun readImageAsByteArray(fileName: String): ByteArray?
//    fun chunkUpdate(fileName: String, lifecycleScope: LifecycleCoroutineScope)
}