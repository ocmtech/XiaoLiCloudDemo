package com.ocm.zh500demo

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.hardware.Camera
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
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
        val sp = getSharedPreferences(getString(R.string.app_name), 0)
        //NFC
        mAdapter = NfcAdapter.getDefaultAdapter(this)
        mPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        )
        val sectorPwd = sp.getString("sectorPwd", "FFFFFFFFFFFF") ?: ""
        val sector = sp.getString("sector", "2") ?: ""
        val block = sp.getString("block", "10") ?: ""
        NFCHelper.setup(sectorPwd, sector, block)
        etPwd.setText(NFCHelper.sectorPwd)
        etSector.setText(NFCHelper.readSector.toString())
        etBlock.setText(NFCHelper.readBlock.toString())
        buttonSave.setOnClickListener {
            val sectorPwd1 = etPwd.text.toString()
            val sector1 = etSector.text.toString()
            val block1 = etBlock.text.toString()
            NFCHelper.setup(sectorPwd1, sector1, block1)
            sp.edit().apply {
                putString("sectorPwd", sectorPwd1)
                putString("sector", sector1)
                putString("block", block1)
            }.apply()
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
        }
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
        NFCHelper.closeZHRFID()
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
