package com.ocm.zh500demo.utils

import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.os.Handler
import android.util.Log
import com.example.zh_idreader.IDReader
import kotlin.collections.ArrayList

//NFC 处理相关
object NFCHelper {

    private const val TAG = "NFCHelper"
    private val rfidCrl = IDReader()
    var listener: NFCListener? = null
    private val handler = Handler()
    private var toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

    var sectorPwd = "FFFFFFFFFFFF" //扇区密码
        private set
    var readSector = 2 //读的扇区
        private set
    var readBlock = 10 //读的块
        private set

    fun setup(sectorPwd: String, sector: String, block: String) {
        this.sectorPwd = sectorPwd
        readSector = sector.toIntOrNull() ?: 2
        readBlock = block.toIntOrNull() ?: 10
        val pwd = StringHelper.hexStringToBytes(sectorPwd)
        val keyAndMode = ArrayList<Int>()
        keyAndMode.add(0x55)
        keyAndMode.add(0x0A)
        keyAndMode.addAll(pwd.map { it.toInt() })
        rfidCrl.Sector_set_key_and_mode(keyAndMode.toIntArray())

        val secNumAndBlockNum = ArrayList<Int>()
        secNumAndBlockNum.add(0x66)
        secNumAndBlockNum.add(readSector)
        secNumAndBlockNum.add((readBlock) - readSector*4)
        rfidCrl.Sector_set_read_sectorNum_and_blockNum(secNumAndBlockNum.toIntArray())
    }

    fun decode(intent: Intent) {
        val tagFromIntent = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        tagFromIntent?.let {
            var cardno = convertCardNo(StringHelper.bytesToHex(it.id))
            cardno = java.lang.Long.parseLong(cardno, 16).toString()
            while (cardno.length < 10) cardno = "0$cardno"

            try {
                val blockStr = decodeSector(tagFromIntent)
                listener?.onNFCReadSuccess(cardno, blockStr)
            } catch (ex: Exception) {
                ex.printStackTrace()
                listener?.onNFCReadSuccess(cardno, null)
            }
        }
    }

    private fun convertCardNo(tmp: String?): String {
        var tmp = tmp
        if (tmp == null) tmp = "00000000"
        while (tmp!!.length < 8) tmp = "0$tmp"
        val cardChar = CharArray(8)
        try {
            cardChar[0] = tmp[6]
            cardChar[1] = tmp[7]
            cardChar[2] = tmp[4]
            cardChar[3] = tmp[5]
            cardChar[4] = tmp[2]
            cardChar[5] = tmp[3]
            cardChar[6] = tmp[0]
            cardChar[7] = tmp[1]
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return String(cardChar)
    }

    /**
     * 黄龙呼啦网络读卡方法
     * @param tag Tag
     */
    private fun decodeSector(tag: Tag): String {
        val mc = MifareClassic.get(tag)
        try {
            mc.connect()
            val pwd = StringHelper.hexStringToBytes(sectorPwd)
            val auth = mc.authenticateSectorWithKeyA(readSector, pwd)
            if (!auth) Throwable("验证失败")
            try {
                val datas = mc.readBlock(readBlock)
                val blockStr = StringHelper.bytesToHex(datas)
                Log.e(TAG, "块： ${readBlock}， 块内容：$blockStr")
                return blockStr
            } catch (ex: Exception) {
                Log.e(TAG,"读扇区失败 $ex，块： $readBlock")
            }
        } catch (ex: Exception) {
            Log.e(TAG, "操作异常 $ex")
        } finally {
            mc.close()
        }
        return ""
    }


    /********** 新的设备，NFC读取方法，可兼容 **********/
    fun closeZHRFID() {
        rfidCrl.Sector_read_disable()
        rfidCrl.closeRFID()
        mThreadRunning = false
        mRunning = false
    }

    private var mThreadRunning = true
    private var mRunning = false
    private val ActivityArray0 = IntArray(32)
    fun openZHRFID() {
        rfidCrl.openRFID()
        rfidCrl.Sector_read_enable()
        val pwd = StringHelper.hexStringToBytes(sectorPwd)
        val keyAndMode = ArrayList<Int>()
        keyAndMode.add(0x55)
        keyAndMode.add(0x0A)
        keyAndMode.addAll(pwd.map { it.toInt() })
        rfidCrl.Sector_set_key_and_mode(keyAndMode.toIntArray())

        val secNumAndBlockNum = ArrayList<Int>()
        secNumAndBlockNum.add(0x66)
        secNumAndBlockNum.add(readSector)
        secNumAndBlockNum.add((readBlock) - readSector*4)
        rfidCrl.Sector_set_read_sectorNum_and_blockNum(secNumAndBlockNum.toIntArray())
        mThreadRunning = true
        mRunning = true

        object : Thread() {
            override fun run() {
                super.run()
                Log.e("dxx", "thread run()")
                while (mThreadRunning) {
                    try {
                        sleep(200)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    Log.e("dxx", "thread run(1)mRunning:$mRunning")
                    if (mRunning) {
                        rfidCrl.Read_rfid_data(ActivityArray0)
                        val cardNoArray = ActivityArray0.copyOfRange(4, 8)
                        var cardNo10D =
                            convertCardNo(StringHelper.bytesToHex(cardNoArray.map { it.toByte() }
                                .toByteArray()))
                        cardNo10D = java.lang.Long.parseLong(cardNo10D, 16).toString()
                        while (cardNo10D.length < 10) cardNo10D = "0$cardNo10D"
                        Log.d("dxx", "cardNo10D: $cardNo10D")
                        Log.d("dxx",
                            "read: ${StringHelper.bytesToHex(ActivityArray0.map { it.toByte() }
                                .toByteArray())}"
                        )
                        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
                        if (ActivityArray0[8] == 0) {
                            handler.post {
                                listener?.onNFCReadSuccess(cardNo10D, null)
                            }
                            continue
                        }
                        val blockArray = ActivityArray0.copyOfRange(9, 25)
                        val blockContent =
                            StringHelper.bytesToHex(blockArray.map { it.toByte() }
                                .toByteArray())
                        Log.d("dxx", "blockContent: $blockContent")
                        handler.post {
                            listener?.onNFCReadSuccess(cardNo10D, blockContent)
                        }
                    }
                }
            }
        }.start()
    }

    interface NFCListener {
        /**
         * 读取到 NFC 的内容
         * @param cardNo10D String 卡号 8H 反 10D
         * @param blockContent String? 读取的扇区块内容，如果密码错误，则为null
         */
        fun onNFCReadSuccess(cardNo10D: String, blockContent: String?)
    }
}