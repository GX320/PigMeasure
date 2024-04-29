package cn.edu.cidp.www.myapplication.bean

import okhttp3.MultipartBody

data class MultipartFileParam(
    val chunkFlag: Boolean,
    val chunk: Int,
    val totalChunk: Int,
    val totalSize: Long,
    val name: String,
    val file: MultipartBody,
    val md5: String
    )