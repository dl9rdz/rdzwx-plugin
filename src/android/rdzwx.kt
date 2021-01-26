/*
 * Copyright (C) Hansi Reiser <dl9rdz@darc.de>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.dl9rdz

import android.os.Handler

import org.apache.cordova.LOG
import org.apache.cordova.CordovaArgs
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.CordovaInterface
import org.apache.cordova.PluginResult

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import java.net.InetAddress
import java.net.Socket
import java.io.ByteArrayInputStream
import java.io.OutputStream
import kotlin.text.Charsets

import kotlin.concurrent.thread

const val LOG_TAG = "dl9rdz-rdzwx"

// Just for testing
val fakeData: ByteArray =
        "?????????`????a?;S2260991 *120313z4830.24N/01228.21EO076/015/A=090787!ws:!&Clb=7.8m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120314z4830.25N/01228.23EO075/015/A=090812!w;F!&Clb=7.2m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120315z4830.25N/01228.25EO075/016/A=090838!waX!&Clb=8.8m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120316z4830.26N/01228.27EO076/017/A=090868!w0x!&Clb=8.7m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120317z4830.26N/01228.30EO076/017/A=090893!wUH!&Clb=6.9m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120318z4830.27N/01228.32EO075/018/A=090917!w'y!&Clb=7.8m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120319z4830.27N/01228.35EO073/017/A=090945!wVL!&Clb=9.2m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120320z4830.28N/01228.37EO070/016/A=090975!w/m!&Clb=9.2m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120321z4830.28N/01228.40EO067/016/A=091000!wg&!&Clb=6.0m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120322z4830.29N/01228.42EO066/015/A=091021!wF0!&Clb=5.8m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120323z4830.30N/01228.44EO066/015/A=091042!w#1!&Clb=8.3m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120324z4830.30N/01228.46EO068/015/A=091070!wX0!&Clb=8.2m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120325z4830.31N/01228.48EO069/015/A=091095!w24!&Clb=7.3m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120326z4830.31N/01228.50EO068/015/A=091120!wi>!&Clb=8.3m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120327z4830.32N/01228.52EO066/016/A=091149!wEL!&Clb=8.9m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120328z4830.33N/01228.54EO065/016/A=091175!w)\\!&Clb=6.7m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120329z4830.33N/01228.56EO065/017/A=091198!wlr!&Clb=7.0m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120330z4830.34N/01228.59EO066/018/A=091222!wX9!&Clb=7.9m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120331z4830.35N/01228.61EO067/019/A=091245!wBf!&Clb=6.6m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120332z4830.36N/01228.64EO068/019/A=091266!w+D!&Clb=5.7m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120333z4830.36N/01228.67EO067/020/A=091290!wt'!&Clb=7.9m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120334z4830.37N/01228.69EO065/019/A=091314!w``!&Clb=8.5m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120335z4830.38N/01228.72EO062/018/A=091341!wR0!&Clb=7.7m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120336z4830.39N/01228.74EO061/017/A=091366!wHN!&Clb=6.6m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120337z4830.40N/01228.76EO060/016/A=091388!w:^!&Clb=6.2m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120338z4830.41N/01228.78EO062/016/A=091410!w(f!&Clb=7.7m/s 402.300MHz Type=RS41Ì????????`????a?;S2260991 *120339z4830.41N/01228.80EO065/015/A=091434!wil!&Clb=7.9m/s 402.300MHz Type=RS41".toByteArray(Charsets.ISO_8859_1)

class JsonRdzHandler {
    private var running: Boolean = false
    private var host: InetAddress? = null
    private var port: Int? = null
    private var rdzwx: RdzWx? = null

    init {
        thread { this.run() }
    }

    // public methods:
    fun connectTo(host: InetAddress, port: Int) {
        this.host = host
        this.port = port
    }


    // internal private methods
    fun run() {
        LOG.d(LOG_TAG, "JsonRdz thread is running")
        while (true) {
            // if no host/port known: do nothing, retry in a second
            val host = this.host
            val port = this.port
            if (host == null || port == null) {
                LOG.d(LOG_TAG, "no host/port")
                Thread.sleep(1000)
                continue
            }
            running = true
            try {
                runConnection(host, port)
            } catch (ex: Exception) {
                LOG.d(LOG_TAG, "Connection closed by exception " + ex.toString())
            }
            rdzwx?.handleTtgoStatus(null)
            running = false
        }
    }

    private var output: OutputStream? = null

    fun initialize(rdzwx: RdzWx) {
        this.rdzwx = rdzwx
    }

    fun postGpsPosition(latitude: Double, longitude: Double, altitude: Double, bearing: Float) {
        val b: ByteArray = ("{\"lat\": " + latitude + " , \"lon\": " + longitude + " , \"alt\": " + altitude + " , \"course\": " + bearing + " }\n").toByteArray(Charsets.ISO_8859_1)
        try {
            output?.write(b)
        } catch (ex: Exception) {
            output = null
        }
    }

    fun postAlive() {
        try {
            output?.write("{\"status\": 1}\n".toByteArray(Charsets.ISO_8859_1))
        } catch (ex: Exception) {
            output = null
        }
        LOG.d(LOG_TAG, "status update")
    }

    private fun runConnection(host: InetAddress, port: Int) {
        LOG.d(LOG_TAG, "Trying to connect!")
        val socket: Socket?
        try {
            socket = Socket(host, port)
        } catch (ex: Exception) {
            Thread.sleep(1000)
            LOG.d(LOG_TAG, "connect failed: " + ex.toString())
            running = false
            return
        }
        LOG.d(LOG_TAG, "Connected!")
        rdzwx?.handleTtgoStatus(host.getHostAddress())

        val input = socket.getInputStream()
        output = socket.getOutputStream()
        // for testing
        //val input = ByteArrayInputStream(fakeData)

        var buf = byteArrayOf()

        LOG.d(LOG_TAG, "Reading from input stream")

        /* OLD: TNC KISS
        // read and parse data first kiss 2 raw, then parse call>APZRDZ:;id_id_id_*hhmmsszdddd.ddxyddddd.ddxy(ddd/ddd)?/A=d{6}(!w__!)blablabla
        // kiss2raw: replace \xDB\xDC with \xC0 and \xDB\xDD with \xDB, undteil \XC0 (FEND) is found
        loop@ while(true) {
            val byte = input.read()
            //LOG.d(LOG_TAG, "GOt a byte: "+byte.toString())
            when(byte) {
            -1 -> break@loop
                0xC0 -> { processFrame(buf); buf = byteArrayOf() }
            0xDB -> when(input.read()) { 0xDC -> buf += 0xC0.toByte(); 0xDD -> buf += 0xDB.toByte() ; else -> error("bb") }
            else -> buf += byte.toByte()
            }
        }
        */
        // new: jsonrdz
        jloop@ while (true) {
            val byte = input.read()
            when (byte) {
                -1 -> break@jloop
                '}'.toInt() -> {
                    buf += byte.toByte(); processFrame(buf); buf = byteArrayOf()
                }
                '\n'.toInt() -> {
                }
                else -> buf += byte.toByte()
            }
        }
        output = null
        socket.close()
    }

    fun processFrame(data: ByteArray) {
        val s = String(data)
        LOG.d(LOG_TAG, s)
        rdzwx?.handleJsonrdzData(s)
    }
}

class RdzWx : CordovaPlugin() {
    val handler = Handler()
    val gpsHandler = GPSHandler()
    val mdnsHandler = MDNSHandler()
    val jsonrdzHandler = JsonRdzHandler()
    var cb: CallbackContext? = null

    val runnable: Runnable = run {
        Runnable {
            LOG.d(LOG_TAG, "Runnable is running - test")
            jsonrdzHandler.postAlive()
            handler.postDelayed(runnable, 5000)
        }
    }

    fun runJsonRdz(serviceInfo: NsdServiceInfo) {
        LOG.d(LOG_TAG, "setting target host for jsonrdz handler")
        jsonrdzHandler.connectTo(serviceInfo.host, serviceInfo.port)
    }

    fun handleJsonrdzData(data: String) {
        if (cb == null) return
        val plugRes = PluginResult(PluginResult.Status.OK, data)
        plugRes.setKeepCallback(true)
        cb?.sendPluginResult(plugRes)
    }

    fun handleTtgoStatus(ip: String?) {
        // ip==null: disconnected; else: connected
        if (cb == null) return
        val status: String
        if (ip == null) status = " { \"msgtype\": \"ttgostatus\", \"state\": \"offline\", \"ip\": \"\" } "
        else status = " { \"msgtype\": \"ttgostatus\", \"state\": \"online\", \"ip\": \"" + ip + "\" } "
        val plugRes = PluginResult(PluginResult.Status.OK, status)
        plugRes.setKeepCallback(true)
        cb?.sendPluginResult(plugRes)
    }

    fun updateGps(latitude: Double, longitude: Double, altitude: Double, bearing: Float) {
        jsonrdzHandler.postGpsPosition(latitude, longitude, altitude, bearing)
        if (cb == null) return
        val status = "{ \"msgtype\": \"gps\", \"lat\": " + latitude + ", \"lon\": " + longitude +
                ", \"alt\": " + altitude + ", \"dir\": " + bearing + "}"
        val plugRes = PluginResult(PluginResult.Status.OK, status)
        plugRes.setKeepCallback(true)
        cb?.sendPluginResult(plugRes)
    }

    override fun pluginInitialize() {
        super.initialize(cordova, webView)

        gpsHandler.initialize(this)
        mdnsHandler.initialize(this)
        jsonrdzHandler.initialize(this)
        handler.postDelayed(runnable, 5000)
    }

    override fun onMessage(id: String?, data: Any?): Any? {
        return null
    }

    override fun onRequestPermissionResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        LOG.d(LOG_TAG, "onRequestPermissionResult called")
        // TODO: Check if this is "GPS permission granted" or something else...
        gpsHandler.setupLocationManager(this)
    }


    override fun execute(action: String, args: CordovaArgs, callbackContext: CallbackContext): Boolean {
        when (action) {
            "start" -> {
                LOG.d(LOG_TAG, "execute: start")
                cb = callbackContext
                val plugRes = PluginResult(PluginResult.Status.OK, "{\"status\": \"OK\"}")
                plugRes.setKeepCallback(true)
                cb?.sendPluginResult(plugRes)
                return true
            }
            else -> {
                LOG.d(LOG_TAG, "unknown action: " + action)
                return false
            }
        }
    }

}
