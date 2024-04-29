package cn.edu.cidp.www.myapplication.controller

interface ICameraController {
    /**
     * 打开相机
     */
    fun openCameraPreView()

    /**
     * 拍照
     */
    fun takePhoto()

    /**
     * 录像
     */
    fun takeVideo()

    /**
     * 切换镜头
     */
    fun switchCamera()
}