package cn.edu.cidp.www.myapplication

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.extensions.ExtensionMode
import cn.edu.cidp.www.myapplication.inner.CameraRatioType

class CameraParams(context: Context?) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: CameraParams? = null


        fun get(context: Context?): CameraParams {
            if (instance == null) {
                synchronized(CameraParams::class.java) {
                    if (instance == null) {
                        instance = CameraParams(context)
                    }
                }
            }
            return instance!!
        }
    }

    var mFacingFront: Boolean = false //是否是前置摄像头
    var mRatioType: Int = CameraRatioType.RATIO_3_4//分辨率尺寸
    var mSplashOn: Boolean = false//闪光灯
    var torchSwitch: Boolean = false//补光
    var timer: Boolean = false//倒计时
    var grid: Boolean = false//网格线
    var hdr: Boolean = false//hdr
    var extensionMode: Int?= ExtensionMode.NONE

    init {
        extensionMode = ExtensionMode.NONE
        hdr = false
        grid = false
        timer = false
        torchSwitch = false
        mFacingFront = false
        mRatioType = CameraRatioType.RATIO_3_4
        mSplashOn = false
    }
}
