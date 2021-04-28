package com.magtonic.magtoniccargoinout

import android.app.AlertDialog
import android.content.*

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore

import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.magtonic.magtoniccargoinout.MainActivity.Companion.isEraser
import com.magtonic.magtoniccargoinout.MainActivity.Companion.isSignMulti
import com.magtonic.magtoniccargoinout.MainActivity.Companion.penColor
import com.magtonic.magtoniccargoinout.MainActivity.Companion.penWidth
import com.magtonic.magtoniccargoinout.MainActivity.Companion.signState
import com.magtonic.magtoniccargoinout.MainActivity.Companion.signatureDetailList
import com.magtonic.magtoniccargoinout.MainActivity.Companion.signatureMultiSignList
import kotlinx.coroutines.*


import java.io.IOException
import java.io.OutputStream



import kotlin.coroutines.CoroutineContext
import com.magtonic.magtoniccargoinout.MainActivity.SignState
import com.magtonic.magtoniccargoinout.ui.data.*

class SignActivity : AppCompatActivity() {
    private val mTAG = SignActivity::class.java.name

    var progressBar: ProgressBar? = null
    private var relativeLayout: RelativeLayout? = null
    private var linearLayoutSign: LinearLayout? = null
    private var linearLayoutUpload: LinearLayout? = null
    private var toastHandle: Toast? = null


    private var signContext: Context? = null


    private var mReceiver: BroadcastReceiver? = null
    private var isRegister = false

    private var menuItemEraser: MenuItem? = null
    //private var isEraser: Boolean = false
    //private var penColor: Int = Color.BLACK
    //private var penWidth: Float = 10f

    private var paintBoard: PaintBoard?= null
    private var btnClear: Button?= null
    private var btnSave: Button?=null
    private var btnPrev: Button?=null
    private var btnSignConfirm: Button?=null

    private val fileUtils: FileUtils?= FileUtils()
    private var uploadSuccess: Boolean = false

    private var linearLayoutSignDetailList: LinearLayout?= null
    private var imageViewShowSignatureDriver: ImageView?=null
    private var imageViewShowSignatureGuard: ImageView?=null
    private var uploadSignNameDriver: String = ""
    private var uploadSignNameGuard: String = ""
    //private var isSignMulti: Boolean = false
    private var sendOrder: String = ""
    private var title: String = ""
    private var sendFragment: String = ""
    private var type: String = ""
    private var date: String = ""

    private var signImageUriPath: Uri?= null

    private var signatureMultiSignListDriver: ArrayList<ShipmentSignatureMultiItem> = ArrayList()
    private var signatureMultiSignListGuard: ArrayList<ShipmentSignatureMultiItem> = ArrayList()
    /*enum class SignState {
        INITIAL,
        DRIVER_UPLOADED,
        GUARD_UPLOADED,
        DRIVER_CONFIRM,
        GUARD_CONFIRM

    }
    companion object {
        @JvmStatic var signState: SignState = SignState.INITIAL
    }*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign)

        signState = SignState.INITIAL

        val intent = this.intent
        isSignMulti = intent.getBooleanExtra("IS_SIGN_MULTI", false)
        sendOrder = intent.getStringExtra("SEND_ORDER") as String
        title = intent.getStringExtra("TITLE") as String
        sendFragment = intent.getStringExtra("SEND_FRAGMENT") as String

        if (intent.getStringExtra("TYPE") != null) {
            type = intent.getStringExtra("TYPE") as String
        }

        if (intent.getStringExtra("DATE") != null) {
            date = intent.getStringExtra("DATE") as String
        }


        Log.e(mTAG, "isSignMulti = $isSignMulti")
        Log.e(mTAG, "sendOrder = $sendOrder")
        Log.e(mTAG, "title = $title")
        Log.e(mTAG, "sendFragment = $sendFragment")

        signContext = applicationContext

        relativeLayout = findViewById(R.id.sign_container)
        linearLayoutSign = findViewById(R.id.linearLayoutSign)
        linearLayoutUpload = findViewById(R.id.linearLayoutUpload)

        linearLayoutSignDetailList = findViewById(R.id.linearLayoutSignDetailList)
        imageViewShowSignatureDriver = findViewById(R.id.imageViewShowSignatureDriver)
        imageViewShowSignatureGuard = findViewById(R.id.imageViewShowSignatureGuard)

        progressBar = ProgressBar(signContext, null, android.R.attr.progressBarStyleLarge)
        val params = RelativeLayout.LayoutParams(MainActivity.screenHeight / 4, MainActivity.screenWidth / 4)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)

        val localRelativeLayout: RelativeLayout? = relativeLayout
        if (localRelativeLayout != null) {
            localRelativeLayout.addView(progressBar, params)
        } else {
            Log.e(mTAG, "localRelativeLayout = null")
        }
        progressBar!!.visibility = View.GONE

        paintBoard = findViewById(R.id.signViewPaint)
        btnClear = findViewById(R.id.signBtnClear)
        btnSave = findViewById(R.id.signBtnSave)
        btnPrev = findViewById(R.id.signBtnPrev)
        btnSignConfirm = findViewById(R.id.signConfirm)

        btnClear!!.setOnClickListener {
            paintBoard!!.clear()
        }

        btnSave!!.setOnClickListener {
            showUploadSignatureDialog()
        }

        btnPrev!!.setOnClickListener {
            paintBoard!!.undo()
        }

        btnSignConfirm!!.setOnClickListener {
            progressBar!!.visibility = View.VISIBLE

            if (!isSignMulti) {
                val confirmIntent = Intent()
                confirmIntent.putExtra("SEND_ORDER", sendOrder)
                when (sendFragment) {
                    "SHIPMENT_SIGNATURE_FRAGMENT" -> {
                        if (signState == SignState.GUARD_UPLOADED) {
                            confirmIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_SIGN_CONFIRM_ACTION
                            confirmIntent.putExtra("SIGN_FILE_NAME", uploadSignNameDriver)
                        } else if (signState == SignState.DRIVER_CONFIRM) {
                            confirmIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_SIGN_CONFIRM_ACTION
                            confirmIntent.putExtra("SIGN_FILE_NAME", uploadSignNameGuard)
                        } else {
                            Log.e(mTAG, "signState = $signState")
                        }


                    }

                }

                signContext!!.sendBroadcast(confirmIntent)
            } else { //guard sign uploaded, start confirm

                Log.e(mTAG, "confirm sign multi, isSignMulti = $isSignMulti")

                progressBar!!.visibility = View.VISIBLE

                Log.e(mTAG, "sign multi, signatureMultiSignList.size = ${signatureMultiSignList.size}")

                if (signState == SignState.GUARD_UPLOADED) { //SignState.GUARD_UPLOADED, then confirm driver
                    Log.e(mTAG, "SignState.GUARD_UPLOADED")
                    signatureMultiSignListDriver.clear()
                    for (item in signatureMultiSignList) {
                        signatureMultiSignListDriver.add(item)
                    }

                    val uploadStartIntent = Intent()
                    uploadStartIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_SIGN_CONFIRM_START
                    signContext!!.sendBroadcast(uploadStartIntent)
                } else if (signState == SignState.DRIVER_CONFIRM) { //SignState.DRIVER_CONFIRM, then confirm guard
                    Log.e(mTAG, "SignState.DRIVER_CONFIRM")
                    signatureMultiSignListGuard.clear()
                    for (item in signatureMultiSignList) {
                        signatureMultiSignListGuard.add(item)
                    }

                    val uploadStartIntent = Intent()
                    uploadStartIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_START
                    signContext!!.sendBroadcast(uploadStartIntent)
                }
            }


        }

        //for action bar
        val actionBar: androidx.appcompat.app.ActionBar? = supportActionBar

        if (actionBar != null) {

            var tempTitle = ""
            actionBar.setDisplayUseLogoEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
            //actionBar.title = getString(R.string.nav_outsourced)
            if (signState == SignState.INITIAL) {
                tempTitle = "$title - " +getString(R.string.shipment_signature_driver)
            }

            actionBar.title = tempTitle
        }


        val filter: IntentFilter

        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != null) {
                    when {
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_CONNECT_TIMEOUT, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_CONNECT_TIMEOUT")
                            progressBar!!.visibility = View.GONE
                            toast(getString(R.string.connect_timeout))
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_CONNECT_UNKNOWN_HOST, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_CONNECT_UNKNOWN_HOST")
                            progressBar!!.visibility = View.GONE
                            toast(getString(R.string.toast_server_error))
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_CONNECT_FAILED, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_CONNECT_FAILED")
                            progressBar!!.visibility = View.GONE
                            toast(getString(R.string.ftp_connect_error))
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_UPLOAD_FAILED, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_UPLOAD_FAILED")
                            progressBar!!.visibility = View.GONE

                            toast(getString(R.string.shipment_signature_upload_failed))
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_UPLOAD_SUCCESS, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_UPLOAD_SUCCESS")

                            uploadSuccess = true
                            //progressBar!!.visibility = View.GONE

                            when (sendFragment) {
                                "SHIPMENT_SIGNATURE_FRAGMENT" -> {
                                    toast(getString(R.string.shipment_signature_upload_success))
                                    //btnSignConfirm!!.text = getString(R.string.shipment_sign_confirm)
                                }

                            }


                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_UPLOAD_COMPLETE, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_UPLOAD_COMPLETE")

                            //delete image
                            deleteImage()

                            progressBar!!.visibility = View.GONE

                            //linearLayoutSign!!.visibility = View.GONE
                            //linearLayoutUpload!!.visibility = View.VISIBLE

                            /*when (sendFragment) {
                                "SHIPMENT_SIGNATURE_FRAGMENT" -> {
                                    val promptView1 = View.inflate(this@SignActivity, R.layout.fragment_shipment_signature_detail_item, null)

                                    val signHeader1 = promptView1.findViewById<TextView>(R.id.shipmentSignatureDetailHeader)
                                    val signContent1 = promptView1.findViewById<TextView>(R.id.shipmentSignatureDetailContent)
                                    signHeader1.text = "出貨單號"
                                    signContent1.text = signatureDetailList[0].data1
                                    linearLayoutSignDetailList!!.addView(promptView1)

                                    val promptView2 = View.inflate(this@SignActivity, R.layout.fragment_shipment_signature_detail_item, null)
                                    val signHeader2 = promptView2.findViewById<TextView>(R.id.shipmentSignatureDetailHeader)
                                    val signContent2 = promptView2.findViewById<TextView>(R.id.shipmentSignatureDetailContent)
                                    signHeader2.text = "項次"
                                    signContent2.text = signatureDetailList[0].data2
                                    linearLayoutSignDetailList!!.addView(promptView2)

                                    val promptView3 = View.inflate(this@SignActivity, R.layout.fragment_shipment_signature_detail_item, null)
                                    val signHeader3 = promptView3.findViewById<TextView>(R.id.shipmentSignatureDetailHeader)
                                    val signContent3 = promptView3.findViewById<TextView>(R.id.shipmentSignatureDetailContent)
                                    signHeader3.text = "訂單編號"
                                    signContent3.text = signatureDetailList[0].data3
                                    linearLayoutSignDetailList!!.addView(promptView3)

                                    val promptView4 = View.inflate(this@SignActivity, R.layout.fragment_shipment_signature_detail_item, null)
                                    val signHeader4 = promptView4.findViewById<TextView>(R.id.shipmentSignatureDetailHeader)
                                    val signContent4 = promptView4.findViewById<TextView>(R.id.shipmentSignatureDetailContent)
                                    signHeader4.text = "訂單項次"
                                    signContent4.text = signatureDetailList[0].data4
                                    linearLayoutSignDetailList!!.addView(promptView4)

                                    val promptView5 = View.inflate(this@SignActivity, R.layout.fragment_shipment_signature_detail_item, null)
                                    val signHeader5 = promptView5.findViewById<TextView>(R.id.shipmentSignatureDetailHeader)
                                    val signContent5 = promptView5.findViewById<TextView>(R.id.shipmentSignatureDetailContent)
                                    signHeader5.text = "料件編號"
                                    signContent5.text = signatureDetailList[0].data5
                                    linearLayoutSignDetailList!!.addView(promptView5)

                                    val promptView6 = View.inflate(this@SignActivity, R.layout.fragment_shipment_signature_detail_item, null)
                                    val signHeader6 = promptView6.findViewById<TextView>(R.id.shipmentSignatureDetailHeader)
                                    val signContent6 = promptView6.findViewById<TextView>(R.id.shipmentSignatureDetailContent)
                                    signHeader6.text = "品名"
                                    signContent6.text = signatureDetailList[0].data6
                                    linearLayoutSignDetailList!!.addView(promptView6)

                                    val promptView7 = View.inflate(this@SignActivity, R.layout.fragment_shipment_signature_detail_item, null)
                                    val signHeader7 = promptView7.findViewById<TextView>(R.id.shipmentSignatureDetailHeader)
                                    val signContent7 = promptView7.findViewById<TextView>(R.id.shipmentSignatureDetailContent)
                                    signHeader7.text = "數量"
                                    signContent7.text = signatureDetailList[0].data7
                                    linearLayoutSignDetailList!!.addView(promptView7)

                                }

                            }*/

                            signState = SignState.DRIVER_UPLOADED
                            var tempTitle = ""
                            if (signState == SignState.DRIVER_UPLOADED) {
                                tempTitle = "$title - " +getString(R.string.shipment_signature_guard)
                            }
                            actionBar!!.title = tempTitle


                            imageViewShowSignatureDriver!!.setImageBitmap(paintBoard!!.bitmap)

                            paintBoard!!.clear()

                            toastLong(getString(R.string.shipment_signature_let_guard_sign))
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_SIGN_CONFIRM_FAILED, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_DRIVER_SIGN_CONFIRM_FAILED")

                            progressBar!!.visibility = View.GONE
                            toast(getString(R.string.shipment_signature_confirm_failed))
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_SIGN_CONFIRM_SUCCESS, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_DRIVER_SIGN_CONFIRM_SUCCESS")

                            progressBar!!.visibility = View.GONE
                            signState = SignState.DRIVER_CONFIRM

                            val guardConfirmIntent = Intent()
                            guardConfirmIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_SIGN_CONFIRM_ACTION
                            guardConfirmIntent.putExtra("SEND_ORDER", sendOrder )
                            guardConfirmIntent.putExtra("SIGN_FILE_NAME", uploadSignNameGuard)
                            signContext!!.sendBroadcast(guardConfirmIntent)
                        }
                        //security guard
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_CONNECT_TIMEOUT, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_CONNECT_TIMEOUT")
                            progressBar!!.visibility = View.GONE
                            toast(getString(R.string.connect_timeout))
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_CONNECT_UNKNOWN_HOST, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_CONNECT_UNKNOWN_HOST")
                            progressBar!!.visibility = View.GONE
                            toast(getString(R.string.toast_server_error))
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_CONNECT_FAILED, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_CONNECT_FAILED")
                            progressBar!!.visibility = View.GONE
                            toast(getString(R.string.ftp_connect_error))
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_UPLOAD_FAILED, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_UPLOAD_FAILED")
                            progressBar!!.visibility = View.GONE

                            toast(getString(R.string.shipment_signature_upload_failed))
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_UPLOAD_SUCCESS, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_UPLOAD_SUCCESS")

                            uploadSuccess = true
                            //progressBar!!.visibility = View.GONE

                            when (sendFragment) {
                                "SHIPMENT_SIGNATURE_FRAGMENT" -> {
                                    toast(getString(R.string.shipment_signature_upload_success))
                                    btnSignConfirm!!.text = getString(R.string.shipment_sign_confirm)

                                    var tempTitle = ""
                                    if (signState == SignState.GUARD_UPLOADED) {
                                        tempTitle = "$title - " +getString(R.string.shipment_sign_confirm)
                                    }
                                    actionBar!!.title = tempTitle
                                }

                            }


                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_UPLOAD_COMPLETE, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_UPLOAD_COMPLETE")

                            //delete image
                            deleteImage()

                            progressBar!!.visibility = View.GONE

                            linearLayoutSign!!.visibility = View.GONE
                            linearLayoutUpload!!.visibility = View.VISIBLE

                            when (sendFragment) {
                                "SHIPMENT_SIGNATURE_FRAGMENT" -> {
                                    val promptView1 = View.inflate(this@SignActivity, R.layout.fragment_shipment_signature_detail_item, null)

                                    val signHeader1 = promptView1.findViewById<TextView>(R.id.shipmentSignatureDetailHeader)
                                    val signContent1 = promptView1.findViewById<TextView>(R.id.shipmentSignatureDetailContent)
                                    signHeader1.text = "出貨單號"
                                    signContent1.text = signatureDetailList[0].data1
                                    linearLayoutSignDetailList!!.addView(promptView1)

                                    val promptView2 = View.inflate(this@SignActivity, R.layout.fragment_shipment_signature_detail_item, null)
                                    val signHeader2 = promptView2.findViewById<TextView>(R.id.shipmentSignatureDetailHeader)
                                    val signContent2 = promptView2.findViewById<TextView>(R.id.shipmentSignatureDetailContent)
                                    signHeader2.text = "項次"
                                    signContent2.text = signatureDetailList[0].data2
                                    linearLayoutSignDetailList!!.addView(promptView2)

                                    val promptView3 = View.inflate(this@SignActivity, R.layout.fragment_shipment_signature_detail_item, null)
                                    val signHeader3 = promptView3.findViewById<TextView>(R.id.shipmentSignatureDetailHeader)
                                    val signContent3 = promptView3.findViewById<TextView>(R.id.shipmentSignatureDetailContent)
                                    signHeader3.text = "訂單編號"
                                    signContent3.text = signatureDetailList[0].data3
                                    linearLayoutSignDetailList!!.addView(promptView3)

                                    val promptView4 = View.inflate(this@SignActivity, R.layout.fragment_shipment_signature_detail_item, null)
                                    val signHeader4 = promptView4.findViewById<TextView>(R.id.shipmentSignatureDetailHeader)
                                    val signContent4 = promptView4.findViewById<TextView>(R.id.shipmentSignatureDetailContent)
                                    signHeader4.text = "訂單項次"
                                    signContent4.text = signatureDetailList[0].data4
                                    linearLayoutSignDetailList!!.addView(promptView4)

                                    val promptView5 = View.inflate(this@SignActivity, R.layout.fragment_shipment_signature_detail_item, null)
                                    val signHeader5 = promptView5.findViewById<TextView>(R.id.shipmentSignatureDetailHeader)
                                    val signContent5 = promptView5.findViewById<TextView>(R.id.shipmentSignatureDetailContent)
                                    signHeader5.text = "料件編號"
                                    signContent5.text = signatureDetailList[0].data5
                                    linearLayoutSignDetailList!!.addView(promptView5)

                                    val promptView6 = View.inflate(this@SignActivity, R.layout.fragment_shipment_signature_detail_item, null)
                                    val signHeader6 = promptView6.findViewById<TextView>(R.id.shipmentSignatureDetailHeader)
                                    val signContent6 = promptView6.findViewById<TextView>(R.id.shipmentSignatureDetailContent)
                                    signHeader6.text = "品名"
                                    signContent6.text = signatureDetailList[0].data6
                                    linearLayoutSignDetailList!!.addView(promptView6)

                                    val promptView7 = View.inflate(this@SignActivity, R.layout.fragment_shipment_signature_detail_item, null)
                                    val signHeader7 = promptView7.findViewById<TextView>(R.id.shipmentSignatureDetailHeader)
                                    val signContent7 = promptView7.findViewById<TextView>(R.id.shipmentSignatureDetailContent)
                                    signHeader7.text = "數量"
                                    signContent7.text = signatureDetailList[0].data7
                                    linearLayoutSignDetailList!!.addView(promptView7)

                                }

                            }

                            signState = SignState.GUARD_UPLOADED

                            imageViewShowSignatureGuard!!.setImageBitmap(paintBoard!!.bitmap)
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_SIGN_CONFIRM_FAILED, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_GUARD_SIGN_CONFIRM_FAILED")

                            progressBar!!.visibility = View.GONE
                            toast(getString(R.string.shipment_signature_confirm_failed))
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_SIGN_CONFIRM_SUCCESS, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_GUARD_SIGN_CONFIRM_SUCCESS")

                            progressBar!!.visibility = View.GONE
                            signState = SignState.GUARD_CONFIRM

                            finish()
                        }

                        //multi sign driver sign ftp upload start
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_UPLOAD_START, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_UPLOAD_START")

                            if (signatureMultiSignListDriver.size > 0) {

                                Log.e(mTAG, "=== signatureMultiSignListDriver list start===")
                                for (i in 0 until signatureMultiSignListDriver.size) {
                                    Log.d(mTAG, "signatureMultiSignListDriver[$i]=${signatureMultiSignListDriver[i].getShipmentNo()}")
                                }
                                Log.e(mTAG, "=== signatureMultiSignListDriver list end  ===")

                                //get first
                                uploadMulti(signatureMultiSignListDriver[0].getShipmentNo() as String)

                            } else {
                                toastLong(getString(R.string.shipment_signature_multi_driver_upload_list_empty))
                            }

                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_CONNECT_TIMEOUT, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_CONNECT_TIMEOUT")
                            progressBar!!.visibility = View.GONE
                            toast(getString(R.string.connect_timeout))
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_CONNECT_UNKNOWN_HOST, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_CONNECT_UNKNOWN_HOST")
                            progressBar!!.visibility = View.GONE
                            toast(getString(R.string.toast_server_error))
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_CONNECT_FAILED, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_CONNECT_FAILED")
                            progressBar!!.visibility = View.GONE
                            toast(getString(R.string.ftp_connect_error))
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_UPLOAD_FAILED, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_UPLOAD_FAILED")
                            progressBar!!.visibility = View.GONE

                            toast(getString(R.string.shipment_signature_upload_failed))
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_UPLOAD_SUCCESS, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_UPLOAD_SUCCESS")

                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_UPLOAD_COMPLETE, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_UPLOAD_COMPLETE")

                            //remove first one
                            if (signatureMultiSignListDriver.size > 0) {
                                //remove save image
                                deleteImage()
                                //remove first one from list
                                signatureMultiSignListDriver.removeAt(0)
                            }

                            if (signatureMultiSignListDriver.size > 0) {
                                //Do next upload
                                Log.e(mTAG, "=== signatureMultiSignListDriver list start===")
                                for (i in 0 until signatureMultiSignListDriver.size) {
                                    Log.d(mTAG, "signatureMultiSignListDriver[$i]=${signatureMultiSignListDriver[i].getShipmentNo()}")
                                }
                                Log.e(mTAG, "=== signatureMultiSignListDriver list end  ===")

                                //get first
                                uploadMulti(signatureMultiSignListDriver[0].getShipmentNo() as String)
                            } else {
                                //delete image

                                deleteImage()
                                progressBar!!.visibility = View.GONE
                                signState = SignState.DRIVER_UPLOADED
                                var tempTitle = ""
                                if (signState == SignState.DRIVER_UPLOADED) {
                                    tempTitle = "$title - " +getString(R.string.shipment_signature_guard)
                                }
                                actionBar!!.title = tempTitle
                                imageViewShowSignatureDriver!!.setImageBitmap(paintBoard!!.bitmap)
                                paintBoard!!.clear()
                                toastLong(getString(R.string.shipment_signature_let_guard_sign))

                            }


                        }

                        //multi sign guard sign ftp upload start
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_UPLOAD_START, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_UPLOAD_START")

                            if (signatureMultiSignListGuard.size > 0) {

                                Log.e(mTAG, "=== signatureMultiSignListGuard list start===")
                                for (i in 0 until signatureMultiSignListGuard.size) {
                                    Log.d(mTAG, "signatureMultiSignListGuard[$i]=${signatureMultiSignListGuard[i].getShipmentNo()}")
                                }
                                Log.e(mTAG, "=== signatureMultiSignListGuard list end  ===")

                                //get first
                                uploadMulti(signatureMultiSignListGuard[0].getShipmentNo() as String)

                            } else {
                                toastLong(getString(R.string.shipment_signature_multi_guard_upload_list_empty))
                            }

                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_CONNECT_TIMEOUT, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_CONNECT_TIMEOUT")
                            progressBar!!.visibility = View.GONE
                            toast(getString(R.string.connect_timeout))
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_CONNECT_UNKNOWN_HOST, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_CONNECT_UNKNOWN_HOST")
                            progressBar!!.visibility = View.GONE
                            toast(getString(R.string.toast_server_error))
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_CONNECT_FAILED, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_CONNECT_FAILED")
                            progressBar!!.visibility = View.GONE
                            toast(getString(R.string.ftp_connect_error))
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_UPLOAD_FAILED, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_UPLOAD_FAILED")
                            progressBar!!.visibility = View.GONE

                            toast(getString(R.string.shipment_signature_upload_failed))
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_UPLOAD_SUCCESS, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_UPLOAD_SUCCESS")

                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_UPLOAD_COMPLETE, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_UPLOAD_COMPLETE")

                            //remove first one
                            if (signatureMultiSignListGuard.size > 0) {
                                //remove save image
                                deleteImage()
                                //remove first one from list
                                signatureMultiSignListGuard.removeAt(0)
                            }

                            if (signatureMultiSignListGuard.size > 0) {
                                //Do next upload
                                Log.e(mTAG, "=== signatureMultiSignListGuard list start===")
                                for (i in 0 until signatureMultiSignListGuard.size) {
                                    Log.d(mTAG, "signatureMultiSignListGuard[$i]=${signatureMultiSignListGuard[i].getShipmentNo()}")
                                }
                                Log.e(mTAG, "=== signatureMultiSignListGuard list end  ===")

                                //get first
                                uploadMulti(signatureMultiSignListGuard[0].getShipmentNo() as String)
                            } else {
                                //delete image

                                /*deleteImage()
                                progressBar!!.visibility = View.GONE
                                signState = SignState.GUARD_UPLOADED
                                var tempTitle = ""
                                if (signState == SignState.GUARD_UPLOADED) {
                                    tempTitle = "$title - " +getString(R.string.shipment_sign_confirm)
                                }
                                actionBar!!.title = tempTitle
                                imageViewShowSignatureGuard!!.setImageBitmap(paintBoard!!.bitmap)
                                paintBoard!!.clear()
                                toastLong(getString(R.string.shipment_sign_confirm))*/

                                progressBar!!.visibility = View.GONE

                                linearLayoutSign!!.visibility = View.GONE
                                linearLayoutUpload!!.visibility = View.VISIBLE

                                when (sendFragment) {
                                    "SHIPMENT_SIGNATURE_FRAGMENT" -> {

                                        for (item in signatureMultiSignList) {
                                            val promptView1 = View.inflate(this@SignActivity, R.layout.fragment_shipment_signature_detail_item, null)

                                            val signHeader1 = promptView1.findViewById<TextView>(R.id.shipmentSignatureDetailHeader)
                                            val signContent1 = promptView1.findViewById<TextView>(R.id.shipmentSignatureDetailContent)
                                            signHeader1.text = item.getShipmentNo()
                                            signContent1.visibility = View.GONE
                                            linearLayoutSignDetailList!!.addView(promptView1)
                                        }

                                    }

                                }

                                signState = SignState.GUARD_UPLOADED

                                imageViewShowSignatureGuard!!.setImageBitmap(paintBoard!!.bitmap)

                            }


                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_SIGN_CONFIRM_START, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_SIGN_CONFIRM_START")
                            if (signatureMultiSignListDriver.size > 0) {

                                Log.e(mTAG, "=== signatureMultiSignListDriver list start===")
                                for (i in 0 until signatureMultiSignListDriver.size) {
                                    Log.d(mTAG, "signatureMultiSignListDriver[$i]=${signatureMultiSignListDriver[i].getShipmentNo()}")
                                }
                                Log.e(mTAG, "=== signatureMultiSignListDriver list end  ===")

                                //get first
                                //uploadMulti(signatureMultiSignListDriver[0].getShipmentNo() as String)
                                uploadSignNameDriver = signatureMultiSignListDriver[0].getShipmentNo() + "1.jpg"
                                val startIntent = Intent()
                                startIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_SIGN_CONFIRM_ACTION
                                startIntent.putExtra("SHIPMENT_NO", signatureMultiSignListDriver[0].getShipmentNo() )
                                startIntent.putExtra("SIGN_FILE_NAME", uploadSignNameDriver)
                                signContext!!.sendBroadcast(startIntent)

                            } else {
                                progressBar!!.visibility = View.GONE
                                toastLong(getString(R.string.shipment_signature_multi_driver_upload_list_empty))
                            }
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_SIGN_CONFIRM_FAILED, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_SIGN_CONFIRM_FAILED")
                            progressBar!!.visibility = View.GONE
                            toast(getString(R.string.shipment_signature_confirm_failed))
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_SIGN_CONFIRM_SUCCESS, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_SIGN_CONFIRM_SUCCESS")
                            //remove first one
                            if (signatureMultiSignListDriver.size > 0) {
                                //remove first one from list
                                signatureMultiSignListDriver.removeAt(0)
                            }

                            if (signatureMultiSignListDriver.size > 0) {
                                //Do next confirm
                                Log.e(mTAG, "=== signatureMultiSignListDriver list start===")
                                for (i in 0 until signatureMultiSignListDriver.size) {
                                    Log.d(mTAG, "signatureMultiSignListDriver[$i]=${signatureMultiSignListDriver[i].getShipmentNo()}")
                                }
                                Log.e(mTAG, "=== signatureMultiSignListDriver list end  ===")

                                //get first
                                uploadSignNameDriver = signatureMultiSignListDriver[0].getShipmentNo() + "1.jpg"
                                val startIntent = Intent()
                                startIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_SIGN_CONFIRM_ACTION
                                startIntent.putExtra("SHIPMENT_NO", signatureMultiSignListDriver[0].getShipmentNo() )
                                startIntent.putExtra("SIGN_FILE_NAME", uploadSignNameDriver)
                                signContext!!.sendBroadcast(startIntent)
                            } else {
                                Log.e(mTAG, "SignState.DRIVER_CONFIRM")
                                signState = SignState.DRIVER_CONFIRM

                                signatureMultiSignListGuard.clear()
                                for (item in signatureMultiSignList) {
                                    signatureMultiSignListGuard.add(item)
                                }

                                //start guard confirm
                                val startIntent = Intent()
                                startIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_START
                                signContext!!.sendBroadcast(startIntent)

                            }
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_START, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_START")
                            if (signatureMultiSignListGuard.size > 0) {

                                Log.e(mTAG, "=== signatureMultiSignListGuard list start===")
                                for (i in 0 until signatureMultiSignListGuard.size) {
                                    Log.d(mTAG, "signatureMultiSignListGuard[$i]=${signatureMultiSignListGuard[i].getShipmentNo()}")
                                }
                                Log.e(mTAG, "=== signatureMultiSignListGuard list end  ===")

                                //get first
                                uploadSignNameGuard = signatureMultiSignListGuard[0].getShipmentNo() + "2.jpg"
                                val startIntent = Intent()
                                startIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_ACTION
                                startIntent.putExtra("SHIPMENT_NO", signatureMultiSignListGuard[0].getShipmentNo() )
                                startIntent.putExtra("SIGN_FILE_NAME", uploadSignNameGuard)
                                signContext!!.sendBroadcast(startIntent)

                            } else {
                                progressBar!!.visibility = View.GONE
                                toastLong(getString(R.string.shipment_signature_multi_driver_upload_list_empty))
                            }
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_FAILED, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_FAILED")
                            progressBar!!.visibility = View.GONE
                            toast(getString(R.string.shipment_signature_confirm_failed))
                        }
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_SUCCESS, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_SUCCESS")
                            //remove first one
                            if (signatureMultiSignListGuard.size > 0) {
                                //remove first one from list
                                signatureMultiSignListGuard.removeAt(0)
                            }

                            if (signatureMultiSignListGuard.size > 0) {
                                //Do next confirm
                                Log.e(mTAG, "=== signatureMultiSignListGuard list start===")
                                for (i in 0 until signatureMultiSignListGuard.size) {
                                    Log.d(mTAG, "signatureMultiSignListGuard[$i]=${signatureMultiSignListGuard[i].getShipmentNo()}")
                                }
                                Log.e(mTAG, "=== signatureMultiSignListGuard list end  ===")

                                //get first
                                uploadSignNameGuard = signatureMultiSignListGuard[0].getShipmentNo() + "2.jpg"
                                val startIntent = Intent()
                                startIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_ACTION
                                startIntent.putExtra("SHIPMENT_NO", signatureMultiSignListGuard[0].getShipmentNo() )
                                startIntent.putExtra("SIGN_FILE_NAME", uploadSignNameGuard)
                                signContext!!.sendBroadcast(startIntent)
                            } else {

                                progressBar!!.visibility = View.GONE
                                signState = SignState.GUARD_CONFIRM

                                val completeIntent = Intent()
                                completeIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_SIGN_CONFIRM_COMPLETE
                                signContext!!.sendBroadcast(completeIntent)

                                finish()


                            }
                        }
                        //multi sign end
                    }

                }
            }
        }

        if (!isRegister) {
            filter = IntentFilter()
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_CONNECT_TIMEOUT)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_CONNECT_UNKNOWN_HOST)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_CONNECT_FAILED)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_UPLOAD_FAILED)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_UPLOAD_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_UPLOAD_COMPLETE)

            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_SIGN_CONFIRM_FAILED)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_SIGN_CONFIRM_SUCCESS)

            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_CONNECT_TIMEOUT)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_CONNECT_UNKNOWN_HOST)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_CONNECT_FAILED)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_UPLOAD_FAILED)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_UPLOAD_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_UPLOAD_COMPLETE)

            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_SIGN_CONFIRM_FAILED)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_SIGN_CONFIRM_SUCCESS)

            //sign multi, ftp upload, driver
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_UPLOAD_START)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_CONNECT_TIMEOUT)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_CONNECT_UNKNOWN_HOST)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_CONNECT_FAILED)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_UPLOAD_FAILED)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_UPLOAD_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_UPLOAD_COMPLETE)
            //sign multi, ftp upload, guard
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_UPLOAD_START)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_CONNECT_TIMEOUT)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_CONNECT_UNKNOWN_HOST)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_CONNECT_FAILED)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_UPLOAD_FAILED)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_UPLOAD_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_UPLOAD_COMPLETE)
            //sign multi, confirm, driver
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_SIGN_CONFIRM_START)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_SIGN_CONFIRM_FAILED)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_SIGN_CONFIRM_SUCCESS)
            //sign multi, confirm, guard
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_START)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_FAILED)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_SUCCESS)

            signContext?.registerReceiver(mReceiver, filter)
            isRegister = true
            Log.d(mTAG, "registerReceiver mReceiver")
        }

        toastLong(getString(R.string.shipment_signature_let_driver_sign))
    }

    override fun onDestroy() {
        Log.i(mTAG, "onDestroy")

        if (isRegister && mReceiver != null) {
            try {
                signContext!!.unregisterReceiver(mReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }

            isRegister = false
            mReceiver = null
            Log.d(mTAG, "unregisterReceiver mReceiver")
        }

        super.onDestroy()
    }

    override fun onResume() {
        Log.i(mTAG, "onResume")
        super.onResume()
    }

    override fun onPause() {
        Log.i(mTAG, "onPause")
        super.onPause()
    }

    override fun onBackPressed() {

        //showExitConfirmDialog()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.sign, menu)

        menuItemEraser = menu.findItem(R.id.sign_draw_pen_or_eraser)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            android.R.id.home-> {
                finish()
            }

            R.id.sign_draw_pen_or_eraser-> {
                Log.e(mTAG, "sign_draw_pen_or_eraser: $isEraser")

                if (isEraser) { //eraser -> pen
                    Log.e(mTAG, "Color Black")
                    menuItemEraser!!.setIcon(R.drawable.eraser_white)
                    penColor = Color.BLACK
                    penWidth = 10f
                } else {
                    Log.e(mTAG, "Color White")
                    menuItemEraser!!.setIcon(R.drawable.baseline_create_white_24)
                    penColor = Color.WHITE
                    penWidth = 50f
                }

                isEraser = !isEraser


            }
        }


        return true
    }

    @Throws(IOException::class)
    private fun saveBitmap(
        context: Context, bitmap: Bitmap,
        //format: Bitmap.CompressFormat, mimeType: String,
        displayName: String
    ): String {
        val path: String
        val format = Bitmap.CompressFormat.JPEG
        val mimeType = "image/jpeg"

        val relativeLocation = Environment.DIRECTORY_PICTURES
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation)
        }
        val resolver = context.contentResolver
        var stream: OutputStream? = null
        //val byteArrayOutputStream = ByteArrayOutputStream()

        var uri: Uri? = null
        try {
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            uri = resolver.insert(contentUri, contentValues)

            signImageUriPath = uri

            Log.e(mTAG, "Uri = $uri")
            path = getRealPathFromURI(context, uri) as String


            //val path2 = getRealPathFromURI2(context, uri)
            Log.e(mTAG, "path = $path")


            //Log.e(mTAG, "path2 = $path2")
            if (uri == null) {
                throw IOException("Failed to create new MediaStore record.")
            }
            stream = resolver.openOutputStream(uri)

            if (stream == null) {
                throw IOException("Failed to get output stream.")
            }
            val ret =bitmap.compress(format, 100, stream)
            if (!ret) {
                throw IOException("Failed to save bitmap.")
            }
        } catch (e: IOException) {
            if (uri != null) {
                resolver.delete(uri, null, null)
            }
            throw e
        } finally {
            stream?.close()
        }


        /*
        bitmap.compress(format, 100, byteArrayOutputStream)
        //scaledImage.compress(format, 100, byteArrayOutputStream)
        val imageByteArray = byteArrayOutputStream.toByteArray()

        Log.e(mTAG, "imageByteArray size = ${imageByteArray.size}")

        MainActivity.base64 = Base64.encodeToString(imageByteArray, Base64.DEFAULT)
        val decodeByeArray: ByteArray = Base64.decode(MainActivity.base64, Base64.DEFAULT)
        Log.e(mTAG, "base64 size = ${MainActivity.base64.length}")
        */

        return path
    }

    private fun deleteImage() {

        val resolver = signContext!!.contentResolver
        var ret = -1
        try {
            ret = resolver.delete(signImageUriPath as Uri, null, null)
        } catch (e: SecurityException ) {
            Log.e(mTAG, "exception: $e")
        }

        Log.e(mTAG, "ret = $ret")
    }

    private fun getRealPathFromURI(context: Context, contentUri: Uri?
    ): String? {

        var ret = ""

        if (contentUri != null) {
            ret = fileUtils!!.getPath(context, contentUri).toString()
        }
        /*var cursor: Cursor? = null
        return try {
            //val proj = arrayOf(MediaStore.Images.Media.DATA)
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
            val column_index: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }*/
        return ret
    }

    /*private fun getRealPathFromURI2(context: Context, contentUri: Uri?): String? {
        val cursor: Cursor?
        val columnIndexID: Int
        val listOfAllImages: MutableList<Uri> = mutableListOf()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        var imageId: Long
        var ret = ""
        cursor = context.contentResolver.query(contentUri as Uri, projection, null, null, null)
        if (cursor != null) {
            columnIndexID = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            ret = cursor.getString(columnIndexID)
            Log.e(mTAG, "getRealPathFromURI2 = $ret")

            cursor.close()
        }

        return ret
    }*/

    private fun showUploadSignatureDialog() {

        // get prompts.xml view
        /*LayoutInflater layoutInflater = LayoutInflater.from(Nfc_read_app.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);*/
        val promptView = View.inflate(this@SignActivity, R.layout.confirm_dialog, null)

        val alertDialogBuilder = AlertDialog.Builder(this@SignActivity).create()
        alertDialogBuilder.setView(promptView)

        //final EditText editFileName = (EditText) promptView.findViewById(R.id.editFileName);
        val textViewMsg = promptView.findViewById<TextView>(R.id.textViewDialog)
        val btnCancel = promptView.findViewById<Button>(R.id.btnDialogCancel)
        val btnConfirm = promptView.findViewById<Button>(R.id.btnDialogConfirm)


        textViewMsg.text = getString(R.string.draw_save_title)
        btnCancel.text = getString(R.string.cancel)
        btnConfirm.text = getString(R.string.confirm)


        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
        btnCancel!!.setOnClickListener {

            alertDialogBuilder.dismiss()
        }
        btnConfirm!!.setOnClickListener {



            if (!isSignMulti) {
                /*val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
            val currentDateAndTime: String = sdf.format(Date())
            uploadSignName = "$currentDateAndTime.jpg"*/
                //uploadSignName = "$sendOrder.jpg"


                //saveBitmap(drawContext as Context, paintBoard!!.bitmap, CompressFormat.JPEG,"image/jpeg", fileName)
                val scaledWidth = 320.0 //stick height to 512
                //val scaledHeight = 512.0 //stick height to 512
                val aspectRatio = scaledWidth / paintBoard!!.width

                val scaledHeight = paintBoard!!.height * aspectRatio

                Log.d(
                    mTAG,
                    "scaledWidth = $scaledWidth, aspectRatio = $aspectRatio, scaledHeight = $scaledHeight"
                )

                val scaledImage = Bitmap.createScaledBitmap(
                    paintBoard!!.bitmap,
                    scaledWidth.toInt(),
                    scaledHeight.toInt(),
                    false
                )

                var uploadSignName = ""
                if (signState == SignState.INITIAL) { //SignState.INITIAL, then upload driver
                    uploadSignName = sendOrder + "1.jpg"
                    uploadSignNameDriver = sendOrder + "1.jpg"
                } else if (signState == SignState.DRIVER_UPLOADED) { //SignState.DRIVER_UPLOADED, then upload guard
                    uploadSignName = sendOrder + "2.jpg"
                    uploadSignNameGuard = sendOrder + "2.jpg"
                }

                val path = saveBitmap(this@SignActivity as Context, scaledImage, uploadSignName)

                if (path != "") {
                    progressBar!!.visibility = View.VISIBLE
                    val ftpUtils = FTPUtils(
                        signContext as Context,
                        Constants.FtpInfo.IP_ADDRESS,
                        Constants.FtpInfo.PORT,
                        Constants.FtpInfo.SHIPMENT_USER,
                        Constants.FtpInfo.SHIPMENT_PASSWORD,
                        uploadSignName,
                        path
                    )
                    val coroutineFtp = Presenter(ftpUtils)
                    coroutineFtp.execute()
                    //val ftpTask = FtpTask()
                    //ftpTask.execute(ftpUtils)
                } else {
                    Log.e(mTAG, "Path = null")
                }
            } else { //sign multi
                //Sign multi
                progressBar!!.visibility = View.VISIBLE

                Log.e(mTAG, "sign multi, signatureMultiSignList.size = ${signatureMultiSignList.size}")

                if (signState == SignState.INITIAL) { //SignState.INITIAL, then upload driver
                    Log.e(mTAG, "SignState.INITIAL")
                    signatureMultiSignListDriver.clear()
                    for (item in signatureMultiSignList) {
                        signatureMultiSignListDriver.add(item)
                    }

                    val uploadStartIntent = Intent()
                    uploadStartIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_UPLOAD_START
                    signContext!!.sendBroadcast(uploadStartIntent)
                } else if (signState == SignState.DRIVER_UPLOADED) { //SignState.DRIVER_UPLOADED, then upload guard
                    Log.e(mTAG, "SignState.DRIVER_UPLOADED")
                    signatureMultiSignListGuard.clear()
                    for (item in signatureMultiSignList) {
                        signatureMultiSignListGuard.add(item)
                    }

                    val uploadStartIntent = Intent()
                    uploadStartIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_UPLOAD_START
                    signContext!!.sendBroadcast(uploadStartIntent)
                }

                /*for (i in 0..signatureMultiSignList.size) {
                    var uploadSignName = ""
                    if (signState == SignState.INITIAL) { //SignState.INITIAL, then upload driver
                        uploadSignName = signatureMultiSignList[i].getShipmentNo() + "1.jpg"
                        uploadSignNameDriver = signatureMultiSignList[i].getShipmentNo()  + "1.jpg"
                    } else if (signState == SignState.DRIVER_UPLOADED) { //SignState.DRIVER_UPLOADED, then upload guard
                        uploadSignName = signatureMultiSignList[i].getShipmentNo() + "2.jpg"
                        uploadSignNameGuard = signatureMultiSignList[i].getShipmentNo()  + "2.jpg"
                    }
                }*/

            }





            alertDialogBuilder.dismiss()
        }
        alertDialogBuilder.show()


    }

    private fun uploadMulti(shipmentNo: String) {
        val scaledWidth = 320.0 //stick height to 512
        //val scaledHeight = 512.0 //stick height to 512
        val aspectRatio = scaledWidth / paintBoard!!.width

        val scaledHeight = paintBoard!!.height * aspectRatio

        Log.d(
            mTAG,
            "scaledWidth = $scaledWidth, aspectRatio = $aspectRatio, scaledHeight = $scaledHeight"
        )

        val scaledImage = Bitmap.createScaledBitmap(
            paintBoard!!.bitmap,
            scaledWidth.toInt(),
            scaledHeight.toInt(),
            false
        )

        var uploadSignName = ""
        if (signState == SignState.INITIAL) { //SignState.INITIAL, then upload driver
            uploadSignName = shipmentNo + "1.jpg"
        } else if (signState == SignState.DRIVER_UPLOADED) { //SignState.DRIVER_UPLOADED, then upload guard
            uploadSignName = shipmentNo + "2.jpg"
        }

        val path = saveBitmap(this@SignActivity as Context, scaledImage, uploadSignName)

        if (path != "") {
            progressBar!!.visibility = View.VISIBLE
            val ftpUtils = FTPUtils(
                signContext as Context,
                Constants.FtpInfo.IP_ADDRESS,
                Constants.FtpInfo.PORT,
                Constants.FtpInfo.SHIPMENT_USER,
                Constants.FtpInfo.SHIPMENT_PASSWORD,
                uploadSignName,
                path
            )
            val coroutineFtp = Presenter(ftpUtils)
            coroutineFtp.execute()
            //val ftpTask = FtpTask()
            //ftpTask.execute(ftpUtils)
        } else {
            Log.e(mTAG, "Path = null")
        }
    }

    private fun toast(message: String) {

        if (toastHandle != null)
            toastHandle!!.cancel()

        val toast = Toast.makeText(signContext, HtmlCompat.fromHtml("<h1>$message</h1>", HtmlCompat.FROM_HTML_MODE_COMPACT), Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, 0)

        /*val toast = Toast.makeText(signContext, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, 0)
        val group = toast.view as ViewGroup
        val textView = group.getChildAt(0) as TextView
        textView.textSize = 30.0f*/
        toast.show()

        toastHandle = toast
    }

    private fun toastLong(message: String) {

        if (toastHandle != null)
            toastHandle!!.cancel()

        val toast = Toast.makeText(this, HtmlCompat.fromHtml("<h1>$message</h1>", HtmlCompat.FROM_HTML_MODE_COMPACT), Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, 0)
        /*val group = toast.view as ViewGroup
        val textView = group.getChildAt(0) as TextView
        textView.textSize = 30.0f*/
        toast.show()
        toastHandle = toast
    }

    //private class FtpTask : AsyncTask<FTPUtils, Void?, FTPClient>() {
    /*private class FtpTask : AsyncTask<FTPUtils, Void?, Context>() {

        override fun onPreExecute() {


            super.onPreExecute()
        }

        override fun onPostExecute(context: Context) {
            Log.v("FTPTask", "task complete")

            val completeIntent = Intent()
            completeIntent.action = Constants.ACTION.ACTION_OUTSOURCED_PROCESS_SIGN_FTP_UPLOAD_COMPLETE
            context.sendBroadcast(completeIntent)

            //Where ftpClient is a instance variable in the main activity

        }

        override fun doInBackground(vararg params: FTPUtils?): Context {
            if (params[0] != null) {
                params[0]!!.uploadFile()
            }

            return params[0]!!.mContext as Context
        }
    }*/

    private class Presenter(ftpUtils: FTPUtils) : CoroutineScope {
        private var job: Job = Job()

        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Main + job // to run code in Main(UI) Thread

        private var ftpUtils: FTPUtils ?= null
        private var isUploadSuccess = false
        init {
            this.ftpUtils =  ftpUtils
        }


        // call this method to cancel a coroutine when you don't need it anymore,
        // e.g. when user closes the screen

        /*fun cancel() {
            job.cancel()
        }*/

        fun execute() = launch {
            onPreExecute()
            //val result = doInBackground() // runs in background thread without blocking the Main Thread
            doInBackground() // runs in background thread without blocking the Main Thread
            onPostExecute()
        }

        private suspend fun doInBackground(): String = withContext(Dispatchers.IO) { // to run code in Background Thread
            // do async work

            isUploadSuccess = ftpUtils!!.uploadFile()
            delay(1000) // simulate async work
            return@withContext "SomeResult"
        }

        // Runs on the Main(UI) Thread
        private fun onPreExecute() {
            Log.e("FTPTask", "task start")
            // show progress
        }

        // Runs on the Main(UI) Thread
        //private fun onPostExecute(result: String) {
        private fun onPostExecute() {
            // hide progress
            Log.e("FTPTask", "task complete, isUploadSuccess = $isUploadSuccess")

            if (isUploadSuccess) {

                if (!isSignMulti) {
                    if (signState == SignState.INITIAL) {
                        val outsourcedCompleteIntent = Intent()
                        outsourcedCompleteIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_UPLOAD_COMPLETE
                        ftpUtils!!.mContext!!.sendBroadcast(outsourcedCompleteIntent)
                    } else if (signState == SignState.DRIVER_UPLOADED) {
                        val outsourcedCompleteIntent = Intent()
                        outsourcedCompleteIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_UPLOAD_COMPLETE
                        ftpUtils!!.mContext!!.sendBroadcast(outsourcedCompleteIntent)
                    }
                } else {
                    if (signState == SignState.INITIAL) {
                        val outsourcedCompleteIntent = Intent()
                        outsourcedCompleteIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_UPLOAD_COMPLETE
                        ftpUtils!!.mContext!!.sendBroadcast(outsourcedCompleteIntent)
                    } else if (signState == SignState.DRIVER_UPLOADED) {
                        val outsourcedCompleteIntent = Intent()
                        outsourcedCompleteIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_UPLOAD_COMPLETE
                        ftpUtils!!.mContext!!.sendBroadcast(outsourcedCompleteIntent)
                    }
                }




            }

        }
    }
}