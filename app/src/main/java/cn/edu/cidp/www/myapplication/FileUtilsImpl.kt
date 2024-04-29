package cn.edu.cidp.www.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.Options
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import cn.edu.cidp.www.myapplication.bean.ResultState
import cn.edu.cidp.www.myapplication.servlet.servletApi
import cn.hutool.core.io.FileUtil
import cn.hutool.crypto.digest.DigestUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.util.*
import kotlin.math.ceil

class FileUtilsImpl: FileUtils {
    companion object{
        private const val IMAGE_PREFIX = "Image_"
        private const val JPG_SUFFIX = ".jpg"
        private const val FOLDER_NAME = "Photo"
    }

    fun getPhotoPath(context: Context) : String{
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath + File.separator + FOLDER_NAME
    }

    override fun createDirectoryIfNotExist(context: Context) {
        val folder = File(
            getPhotoPath(context)
        )
        if (!folder.exists()){
            folder.mkdirs()
        }
    }

    override fun createFile(context: Context) = File(
        getPhotoPath(context) + File.separator + IMAGE_PREFIX + System.currentTimeMillis() + JPG_SUFFIX
    )

    override fun openFile(context: Context, fileName: String?): Bitmap? {
        val options = BitmapFactory.Options()
        //设置inJustDecodeBounds为true表示只获取大小，不生成Bitmap
        options.inJustDecodeBounds = true
        //解析图片大小
        val path = getPhotoPath(context)
        if (fileName == null){
            return null
        }
//        val fileName = path + File.separator + ergodicFiles(path).last()
        Log.d("openFile", "openFile: $fileName")
        val file = File(fileName)
        var stream = file.inputStream()
        BitmapFactory.decodeStream(stream, null, options)
        stream.close()
        var width = options.outWidth
        var height = options.outHeight
        val ratio = 0
        Log.d("openFile", "width:$width,height:$height")
        //如果宽度大于高度，交换宽度和高度
        if (width > height){
            val temp = width
            width = height
            height = temp
        }
        Log.d("openFile", "width:$width,height:$height")
        //计算取样比例
        val sampleRatio = (width / 900).coerceAtLeast(height / 1600)
        //定义图片解码选项
        val options1 = Options()
        options1.inSampleSize = sampleRatio


        //读取图片，并将图片缩放到指定的目标大小
        stream = file.inputStream()
        val bitmap = BitmapFactory.decodeStream(stream, null, options1)
        stream.close()
        return bitmap
    }

    override fun delete(fileName: String) {
        File(fileName).delete()
    }

//    override fun update(fileName: String, lifecycleScope: LifecycleCoroutineScope) {
//        lifecycleScope.launch(Dispatchers.Main){
//            val file = File(fileName)
//            val totalSize = FileUtil.size(file)
//            val name = file.name
//            Log.d("networkTest", "name:$name")
//            val md5 = DigestUtil.md5Hex(file).uppercase(Locale.getDefault())
//            val requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file)
//            val multipartBody = MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("chunkFlag","false")
//                .addFormDataPart("totalSize", totalSize.toString())
//                .addFormDataPart("name", name)
//                .addFormDataPart("file",fileName ,requestBody)
//                .addFormDataPart("md5",md5)
//                .build()
////            builder.addFormDataPart("file", file.name, requestBody)
//            val result = withContext(Dispatchers.IO){
//                try {
//                    servletApi.fileUpload(
//                        chunkFlag = multipartBody.part(0),
//                        totalSize = multipartBody.part(1),
//                        name = multipartBody.part(2),
//                        file = multipartBody.part(3),
//                        md5 = multipartBody.part(4)
//                    ).execute().body()
//                } catch (e: Exception){
//                    Log.d("networkTest", "出错了：$e")
//                    null
//                }
//            }
//
//            // 处理响应数据
//            result?.let { responseBody ->
//                val resultState = responseBody as? ResultState
//                if (resultState != null) {
//                    // 成功将响应体转换为 ResultState 类型
//                    // 在这里处理 resultState 对象
//                } else {
//                    Log.d("networkTest", "响应体不是 ResultState 类型")
//                }
//            }
//        }
//    }

    override fun update(image: ByteArray, imageName: String, lifecycleScope: LifecycleCoroutineScope, callback: (String) -> Unit) {
        lifecycleScope.launch(Dispatchers.Main) {
            val md5 = DigestUtil.md5Hex(image).uppercase(Locale.getDefault())

            val requestBody = RequestBody.create(MediaType.parse("image/jpeg"), image)

            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("chunkFlag","false")
                .addFormDataPart("image", imageName, requestBody) // 使用图片原始名称
                .addFormDataPart("md5", md5)
                .build()

            val result = withContext(Dispatchers.IO) {
                try {
                    servletApi.imageUpload(
                        chunkFlag = multipartBody.part(0),
                        image = multipartBody.part(1),
                        md5 = multipartBody.part(2)
                    ).execute().body()
                } catch (e: Exception) {
                    Log.d("networkTest", "出错了：$e")
                    null
                }
            }

            // 处理响应数据
            result?.let { responseBody ->
                val resultState = responseBody as? ResultState
                if (resultState != null) {
                    // 成功将响应体转换为 ResultState 类型
                    // 在这里处理 resultState 对象
                    val message = resultState.message
                    val pigLength = 5 * (message.toFloat())
                    callback("上传成功，猪身长为： $pigLength 米") // 使用安全调用运算符和 Elvis 操作符确保安全地获取 message 的长度，并计算猪身长
                } else {
                    Log.d("networkTest", "响应体不是 ResultState 类型")
                    callback("上传失败，请重试！") // 上传失败，调用回调函数并传递失败消息
                }
            }
        }
    }



    override fun readImageAsByteArray(fileName: String): ByteArray? {
        val file = File(fileName)
        if (!file.exists()) {
            return null
        }

        val inputStream = FileInputStream(file)
        try {
            val byteArray = inputStream.readBytes()
            return byteArray
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            inputStream.close()
        }
    }

//    override fun chunkUpdate(fileName: String, lifecycleScope: LifecycleCoroutineScope) {
//        lifecycleScope.launch(Dispatchers.Main){
//            val TENMB = 1048576
//            val file = File(fileName)
//            val name = file.name
//            val fileInputStream = FileInputStream(file)
//            val accessFile = RandomAccessFile(file,"r")
//            val mD5 = DigestUtil.md5Hex(fileInputStream).uppercase(Locale.getDefault())
//            //文件的大小
//            val totalSize = FileUtil.size(file);
//            val chunkSize = ceil(totalSize.toDouble() / TENMB).toInt()   //计算分成几片
//            for (i in 0..chunkSize){
//                Log.d("networkTest", "开始的分片 = ${i + 1}")
//                //文件操作的指针位置
//                val filePoint = accessFile.filePointer
//                var bytes: ByteArray
//                if (i == chunkSize - 1){
//                    val len = (totalSize - filePoint).toInt()
//                    bytes = ByteArray(len)
//                    accessFile.read(bytes, 0,bytes.size)
//                }else{
//                    bytes = ByteArray(TENMB)
//                    accessFile.read(bytes, 0,bytes.size)
//                }
//                val requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), bytes)
//                val multipartBody = MultipartBody.Builder()
//                    .setType(MultipartBody.FORM)
//                    .addFormDataPart("chunkFlag","true")    //是否分片
//                    .addFormDataPart("chunk", (i + 1).toString())   //当前第几片
//                    .addFormDataPart("totalChunk", chunkSize.toString())    //总分片数量
//                    .addFormDataPart("totalSize", totalSize.toString()) //文件总大小(byte)
//                    .addFormDataPart("name", name)  //文件名
//                    .addFormDataPart("file",fileName ,requestBody)  //文件
//                    .addFormDataPart("md5",mD5)
//                    .build()
//                val result = withContext(Dispatchers.IO){
//                    try {
//                        servletApi.fileChunkUpload(
//                            chunkFlag = multipartBody.part(0),
//                            chunk = multipartBody.part(1),
//                            totalChunk = multipartBody.part(2),
//                            totalSize = multipartBody.part(3),
//                            name = multipartBody.part(4),
//                            file = multipartBody.part(5),
//                            md5 = multipartBody.part(6)
//                        ).execute().body() as cn.edu.cidp.www.myapplication.bean.Result
//                    }catch (e: Exception){
//                        Log.d("networkTest", "出错了：$e")
//                    }
//                }
//                Log.d("networkTest", "result = $result")
//                Log.d("networkTest", "结束的分片 = ${i + 1}")
//            }
//        }
//
//    }

    //遍历文件夹
    fun ergodicFiles(path : String) : List<String>{
        val fileNames: MutableList<String> = mutableListOf()
        var fileTree: FileTreeWalk = File(path).walk()
        fileTree.maxDepth(1)    //遍历目录层级为1，即无需检查子目录
            .filter { it.isFile }   //只挑选出文件，不处理文件夹
            .filter { it.extension == "jpg" || it.extension == "mp4"}
            .forEach {
                fileNames.add(it.name)
            }
        Log.d("ergodicFiles", "ergodicFiles: ")
        fileNames.reverse()
        return fileNames
    }

}