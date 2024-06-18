
package com.pedro.streamer.rotation

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import android.util.Range
import android.util.Size
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import com.pedro.encoder.input.video.Camera2ApiManager
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.encoder.input.video.facedetector.FaceDetectorCallback
import com.pedro.library.util.sources.video.VideoSource

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class   AAOSCameraSource(context: Context): VideoSource() {

    public  class Camera2Helper(context: Context) {
        private var mCameraManager: CameraManager? = null
        init{
            mCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        }
        public fun getCameraIdList(): Array<String>? {
            try {
                // String[] to Array<String>
                return mCameraManager?.getCameraIdList()?.toList()?.toTypedArray()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }

    private val camera = AAOSCameraApiManager(context)
    private val cameraHelper : Camera2Helper  = Camera2Helper(context)

    private  var mId : String   = ""

    override fun create(width: Int, height: Int, fps: Int, rotation: Int): Boolean {
        val result = checkResolutionSupported(width, height)
        if (!result) {
            throw IllegalArgumentException("Unsupported resolution: ${width}x$height")
        }
        return true
    }

    override fun start(surfaceTexture: SurfaceTexture) {
        this.surfaceTexture = surfaceTexture
        if (!isRunning()) {
            surfaceTexture.setDefaultBufferSize(width, height)
            camera.prepareCamera(surfaceTexture, width, height, fps)
            Log.i("AAOSCameraSource", "Camera id: $mId")
            camera.openCameraId(mId)
        }
    }

    override fun stop() {
        if (isRunning()) camera.closeCamera()
    }

    override fun release() {}

    override fun isRunning(): Boolean = camera.isRunning


    private fun mapCamera1Resolutions(resolutions: List<Camera.Size>, shouldRotate: Boolean) = resolutions.map {
        if (shouldRotate) Size(it.height, it.width) else Size(it.width, it.height)
    }
    private fun checkResolutionSupported(width: Int, height: Int): Boolean {
        if (width % 2 != 0 || height % 2 != 0) {
            throw IllegalArgumentException("width and height values must be divisible by 2")
        }


        val ids = cameraHelper.getCameraIdList()
        val id = ids?.get(0)
        mId = id?:""

        val size = Size(width, height)
        val resolutions = camera.getCameraResolutions(mId)

        return if (camera.levelSupported == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
              //this is a wrapper of camera1 api. Only listed resolutions are supported
            resolutions?.contains(size) ?: false
        } else {
            val widthList = resolutions.map { size.width }
            val heightList = resolutions.map { size.height }
            val maxWidth = widthList.maxOrNull() ?: 0
            val maxHeight = heightList.maxOrNull() ?: 0
            val minWidth = widthList.minOrNull() ?: 0
            val minHeight = heightList.minOrNull() ?: 0
            size.width in minWidth..maxWidth && size.height in minHeight..maxHeight
        }
    }

    fun switchCamera() {
        if (isRunning()) {
            stop()
            surfaceTexture?.let {
                start(it)
            }
        }
    }

    fun setExposure(level: Int) {
        if (isRunning()) camera.exposure = level
    }

    fun getExposure(): Int {
        return if (isRunning()) camera.exposure else 0
    }

    fun enableLantern() {
        if (isRunning()) camera.enableLantern()
    }

    fun disableLantern() {
        if (isRunning()) camera.disableLantern()
    }

    fun isLanternEnabled(): Boolean {
        return if (isRunning()) camera.isLanternEnabled else false
    }

    fun enableAutoFocus() {
        if (isRunning()) {
            camera.enableAutoFocus()
        }
    }

    fun disableAutoFocus() {
        if (isRunning()) camera.disableAutoFocus()
    }

    fun isAutoFocusEnabled(): Boolean {
        return if (isRunning()) camera.isAutoFocusEnabled else false
    }

    fun tapToFocus(event: MotionEvent) {
        camera.tapToFocus(event)
    }

    fun setZoom(event: MotionEvent) {
        if (isRunning()) camera.setZoom(event)
    }

    fun setZoom(level: Float) {
        if (isRunning()) camera.zoom = level
    }

    fun getZoomRange(): Range<Float> = camera.zoomRange

    fun getZoom(): Float = camera.zoom

    fun enableFaceDetection(callback: FaceDetectorCallback): Boolean {
        return if (isRunning()) camera.enableFaceDetection(callback) else false
    }

    fun disableFaceDetection() {
        if (isRunning()) camera.disableFaceDetection()
    }

    fun isFaceDetectionEnabled() = camera.isFaceDetectionEnabled

    fun camerasAvailable(): Array<String>? = camera.camerasAvailable

    fun openCameraId(id: String) {
        if (isRunning()) stop()
        camera.openCameraId(id)
    }

    fun enableOpticalVideoStabilization(): Boolean {
        return if (isRunning()) camera.enableOpticalVideoStabilization() else false
    }

    fun disableOpticalVideoStabilization() {
        if (isRunning()) camera.disableOpticalVideoStabilization()
    }

    fun isOpticalVideoStabilizationEnabled() = camera.isOpticalStabilizationEnabled

    fun enableVideoStabilization(): Boolean {
        return if (isRunning()) camera.enableVideoStabilization() else false
    }

    fun disableVideoStabilization() {
        if (isRunning()) camera.disableVideoStabilization()
    }

    fun isVideoStabilizationEnabled() = camera.isVideoStabilizationEnabled
}