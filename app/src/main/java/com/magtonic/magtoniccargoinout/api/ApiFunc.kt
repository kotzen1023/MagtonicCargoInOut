package com.magtonic.magtoniccargoinout.api

import android.util.Log
import com.google.gson.Gson
import com.magtonic.magtoniccargoinout.MainActivity.Companion.base_ip_address_webservice
import com.magtonic.magtoniccargoinout.MainActivity.Companion.iep_ip_address_webservice
import com.magtonic.magtoniccargoinout.MainActivity.Companion.timeOutSeconds
import com.magtonic.magtoniccargoinout.model.send.*

import okhttp3.*

import java.io.IOException
import java.util.concurrent.TimeUnit


class ApiFunc {
    private val mTAG = ApiFunc::class.java.name
    //val baseIP = "http://192.1.1.42:81/asmx/WebService.asmx/"
    //private val baseIP = "http://192.1.1.50/asmx/webservice.asmx/"
    private val baseIP = base_ip_address_webservice
    //private val testIP = "http://192.1.1.38/web/ws/r/aws_ttsrv2_toptest"
    //private val realIP = "http://192.1.1.38/web/ws/r/aws_ttsrv2"
    //private val iepIP = "http://192.1.1.121/webs.asmx/"
    private val iepIP = iep_ip_address_webservice

    //api http address string define

    //receipt
    //2.Get Receipt Content
    private val apiStrGetReceipt = baseIP + "Sel_pmn01"

    private val apiStrGetReceiptPoint = baseIP + "Sel_pmn03"

    private val apiStrGuestInOrOutMulti = iepIP + "webs_app_car003"

    private val apiStrGetGuestNotLeaveYetMulti = iepIP + "webs_app_car004"

    private val apiStrShipmentCheck = iepIP + "webs_app_ogb01"

    private val apiStrShipmentSignature = iepIP + "webs_app_ogap01"

    private val apiStrShipmentSignatureDetail = iepIP + "webs_app_ogap02"

    private val apiStrShipmentSignatureConfirm = iepIP + "webs_app_ogap03"

    private object ContentType {

        const val title = "Content-Type"
        const val xxxForm = "application/x-www-form-urlencoded"

    }//ContentType

    fun getReceipt(para: HttpReceiptGetPara, callback: Callback) {
        Log.e(mTAG, "ApiFunc->getReceipt")
        postWithParaPJsonStr(apiStrGetReceipt, Gson().toJson(para), callback)

    }

    fun getReceiptPoint(para: HttpReceiptPointGetPara, callback: Callback) {
        Log.e(mTAG, "ApiFunc->getReceiptPoint")
        postWithParaPJsonStrandTimeOut(apiStrGetReceiptPoint, Gson().toJson(para), callback)

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

    fun getShipmentSignatureMulti(para: HttpShipmentSignaturePara, callback: Callback) {
        postWithParaPJsonStr(apiStrShipmentSignature, Gson().toJson(para), callback)
    }

    fun getShipmentSignatureDetail(para: HttpShipmentSignatureDetailPara, callback: Callback) {
        postWithParaPJsonStr(apiStrShipmentSignatureDetail, Gson().toJson(para), callback)
    }

    fun getShipmentSignatureConfirm(para: HttpShipmentSignatureConfirmPara, callback: Callback) {
        postWithParaPJsonStr(apiStrShipmentSignatureConfirm, Gson().toJson(para), callback)
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

    private fun postWithParaPJsonStrandTimeOut(url: String, jsonStr: String, callback: Callback) {
        Log.e(mTAG, "->postWithParaPJsonStrandTimeOut")


        val body = FormBody.Builder()
            .add("p_json", jsonStr)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader(ContentType.title, ContentType.xxxForm)
            .build()



        val client = OkHttpClient().newBuilder()
            //.connectTimeout(5000, TimeUnit.MILLISECONDS) //5 secs
            //.readTimeout(5000, TimeUnit.MILLISECONDS) //5 secs
            //.writeTimeout(5000, TimeUnit.MILLISECONDS) //5 secs
            .connectTimeout(timeOutSeconds, TimeUnit.SECONDS) //5 secs
            .readTimeout(timeOutSeconds, TimeUnit.SECONDS) //5 secs
            .writeTimeout(timeOutSeconds, TimeUnit.SECONDS) //5 secs
            .retryOnConnectionFailure(false)
            .build()


        try {
            val response = client.newCall(request).enqueue(callback)



            Log.d("pPara_pjson_timeout", "response = $response")

            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
            client.cache?.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }


    }
}