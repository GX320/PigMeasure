package cn.edu.cidp.www.myapplication.servlet

import cn.edu.cidp.www.myapplication.bean.*
import android.util.Log
import android.util.Size
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

val servletApi: ServletApi by lazy {

    val interceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
        Log.i("UserApi", it)
    }).setLevel(HttpLoggingInterceptor.Level.BODY)

    val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)   //拦截器
        .build()

    val retrofit = Retrofit.Builder()
//        .client(client)
//        .baseUrl("http://192.168.3.23:8001/")
        .baseUrl("http://60.212.188.152:9400/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    retrofit.create(ServletApi::class.java)
}

interface ServletApi{
//    @FormUrlEncoded
    @Multipart
    @POST("/api/pigTest/receiveImages")
    fun fileUpload(
        @Part chunkFlag: MultipartBody.Part,
        @Part totalSize: MultipartBody.Part,
        @Part name: MultipartBody.Part,
        @Part file: MultipartBody.Part,
        @Part md5: MultipartBody.Part
    ) : Call<ResultState>

    @Multipart
    @POST("/api/pigTest/receiveImages")
    fun imageUpload(
        @Part chunkFlag: MultipartBody.Part,
        @Part image: MultipartBody.Part,
        @Part md5: MultipartBody.Part
    ) : Call<ResultState>

//    @Multipart
//    @POST("/api/pigTest/receiveImages")
//    fun fileChunkUpload(
//        @Part chunkFlag: MultipartBody.Part,
//        @Part chunk: MultipartBody.Part,
//        @Part totalChunk: MultipartBody.Part,
//        @Part totalSize: MultipartBody.Part,
//        @Part name: MultipartBody.Part,
//        @Part file: MultipartBody.Part,
//        @Part md5: MultipartBody.Part
//    ): Call<ResultState>

}