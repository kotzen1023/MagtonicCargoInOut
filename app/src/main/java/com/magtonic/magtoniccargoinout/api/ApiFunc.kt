package com.magtonic.magtoniccargoinout.api

import android.util.Log
import com.google.gson.Gson
import com.magtonic.magtoniccargoinout.model.send.HttpGuestInOrOutMultiPara
import com.magtonic.magtoniccargoinout.model.send.HttpGuestNotLeaveGetPara
import com.magtonic.magtoniccargoinout.model.send.HttpReceiptGetPara
import com.magtonic.magtoniccargoinout.model.send.HttpShipmentPara
import okhttp3.*

import java.io.IOException


class ApiFunc {
    private val mTAG = ApiFunc::class.java.name
    //val baseIP = "http://192.1.1.42:81/asmx/WebService.asmx/"
    private val baseIP = "http://192.1.1.50/asmx/webservice.asmx/"
    //private val testIP = "http://192.1.1.38/web/ws/r/aws_ttsrv2_toptest"
    //private val realIP = "http://192.1.1.38/web/ws/r/aws_ttsrv2"
    private val iepIP = "http://192.1.1.121/webs.asmx/"

    //api http address string define

    //receipt
    //2.Get Receipt Content
    private val apiStrGetReceipt = baseIP + "Sel_pmn01"

    private val apiStrGuestInOrOutMulti = iepIP + "webs_app_car003"

    private val apiStrGetGuestNotLeaveYetMulti = iepIP + "webs_app_car004"

    private val apiStrShipmentCheck = iepIP + "webs_app_ogb01"

    private object ContentType {

        const val title = "Content-Type"
        const val xxxForm = "application/x-www-form-urlencoded"

    }//ContentType

    fun getReceipt(para: HttpReceiptGetPara, callback: Callback) {
        Log.e(mTAG, "ApiFunc->getReceipt")
        postWithParaPJsonStr(apiStrGetReceipt, Gson().toJson(para), callback)

    }

    fun guestInOrOutMulti(para: HttpGuestInOrOutMultiPara, callback: Callback) {
        postWithParaPJsonStr(apiStrGuestInOrOutMulti, Gson().toJson(para), callback)
    }

    fun getGuestMulti(para: HttpGuestNotLeaveGetPara, callback: Callback) {
        postWithParaPJsonStr(apiStrGetGuestNotLeaveYetMulti, Gson().toJson(para), callback)
    }

    fun getShipmentCheckMulti(para: HttpShipmentPara, callback: Callback) {
        postWithParaPJsonStr(apiStrShipmentCheck, Gson().toJson(para), callback)
    }

    // post with only one para  "p_json"
    private fun postWithParaPJsonStr(url: String, jsonStr: String, callback: Callback) {
        Log.e(mTAG, "->postWithParaPJsonStr")
        val body = FormBody.Builder()
            .add("p_json", jsonStr)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader(ContentType.title, ContentType.xxxForm)
            .build()

        val client = OkHttpClient().newBuilder()
            .retryOnConnectionFailure(false)
            .build()

        try {
            val response = client.newCall(request).enqueue(callback)
            Log.d("postWithParaPJsonStr", "response = $response")

            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
            client.cache?.close()

        } catch (e: IOException) {



            e.printStackTrace()
        }

    }
}