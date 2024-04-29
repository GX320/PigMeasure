package cn.edu.cidp.www.myapplication.controller

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture.withOutput
import androidx.camera.view.PreviewView
import androidx.compose.runtime.MutableState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import cn.edu.cidp.www.myapplication.CameraParams
import cn.edu.cidp.www.myapplication.FileUtils
import cn.edu.cidp.www.myapplication.FileUtilsImpl
import cn.edu.cidp.www.myapplication.getFileName
import com.google.common.util.concurrent.ListenableFuture

class CameraXController(
    private val previewView: PreviewView,
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val bitmap: MutableState<Bitmap?>
) : ICameraController {

    private val TAG = "CameraX"
    private var fileUtils: FileUtils? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    var mPreView: Preview? = null
    var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    private var cameraProvider: ProcessCameraProvider? = null
    var cameraParams: CameraParams? = null

    /**
     * 初始化相机配置
     */
    init {
        fileUtils = FileUtilsImpl()
        cameraParams = CameraParams.get(context)
        //Preview 预览流
        mPreView = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

    }

    /**
     * 打开相机预览
     */
    override fun openCameraPreView() {

        //图像捕捉
        imageCapture = ImageCapture.Builder().build()
        cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProvider = cameraProviderFuture!!.get()

        //视频帧捕捉
        val recorder = Recorder.Builder()
            .setQualitySelector(
                QualitySelector.from(
                    Quality.HD,
                    //设置分辨率
                    FallbackStrategy.higherQualityOrLowerThan(Quality.LOWEST)
                )
            )
            .build()
        videoCapture = withOutput(recorder)

        cameraProviderFuture?.addListener({
            //前后摄像头选择器
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(if (cameraParams?.mFacingFront == true) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK)
                .build()
            bindCameraId(cameraSelector, lifecycleOwner)
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * 拍照
     */
    override fun takePhoto() {
        fileUtils?.createDirectoryIfNotExist(context)
        val file = fileUtils?.createFile(context)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file!!).build()
        imageCapture!!.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // 需要回调
//                    val saveUri = Uri.fromFile(file)
//                    imageUri.value = saveUri
//                        Toast.makeText(context, saveUri.path, Toast.LENGTH_SHORT).show()
                    bitmap.value = FileUtilsImpl().openFile(context, getFileName(context))

                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("--onError--", exception.toString())
                }
            })
    }

    /**
     * 录像
     */
    @SuppressLint("CheckResult")
    override fun takeVideo() {
        val videoCapture = this.videoCapture ?: return

        //如果正在录像，则停止
        if (recording != null){
            recording?.stop()
            recording = null
            return
        }

        fileUtils?.createDirectoryIfNotExist(context)
//        val file = fileUtils?.createFile(context)
        val name = System.currentTimeMillis().toString()
        Log.d(TAG, "name: $name")
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE,"video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX")
        }
        val mediaStoreOutputOptions = context.contentResolver.let {
            MediaStoreOutputOptions
                .Builder(it, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues)
                .build()

        }
        recording = videoCapture.output
            .prepareRecording(context,mediaStoreOutputOptions)
            .apply {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED){
                    //启动音频
                    withAudioEnabled()
                    Log.d(TAG, "音频启动了")
                }
            }
            .start(ContextCompat.getMainExecutor(context)){ recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        //录制开始
                        Toast.makeText(context, "开始录制", Toast.LENGTH_SHORT)
                            .show()
                    }
                    is VideoRecordEvent.Finalize -> {
                        //录制结束
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded: " +
                                    "${recordEvent.outputResults.outputUri}"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT)
                                .show()
                            Log.d(TAG, msg)
                        } else {
                            recording?.close()
                            recording = null

                            Log.e(
                                TAG, "Video capture ends with error: " +
                                        "${recordEvent.cause?.message}"


                            )
                        }

                    }
                }
            }
    }

    /**
     * 切换摄像头
     */
    override fun switchCamera() {
        cameraParams?.mFacingFront = !cameraParams?.mFacingFront!!
        openCameraPreView()
    }

    /**
     * 绑定相机id
     */
    private fun bindCameraId(cameraSelector: CameraSelector, lifecycleOwner: LifecycleOwner?){
        try {
            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(
                lifecycleOwner!!,
                cameraSelector,
                mPreView,
                imageCapture,
                videoCapture
            )
        }catch (e: Exception){
            Log.e("Exception", e.toString())
        }
    }
}