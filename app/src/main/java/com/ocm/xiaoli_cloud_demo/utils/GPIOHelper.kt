package com.ocm.xiaoli_cloud_demo.utils

import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.util.*

/**
 * 二维码模块处理
 */
object GPIOHelper {
    private const val LED_CTL_PATH = "/sys/class/zh_gpio_out/out"
    private const val TAG = "GPIOHelper"

    private enum class LedCTL(val param: String) {
        CAMERA_RED_ON("7"), CAMERA_RED_OFF("8"),
        RELAY_ON("9"), RELAY_OFF("10")
    }

    private var readQRTimer: Timer? = null

    /** 红外补光 **/
    fun irLightOn() {
        ctlLed(LedCTL.CAMERA_RED_ON.param)
    }

    fun irLightOff() {
        readQRTimer?.cancel()
        ctlLed(LedCTL.CAMERA_RED_OFF.param)
    }

    /** 继电器 **/
    fun relayOn() {
        ctlLed(LedCTL.RELAY_ON.param)
    }

    fun relayOff() {
        ctlLed(LedCTL.RELAY_OFF.param)
    }

    private fun ctlLed(cmd: String) {
        val file = File(LED_CTL_PATH)
        if (!file.exists() || !file.canWrite()) {
            Log.e(TAG, "文件不存在 或者 不可写")
            return
        }
        try {
            val fout = FileOutputStream(file)
            val pWriter = PrintWriter(fout)
            pWriter.println(cmd)
            pWriter.flush()
            pWriter.close()
            fout.close()

        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
    }
}