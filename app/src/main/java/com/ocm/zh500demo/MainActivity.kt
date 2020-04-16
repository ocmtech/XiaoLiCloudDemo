package com.ocm.zh500demo

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Rect
import android.hardware.Camera
import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.guo.android_extend.widget.CameraFrameData
import com.guo.android_extend.widget.CameraSurfaceView
import com.ocm.zh500demo.utils.GPIOHelper
import com.ocm.zh500demo.utils.NFCHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NFCHelper.NFCListener {

    //NFC
    private var mAdapter: NfcAdapter? = null
    private var mPendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupCamera()

        //NFC
        mAdapter = NfcAdapter.getDefaultAdapter(this)
        mPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        )
        NFCHelper.openZHRFID()
        NFCHelper.listener = this
    }

    override fun onResume() {
        super.onResume()
        mAdapter?.enableForegroundDispatch(this, mPendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        mAdapter?.disableForegroundDispatch(this)
    }

    override fun onDestroy() {
        GPIOHelper.irLightOff()
        super.onDestroy()
    }

    private fun setupCamera() {
        setupIRCamera()
        setupRGBCamera()
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        NFCHelper.decode(intent)
    }


    private fun setupIRCamera() {
        GPIOHelper.irLightOn()
        irSurfaceView.debug_print_fps(false, false)
        irSurfaceView.setupGLSurafceView(irGlsurfaceView, true, 0, 90)
        irSurfaceView.setOnCameraListener(object : CameraSurfaceView.OnCameraListener{
            override fun setupChanged(format: Int, width: Int, height: Int) {
            }

            override fun onBeforeRender(data: CameraFrameData?) {
            }

            override fun startPreviewImmediately(): Boolean {
                return true
            }

            override fun onPreview(data: ByteArray?, width: Int, height: Int, format: Int, timestamp: Long): Any {
                return arrayOfNulls<Rect>(0)
            }

            override fun setupCamera(): Camera? {
                try {
                    val mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
                    return mCamera
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }

            override fun onAfterRender(data: CameraFrameData) {
            }
        })
    }

    private fun setupRGBCamera() {
        rgbSurfaceView.setupGLSurafceView(rgbGlsurfaceView, true, 0, 90)
        rgbSurfaceView.debug_print_fps(false, false)
        rgbSurfaceView.setOnCameraListener(object : CameraSurfaceView.OnCameraListener{

            override fun setupChanged(format: Int, width: Int, height: Int) {
            }

            override fun onBeforeRender(data: CameraFrameData?) {
            }

            override fun startPreviewImmediately(): Boolean {
                return true
            }

            override fun onPreview(data: ByteArray?, width: Int, height: Int, format: Int, timestamp: Long): Any {
                return arrayOfNulls<Rect>(0)
            }

            override fun setupCamera(): Camera? {
                try {
                    val mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
                    return mCamera
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }

            override fun onAfterRender(data: CameraFrameData) {
            }
        })
    }

    override fun onNFCReadSuccess(cardNo10D: String, blockContent: String?) {
        tvNFCResult.text = "卡号：${cardNo10D}\n扇区块内容：${blockContent}"
    }
}
