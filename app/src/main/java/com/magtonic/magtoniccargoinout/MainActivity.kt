package com.magtonic.magtoniccargoinout

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog

import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color

import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.*

import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import com.magtonic.magtoniccargoinout.api.ApiFunc
import com.magtonic.magtoniccargoinout.model.item.ItemGuest
import com.magtonic.magtoniccargoinout.model.item.ItemReceipt
import com.magtonic.magtoniccargoinout.model.receive.*
import com.magtonic.magtoniccargoinout.model.send.*
import com.magtonic.magtoniccargoinout.model.sys.ScanBarcode
import com.magtonic.magtoniccargoinout.persistence.HistoryDataBase
import com.magtonic.magtoniccargoinout.ui.data.Constants
import com.magtonic.magtoniccargoinout.ui.data.ShipmentSignatureMultiItem
import com.magtonic.magtoniccargoinout.ui.home.HomeFragment

import com.magtonic.magtoniccargoinout.ui.shipment.ShipmentCheckFragment
import com.magtonic.magtoniccargoinout.ui.signature.ShipmentSignatureFragment
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val mTAG = MainActivity::class.java.name
    private val requestIdMultiplePermission = 1

    enum class CurrentFragment {
        HOME_FRAGMENT, SHIPMENT_CHECK_FRAGMENT, SHIPMENT_SIGNATURE_FRAGMENT
    }

    var currentFrag: CurrentFragment = CurrentFragment.HOME_FRAGMENT

    enum class SignState {
        INITIAL,
        DRIVER_UPLOADED,
        GUARD_UPLOADED,
        DRIVER_CONFIRM,
        GUARD_CONFIRM

    }

    var pref: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    private val fileName = "Preference"

    //private var mContext: Context? = null

    private var imm: InputMethodManager? = null

    private var mReceiver: BroadcastReceiver? = null
    private var isRegister = false

    private var menuItemKeyboard: MenuItem? = null
    private var menuItemShipmentHistory: MenuItem? = null

    @SuppressLint("StaticFieldLeak")
    companion object {
        @JvmStatic var screenWidth: Int = 0
        @JvmStatic var screenHeight: Int = 0
        @JvmStatic var itemReceipt: ItemReceipt? = null //for receipt
        @JvmStatic var isKeyBoardShow: Boolean = false
        @JvmStatic var isWifiConnected: Boolean = false
        @JvmStatic var currentSSID: String = ""

        //guest
        @JvmStatic var guestListT: ArrayList<RJGuest> = ArrayList()
        @JvmStatic var guestListA: ArrayList<RJGuest> = ArrayList()
        @JvmStatic var guestListB: ArrayList<RJGuest> = ArrayList()
        @JvmStatic var currentPlant: String = "T"

        //timeout
        @JvmStatic var timeOutSeconds: Long = 60

        //shipment check
        @JvmStatic var shipmentList: ArrayList<RJShipment> = ArrayList()
        @JvmStatic var signatureList: ArrayList<RJSignature> = ArrayList()
        @JvmStatic var signatureDetailList: ArrayList<RJSignatureDetail> = ArrayList()
        @JvmStatic var signatureMultiSignList: ArrayList<ShipmentSignatureMultiItem> = ArrayList()
        @JvmStatic var currentShipmentNo: String = ""
        @JvmStatic var isShipmentSignatureInDetail: Int = 0
        @JvmStatic var isSignMulti: Boolean = false

        @JvmStatic var isEraser: Boolean = false
        @JvmStatic var penColor: Int = Color.BLACK
        @JvmStatic var penWidth: Float = 10f

        @JvmStatic var base64: String = ""

        @JvmStatic var signState: SignState = SignState.INITIAL
        @JvmStatic var currentDate: String = ""
        //for change ip
        @JvmStatic var base_ip_address: String = ""
        @JvmStatic var iep_ip_address: String = ""
        @JvmStatic var ftp_ip_address: String = ""
        @JvmStatic var base_ip_address_webservice: String = "http://$base_ip_address/asmx/webservice.asmx/"
        @JvmStatic var iep_ip_address_webservice: String = "http://$iep_ip_address/webs.asmx/"

        var db: HistoryDataBase? = null
        //@JvmStatic var historyList: ArrayList<History>? = null
        //timer
        private var handler: MyHandler? = null
        //private var guestContext: Context? = null
        private var mContext: Context? = null

        private var mTimer: Timer? = null
        private var mTimerTask //計時任務，判斷是否未操作時間到達3s
                : MyTimerTask? = null
        private var mLastActionTime //上一次操作時間
                : Long = 0


        //form In&Out timer
        private class MyTimerTask : TimerTask() {
            override fun run() { //3s未操作,則結束計時
                if (System.currentTimeMillis() - mLastActionTime > 60000) { //結束計時

                    // 停止計時任務

                    mLastActionTime = System.currentTimeMillis()

                    val getIntent = Intent()
                    getIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_LIST_ACTION
                    mContext!!.sendBroadcast(getIntent)

                } else {
                    System.currentTimeMillis() - mLastActionTime
                    //Log.e("=====", "${(System.currentTimeMillis() - mLastActionTime)/1000}")
                }
            }
        }

        //private class MyHandler(context: Context) : Handler() {
        private class MyHandler : Handler(Looper.getMainLooper()) {
            //private var mFragment: WeakReference<Context> = WeakReference(context)

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what == 1) { //回到主執行緒執行結束操作
                    //Log.e("=====", "結束計時")

                }
            }
        }

        private fun startTimer() {
            mTimer = Timer(true)
            mTimerTask = MyTimerTask()
            mTimer!!.schedule(mTimerTask, 0, 1000) //延時1000ms後執行，1000ms檢查一次
            // 初始化上次操作時間為登入成功的時間
            mLastActionTime = System.currentTimeMillis()
        }

        //倒計時完畢的操作
        private fun removeTimer() {
            val message = Message()
            message.what = 1
            handler!!.sendMessage(message)
        }

        // 停止計時任務
        private fun stopTimer() {
            if (mTimer != null)
                mTimer!!.cancel()
            mTimer = null
        }
    }

    var barcode: ScanBarcode? = null

    private var navView: NavigationView? = null

    private var toastHandle: Toast? = null

    private var isBarcodeScanning: Boolean = false
    private var currentSearchPlant: String = "T"

    var fabBack: FloatingActionButton? = null
    var fabSign: FloatingActionButton? = null

    var fabAddToMultiSign: FloatingActionButton? = null
    var fabMultiSignList: FloatingActionButton? = null

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mContext = applicationContext

        FirebaseApp.initializeApp(this)

        //disable Scan2Key Setting
        val disableServiceIntent = Intent()
        disableServiceIntent.action = "unitech.scanservice.scan2key_setting"
        disableServiceIntent.putExtra("scan2key", false)
        sendBroadcast(disableServiceIntent)

        Log.d(mTAG, "onCreate")

        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M)
        {
            windowManager.defaultDisplay.getMetrics(displayMetrics)
        } else {
            //mContext!!.display!!.getMetrics(displayMetrics)
            this@MainActivity.display!!.getRealMetrics(displayMetrics)
        }
        screenHeight = displayMetrics.heightPixels
        screenWidth = displayMetrics.widthPixels

        Log.e(mTAG, "width = $screenWidth, height = $screenHeight")

        pref = getSharedPreferences(fileName, Context.MODE_PRIVATE)

        // guest read current plant
        currentPlant = pref!!.getString("CURRENT_PLANT", "T") as String
        base_ip_address = pref!!.getString("BASE_IP_ADDRESS", "192.1.1.50") as String
        iep_ip_address = pref!!.getString("IEP_IP_ADDRESS", "192.1.1.121") as String
        ftp_ip_address = pref!!.getString("FTP_IP_ADDRESS", "192.1.1.121") as String

        base_ip_address_webservice = "http://$base_ip_address/asmx/webservice.asmx/"
        iep_ip_address_webservice = "http://$iep_ip_address/webs.asmx/"


        val migration12 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //database.execSQL("ALTER TABLE '"+History.TABLE_NAME+"' ADD COLUMN 'timeStamp' LONG NOT NULL DEFAULT 0")
            }
        }

        //load db
        db = Room.databaseBuilder(mContext as Context, HistoryDataBase::class.java, HistoryDataBase.DATABASE_NAME)
            .allowMainThreadQueries()
            .addMigrations(migration12)
            .build()

        //for timer
        handler = MyHandler()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        //get virtual keyboard
        imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        Log.e(mTAG, "navView header: "+navView!!.headerCount)
        val header = navView!!.inflateHeaderView(R.layout.nav_header_main)
        //textViewUserName = header.findViewById(R.id.textViewUserName)
        Log.e(mTAG, "navView header: "+navView!!.headerCount)
        navView!!.removeHeaderView(navView!!.getHeaderView(0))

        val mDrawerToggle = object : ActionBarDrawerToggle(
            this, /* host Activity */
            drawerLayout, /* DrawerLayout object */
            toolbar, /* nav drawer icon to replace 'Up' caret */
            R.string.navigation_drawer_open, /* "open drawer" description */
            R.string.navigation_drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state.  */

            override fun onDrawerClosed(view: View) {
                super.onDrawerClosed(view)

                Log.d(mTAG, "onDrawerClosed")

            }

            /** Called when a drawer has settled in a completely open state.  */
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)

                Log.d(mTAG, "onDrawerOpened")

                if (isKeyBoardShow) {
                    imm?.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0)
                }
            }
        }

        drawerLayout.addDrawerListener(mDrawerToggle)
        mDrawerToggle.syncState()

        navView!!.setNavigationItemSelectedListener(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions()
        } else {
            initView()
            //initLog()
        }

        fabBack = findViewById(R.id.fabBack)
        fabBack!!.setOnClickListener {
            Log.d(mTAG, "===> fabBack")

            when (currentFrag) {

                CurrentFragment.SHIPMENT_SIGNATURE_FRAGMENT -> {
                    if (isShipmentSignatureInDetail == 1) {

                        fabAddToMultiSign!!.visibility = View.VISIBLE
                        fabMultiSignList!!.visibility = View.VISIBLE

                        fabBack!!.visibility = View.GONE
                        fabSign!!.visibility = View.GONE

                        isShipmentSignatureInDetail = 0

                        val backIntent = Intent()
                        backIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_BACK_TO_SHIPMENT_NO_LIST
                        sendBroadcast(backIntent)
                    }


                }

                else -> {
                    Log.e(mTAG, "Unknown fragment")
                }
            }
        }

        fabSign = findViewById(R.id.fabSign)
        fabSign!!.setOnClickListener {
            val showIntent = Intent()
            when (currentFrag) {
                CurrentFragment.SHIPMENT_SIGNATURE_FRAGMENT -> {
                    showIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SHOW_SIGN_DIALOG_ACTION
                }
                else -> {
                    Log.e(mTAG, "Unknown Fragment")
                }
            }

            sendBroadcast(showIntent)

        }

        fabAddToMultiSign = findViewById(R.id.fabAddToMultiSign)
        fabAddToMultiSign!!.setOnClickListener {
            val showIntent = Intent()
            when (currentFrag) {
                CurrentFragment.SHIPMENT_SIGNATURE_FRAGMENT -> {
                    showIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_ADD_SHIPMENT_NO_TO_MULTI_ACTION
                }
                else -> {
                    Log.e(mTAG, "Unknown Fragment")
                }
            }

            sendBroadcast(showIntent)
        }

        fabMultiSignList = findViewById(R.id.fabMultiSignList)
        fabMultiSignList!!.setOnClickListener {

            if (signatureMultiSignList.size > 0) {
                val showIntent = Intent()
                when (currentFrag) {
                    CurrentFragment.SHIPMENT_SIGNATURE_FRAGMENT -> {
                        showIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SHOW_SIGN_MULTI_LIST
                    }
                    else -> {
                        Log.e(mTAG, "Unknown Fragment")
                    }
                }

                sendBroadcast(showIntent)
            } else {
                toastLong(getString(R.string.shipment_signature_multi_list_empty))
            }


        }
        //load from db
        /*if (db != null) {
            if (historyList != null) {
                historyList!!.clear()
            } else {
                historyList = ArrayList()
            }

            /*val tempList = db!!.historyDao().getAll() as ArrayList<History>

            for (i in tempList.size - 1  downTo 0) {
                historyList!!.add(tempList[i])
            }*/

            historyList = db!!.historyDao().getAll() as ArrayList<History>

            if (historyList!!.size > 1 ) {
                historyList = historyList!!.sortedBy { it.getId() }.reversed() as ArrayList<History>
            }



            Log.e(mTAG, "historyList.size = "+historyList!!.size)

            for (i in 0 until historyList!!.size) {
                Log.e(mTAG, "historyList[$i] = "+ historyList!![i].getId() +
                        " Barcode = "+ historyList!![i].getBarcode()+
                        " WorkOrderState = "+ historyList!![i].getWorkOrderState()+
                        " WorkOrderDesc = "+ historyList!![i].getWorkOrderDesc()+
                        " ShipmentState = "+ historyList!![i].getShipmentState()+
                        " ShipmentDesc = "+ historyList!![i].getShipmentDesc()+
                        " DateTime = "+ historyList!![i].getDatetime()+
                        " Date = "+ historyList!![i].getDate()
                )
            }

        } else {
            Log.e(mTAG, "db = null")
        }*/

        val filter: IntentFilter
        @SuppressLint("CommitPrefEdits")
        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != null) {
                    if (intent.action!!.equals(Constants.ACTION.ACTION_HIDE_KEYBOARD, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_HIDE_KEYBOARD")

                        imm?.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0)
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_USER_INPUT_SEARCH, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_USER_INPUT_SEARCH")

                        imm?.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0)

                        val inputNo = intent.getStringExtra("INPUT_NO")

                        Log.e(mTAG, "inputNo = $inputNo")
                        //clear
                        //ReceiptList.removeAllItem()
                        //rva06 = ""
                        //et_Barcode.setText(text)
                        itemReceipt = null

                        if (inputNo != null) {
                            barcode = ScanBarcode.setPoBarcodeByScanTransform(inputNo.trim())

                            Log.e(mTAG, "barcode = $barcode")

                            if (barcode != null) {

                                getReceipt(barcode)

                            } else {
                                Log.e(mTAG, "barcode = null")
                                val sendIntent = Intent()
                                sendIntent.action = Constants.ACTION.ACTION_BARCODE_NULL
                                sendBroadcast(sendIntent)
                            }
                        } else {
                            Log.e(mTAG, "inputNo = null")
                        }


                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GUEST_START_TIMER, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_GUEST_START_TIMER")

                        startTimer()

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GUEST_STOP_TIMER, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_GUEST_STOP_TIMER")

                        removeTimer()
                        stopTimer()

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GUEST_GET_CURRENT_PLANT_GUEST_LIST, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_GUEST_GET_CURRENT_PLANT_GUEST_LIST")



                        val plant = intent.getStringExtra("PLANT")

                        Log.e(mTAG, "plant = $plant")

                        when(plant) {
                            "A" -> guestListA.clear()
                            "B" -> guestListB.clear()
                            else -> guestListT.clear()
                        }

                        getGuestMulti(plant as String)
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GUEST_GET_CURRENT_PLANT_GUEST_SUCCESS, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_GUEST_GET_CURRENT_PLANT_GUEST_SUCCESS")

                        val successIntent = Intent()
                        successIntent.action = Constants.ACTION.ACTION_GUEST_FRAGMENT_REFRESH
                        mContext!!.sendBroadcast(successIntent)

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GUEST_GET_CURRENT_PLANT_GUEST_FAILED, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_GUEST_GET_CURRENT_PLANT_GUEST_FAILED")

                        isBarcodeScanning = false
                        val noExistIntent = Intent()
                        noExistIntent.action = Constants.ACTION.ACTION_GUEST_LIST_CLEAR
                        mContext!!.sendBroadcast(noExistIntent)


                        val errorString = intent.getStringExtra("result2")
                        toast(errorString as String)
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GUEST_IN_OR_LEAVE_ACTION, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_GUEST_IN_OR_LEAVE_ACTION")

                        val inOrOut = intent.getStringExtra("DATA1")
                        val plant = intent.getStringExtra("DATA2")
                        val guestNo = intent.getStringExtra("DATA3")
                        val pmn01 = intent.getStringExtra("DATA4")
                        val pmn02 = intent.getStringExtra("DATA5")
                        val inDate = intent.getStringExtra("DATA6")

                        guestInOrOutMulti(inOrOut as String, plant as String, guestNo as String, pmn01 as String, pmn02 as String, inDate as String)

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_LIST_ACTION, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_GUEST_SEARCH_GUEST_LIST_ACTION")

                        //save current plant
                        editor = pref!!.edit()
                        editor!!.putString("CURRENT_PLANT", currentPlant)
                        editor!!.apply()

                        //start from T
                        //currentSearchPlant = "T"
                        currentSearchPlant = currentPlant
                        getGuestMulti(currentPlant)

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_NEXT_ACTION, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_GUEST_SEARCH_GUEST_NEXT_ACTION")

                        when(currentSearchPlant) {
                            "T" -> currentSearchPlant = "A"
                            "A" -> currentSearchPlant = "B"
                        }

                        getGuestMulti(currentSearchPlant)
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_COMPLETE, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_GUEST_SEARCH_GUEST_COMPLETE")

                        val successIntent = Intent()
                        successIntent.action = Constants.ACTION.ACTION_GUEST_FRAGMENT_REFRESH
                        mContext!!.sendBroadcast(successIntent)

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_CHECK_ACTION, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_CHECK_ACTION")

                        val inputNo = intent.getStringExtra("INPUT_NO")

                        Log.e(mTAG, "inputNo = $inputNo")

                        if (inputNo != null) {

                            try {
                                barcode = ScanBarcode.setPoBarcodeByScanTransform(inputNo.trim())

                                Log.e(mTAG, "barcode = $barcode")

                                if (barcode != null) {


                                    val firstFourStr = inputNo.substring(0, 4)
                                    Log.e(mTAG, "firstFourStr = $firstFourStr")

                                    if (firstFourStr == "AX30") {
                                        getShipmentCheckMulti(barcode)
                                    } else {
                                        val scanIntent = Intent()
                                        scanIntent.action = Constants.ACTION.ACTION_SHIPMENT_FRAGMENT_NOT_COMPLETE_BIKE
                                        sendBroadcast(scanIntent)
                                    }


                                    //getShipmentCheckMulti(barcode)
                                    /*when (currentFrag) {
                                        CurrentFragment.RECEIPT_FRAGMENT -> {
                                            //checkIfReceiptUploaded(barcode)
                                            getReceipt(barcode)
                                        }
                                        CurrentFragment.STORAGE_FRAGMENT -> {
                                            getStorage(barcode)
                                        }
                                        CurrentFragment.MATERIAL_ISSUING_FRAGMENT -> {
                                            getMaterial(barcode)
                                        }
                                        CurrentFragment.PROPERTY_FRAGMENT -> {
                                            getProperty(barcode)
                                        }
                                        CurrentFragment.HOME_FRAGMENT -> {

                                        }
                                        CurrentFragment.GUEST_FRAGMENT -> {
                                            getReceipt(barcode)
                                        }
                                        CurrentFragment.OUTSOURCED_FRAGMENT -> {
                                            getOutSourcedProcessBySupplierNo(inputNo)
                                        }
                                        CurrentFragment.ISSUANCE_LOOKUP_FRAGMENT -> {
                                            getIssuanceLookup(inputNo)
                                        }

                                        else -> {
                                            Log.e(mTAG, "Unknown fragment")
                                        }
                                    }*/
                                } else {
                                    Log.e(mTAG, "barcode = null")
                                    val sendIntent = Intent()
                                    sendIntent.action = Constants.ACTION.ACTION_BARCODE_NULL
                                    sendBroadcast(sendIntent)
                                }
                            } catch (ex: NumberFormatException) {
                                ex.printStackTrace()
                            }


                        } else {
                            Log.e(mTAG, "inputNo = null")
                        }

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SEARCH_NO_EMPTY, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_SEARCH_NO_EMPTY")

                        toast(getString(R.string.shipment_input_no_empty))

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SEARCH_NO_ACTION, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_SEARCH_NO_ACTION")

                        fabBack!!.visibility = View.GONE
                        fabSign!!.visibility = View.GONE

                        fabAddToMultiSign!!.visibility = View.GONE
                        fabMultiSignList!!.visibility = View.GONE

                        val inputDate = intent.getStringExtra("INPUT_DATE")
                        val inputNo = intent.getStringExtra("INPUT_NO")

                        Log.e(mTAG, "inputDate = $inputDate, inputNo = $inputNo")

                        getShipmentSignatureMulti(inputDate as String, inputNo as String)

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SEARCH_DETAIL_ACTION, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_SEARCH_DETAIL_ACTION")

                        val shipmentNo = intent.getStringExtra("SHIPMENT_NO")

                        Log.e(mTAG, "shipmentNo = $shipmentNo")

                        getShipmentSignatureDetail(shipmentNo as String)

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_HIDE_FAB_BACK, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_HIDE_FAB_BACK")

                        fabSign!!.visibility = View.GONE
                        fabBack!!.visibility = View.GONE

                        if (signatureList.size > 0) {
                            fabAddToMultiSign!!.visibility = View.VISIBLE
                            fabMultiSignList!!.visibility = View.VISIBLE
                        }

                        isShipmentSignatureInDetail = 0
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_SIGN_CONFIRM_ACTION, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_DRIVER_SIGN_UPLOAD_ACTION")

                        val sendOrder = intent.getStringExtra("SEND_ORDER")
                        val uploadSignFileName = intent.getStringExtra("SIGN_FILE_NAME")

                        currentShipmentNo = sendOrder as String

                        confirmShipmentSignature(sendOrder , uploadSignFileName as  String, "tiptop", "1")
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_SIGN_CONFIRM_ACTION, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_GUARD_SIGN_CONFIRM_ACTION")

                        val sendOrder = intent.getStringExtra("SEND_ORDER")
                        val uploadSignFileName = intent.getStringExtra("SIGN_FILE_NAME")

                        currentShipmentNo = sendOrder as String

                        confirmShipmentSignature(sendOrder , uploadSignFileName as  String, "tiptop", "2")
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_ADD_SHIPMENT_NO_TO_MULTI_SUCCESS, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_ADD_SHIPMENT_NO_TO_MULTI_SUCCESS")

                        toast(getString(R.string.shipment_no_multi_sign_add))
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_ADD_SHIPMENT_NO_TO_MULTI_EXIST, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_ADD_SHIPMENT_NO_TO_MULTI_EXIST")

                        toast(getString(R.string.shipment_no_multi_sign_exist))
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SHOW_SIGN_MULTI_LIST, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_SHOW_SIGN_MULTI_LIST")

                        val intent = Intent(mContext, SignMultiActivity::class.java)
                        //intent.putExtra("SEND_ORDER", currentSendOrder)
                        //intent.putExtra("TITLE", getString(R.string.nav_signature))
                        //intent.putExtra("SEND_FRAGMENT", "SHIPMENT_SIGNATURE_FRAGMENT")
                        startActivity(intent)
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_WEBSERVICE_FTP_IP_ADDRESS_UPDATE_ACTION, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_WEBSERVICE_FTP_IP_ADDRESS_UPDATE_ACTION")

                        base_ip_address_webservice = "http://$base_ip_address/asmx/webservice.asmx/"
                        iep_ip_address_webservice = "http://$iep_ip_address/webs.asmx/"

                        editor = pref!!.edit()
                        editor!!.putString("BASE_IP_ADDRESS", base_ip_address)
                        editor!!.putString("IEP_IP_ADDRESS", iep_ip_address)
                        editor!!.putString("FTP_IP_ADDRESS", ftp_ip_address)
                        editor!!.apply()
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_WEBSERVICE_FTP_IP_ADDRESS_SHOW_PASSWORD_DIALOG, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_WEBSERVICE_FTP_IP_ADDRESS_SHOW_PASSWORD_DIALOG")

                        showIPSettingPasswordDialog()
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_SIGN_CONFIRM_ACTION, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_SIGN_CONFIRM_ACTION")

                        val shipmentNo = intent.getStringExtra("SHIPMENT_NO")
                        val uploadSignFileName = intent.getStringExtra("SIGN_FILE_NAME")

                        confirmShipmentSignature(shipmentNo as String , uploadSignFileName as String, "tiptop", "1")
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_ACTION, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_ACTION")

                        val shipmentNo = intent.getStringExtra("SHIPMENT_NO")
                        val uploadSignFileName = intent.getStringExtra("SIGN_FILE_NAME")

                        confirmShipmentSignature(shipmentNo as String , uploadSignFileName as String, "tiptop", "2")
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_SIGN_CONFIRM_COMPLETE, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_SIGN_CONFIRM_COMPLETE")

                        signatureMultiSignList.clear()

                        toastLong(getString(R.string.shipment_signature_confirm_success))
                    }
                }

                //detect wifi
                if ("android.net.wifi.STATE_CHANGE" == intent.action) {
                    Log.e(mTAG, "Wifi STATE_CHANGE")

                    //val info: NetworkInfo? = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)

                    val wifiMgr = mContext!!.getSystemService(Context.WIFI_SERVICE) as WifiManager

                    //if (info!!.isConnected) {
                    if (wifiMgr.isWifiEnabled) {

                        val wifiInfo: WifiInfo = wifiMgr.connectionInfo
                        if (wifiInfo.networkId == -1) {
                            Log.d(mTAG, "Not connected to an access point")// Not connected to an access point
                            //fabWifi!!.visibility = View.VISIBLE

                            isWifiConnected = false
                            currentSSID = ""
                            Log.e(mTAG, "info ===> not connected ")
                            //toast(getString(R.string.wifi_state_disconnected))
                        } else {
                            isWifiConnected = true

                            currentSSID = wifiInfo.ssid

                            Log.e(mTAG, "currentSSID = $currentSSID")
                            //toast(getString(R.string.wifi_state_connected, currentSSID))
                            //Log.d(mTAG, "Connected to ${wifiInfo.ssid}")// Not connected to an access point
                            //fabWifi!!.visibility = View.GONE
                        }

                        /*isWifiConnected = true
                        Log.e(mTAG, "info ===> connected ")
                        val wifiManager: WifiManager = mContext!!.getSystemService(Context.WIFI_SERVICE) as WifiManager
                        val wifiInfo = wifiManager.connectionInfo

                        val rssi = wifiInfo.rssi
                        val level = WifiManager.calculateSignalLevel(rssi, 10)
                        val percentage = (level / 10.0 * 100).toInt()

                        Log.e(mTAG, "rssi = $rssi, level = %$level, percentage = $percentage")

                        currentSSID = wifiInfo.ssid*/



                    } else {
                        //show wifi

                        isWifiConnected = false
                        currentSSID = ""
                        Log.e(mTAG, "info ===> not connected ")

                    }

                    val changeIntent = Intent()
                    changeIntent.action = Constants.ACTION.ACTION_WIFI_STATE_CHANGED
                    sendBroadcast(changeIntent)

                }

                if ("android.net.wifi.WIFI_STATE_CHANGED" == intent.action) {
                    Log.e(mTAG, "Wifi WIFI_STATE_CHANGED")

                    //val wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
                }

                if ("unitech.scanservice.data" == intent.action) {
                    val bundle = intent.extras
                    if (bundle != null) {

                        if (isWifiConnected) {
                            //detect if is scanning or not
                            if (!isBarcodeScanning) {
                                isBarcodeScanning = true

                                val text = bundle.getString("text")
                                Log.d(mTAG, "text = " + text!!)
                                //showMyToast(text, ReceiptActivity.this);

                                //check barcode AP or AX
                                if (text.isNotEmpty() && text.length > 2) {
                                    val firstTwoStr = text.substring(0, 2)
                                    Log.e(mTAG, "firstTwoStr = $firstTwoStr")

                                    when (currentFrag) {
                                        CurrentFragment.HOME_FRAGMENT-> {

                                            //go shipment check
                                            if (firstTwoStr == "AX") {
                                                title = getString(R.string.nav_shipment_check)
                                                //start new fragment
                                                //start with receipt fragment
                                                var fragment: Fragment? = null
                                                val fragmentClass = ShipmentCheckFragment::class.java

                                                try {
                                                    fragment = fragmentClass.newInstance()
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }

                                                val fragmentManager = supportFragmentManager
                                                //fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
                                                fragmentManager.beginTransaction().replace(R.id.flContent, fragment!!).commitAllowingStateLoss()

                                                currentFrag = CurrentFragment.SHIPMENT_CHECK_FRAGMENT
                                            }
                                        }

                                        CurrentFragment.SHIPMENT_CHECK_FRAGMENT -> {
                                            if (firstTwoStr == "AP") {
                                                title = getString(R.string.nav_guest)
                                                //start new fragment
                                                //start with receipt fragment
                                                var fragment: Fragment? = null
                                                val fragmentClass = HomeFragment::class.java

                                                try {
                                                    fragment = fragmentClass.newInstance()
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }

                                                val fragmentManager = supportFragmentManager
                                                //fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
                                                fragmentManager.beginTransaction().replace(R.id.flContent, fragment!!).commitAllowingStateLoss()

                                                currentFrag = CurrentFragment.HOME_FRAGMENT
                                            }
                                        }

                                        CurrentFragment.SHIPMENT_SIGNATURE_FRAGMENT -> {

                                        }
                                    }
                                }

                                //clear
                                //ReceiptList.removeAllItem()
                                //rva06 = ""
                                itemReceipt = null

                                //et_Barcode.setText(text)

                                barcode = ScanBarcode.setPoBarcodeByScanTransform(text.toString().trim())

                                if (isWifiConnected) {

                                    when (currentFrag) {
                                        CurrentFragment.HOME_FRAGMENT-> {
                                            val scanIntent = Intent()
                                            scanIntent.action = Constants.ACTION.ACTION_GUEST_SCAN_BARCODE
                                            scanIntent.putExtra("BARCODE_BY_SCAN", barcode!!.poBarcodeByScan)
                                            scanIntent.putExtra("BARCODE", barcode!!.poBarcode)
                                            scanIntent.putExtra("LINE", barcode!!.poLine)
                                            sendBroadcast(scanIntent)
                                            getReceipt(barcode)
                                        }

                                        CurrentFragment.SHIPMENT_CHECK_FRAGMENT-> {

                                            val firstFourStr = text.substring(0, 4)
                                            Log.e(mTAG, "firstFourStr = $firstFourStr")

                                            if (firstFourStr == "AX30") {
                                                val scanIntent = Intent()
                                                scanIntent.action = Constants.ACTION.ACTION_SHIPMENT_SCAN_BARCODE
                                                scanIntent.putExtra("BARCODE", barcode!!.poBarcodeByScan)
                                                scanIntent.putExtra("LINE", barcode!!.poLine)
                                                sendBroadcast(scanIntent)
                                                getShipmentCheckMulti(barcode)
                                            } else {
                                                val scanIntent = Intent()
                                                scanIntent.action = Constants.ACTION.ACTION_SHIPMENT_FRAGMENT_NOT_COMPLETE_BIKE
                                                sendBroadcast(scanIntent)
                                                isBarcodeScanning = false
                                            }


                                        }

                                        CurrentFragment.SHIPMENT_SIGNATURE_FRAGMENT -> {

                                            Log.e(mTAG, "poBarcodeByScan = ${barcode!!.poBarcodeByScan}")

                                            fabBack!!.visibility = View.GONE
                                            fabSign!!.visibility = View.GONE

                                            fabAddToMultiSign!!.visibility = View.GONE
                                            fabMultiSignList!!.visibility = View.GONE

                                            val scanIntent = Intent()
                                            scanIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SCAN_BARCODE
                                            scanIntent.putExtra("BARCODE", barcode!!.poBarcodeByScan)
                                            sendBroadcast(scanIntent)

                                            getShipmentSignatureMulti(currentDate, barcode!!.poBarcodeByScan)

                                        }
                                    }


                                } else {
                                    toast(getString(R.string.get_or_send_failed_wifi_is_not_connected))
                                    isBarcodeScanning = false
                                }
                            } else {
                                Log.e(mTAG, "isBarcodeScanning = true")
                                toast(getString(R.string.barcode_scanning_get_info))
                            }
                        } else {
                            Log.e(mTAG, "Wifi is not connected. Barcode scan is useless.")
                            toast(getString(R.string.barcode_scan_off_because_wifi_is_not_connected))
                        }
                    }
                }
                if ("unitech.scanservice.datatype" == intent.action) {
                    val bundle = intent.extras
                    if (bundle != null) {
                        val type = bundle.getInt("text")

                        Log.d(mTAG, "type = $type")

                    }
                }
            }
        }


        if (!isRegister) {
            filter = IntentFilter()
            //keyboard
            filter.addAction(Constants.ACTION.ACTION_HIDE_KEYBOARD)
            //receipt
            filter.addAction(Constants.ACTION.ACTION_USER_INPUT_SEARCH)
            //timer
            filter.addAction(Constants.ACTION.ACTION_GUEST_START_TIMER)
            filter.addAction(Constants.ACTION.ACTION_GUEST_STOP_TIMER)
            //guest
            filter.addAction(Constants.ACTION.ACTION_GUEST_GET_CURRENT_PLANT_GUEST_LIST)
            filter.addAction(Constants.ACTION.ACTION_GUEST_GET_CURRENT_PLANT_GUEST_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_GUEST_GET_CURRENT_PLANT_GUEST_FAILED)
            filter.addAction(Constants.ACTION.ACTION_GUEST_IN_OR_LEAVE_ACTION)
            filter.addAction(Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_LIST_ACTION)
            filter.addAction(Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_NEXT_ACTION)
            filter.addAction(Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_COMPLETE)
            //shipment
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_CHECK_ACTION)
            //signature
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SEARCH_NO_EMPTY)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SEARCH_NO_ACTION)
            //signature detail
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SEARCH_DETAIL_ACTION)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_HIDE_FAB_BACK)
            //sign upload confirm
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_SIGN_CONFIRM_ACTION)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_SIGN_CONFIRM_ACTION)
            //sign multi
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_ADD_SHIPMENT_NO_TO_MULTI_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_ADD_SHIPMENT_NO_TO_MULTI_EXIST)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SHOW_SIGN_MULTI_LIST)
            //sign multi driver confirm
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_SIGN_CONFIRM_ACTION)
            //sign multi guard confirm
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_ACTION)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_SIGN_CONFIRM_COMPLETE)
            //ip setting
            filter.addAction(Constants.ACTION.ACTION_WEBSERVICE_FTP_IP_ADDRESS_UPDATE_ACTION)
            filter.addAction(Constants.ACTION.ACTION_WEBSERVICE_FTP_IP_ADDRESS_SHOW_PASSWORD_DIALOG)

            filter.addAction("android.net.wifi.STATE_CHANGE")
            filter.addAction("android.net.wifi.WIFI_STATE_CHANGED")
            filter.addAction("unitech.scanservice.data")
            filter.addAction("unitech.scanservice.datatype")
            mContext!!.registerReceiver(mReceiver, filter)
            isRegister = true
            Log.d(mTAG, "registerReceiver mReceiver")
        }

        //timer
        mLastActionTime = System.currentTimeMillis()
    }

    override fun onDestroy() {
        Log.i(mTAG, "onDestroy")

        //enable Scan2Key Setting
        val enableServiceIntent = Intent()
        enableServiceIntent.action = "unitech.scanservice.scan2key_setting"
        enableServiceIntent.putExtra("scan2key", true)
        sendBroadcast(enableServiceIntent)

        if (isRegister && mReceiver != null) {
            try {
                mContext!!.unregisterReceiver(mReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }

            isRegister = false
            mReceiver = null
            Log.d(mTAG, "unregisterReceiver mReceiver")
        }

        removeTimer()
        stopTimer()

        super.onDestroy()
    }

    override fun onResume() {
        Log.i(mTAG, "onResume")
        super.onResume()

        //disable Scan2Key Setting
        val disableServiceIntent = Intent()
        disableServiceIntent.action = "unitech.scanservice.scan2key_setting"
        disableServiceIntent.putExtra("scan2key", false)
        sendBroadcast(disableServiceIntent)

    }

    override fun onPause() {
        Log.i(mTAG, "onPause")
        super.onPause()

        //disable Scan2Key Setting
        val enableServiceIntent = Intent()
        enableServiceIntent.action = "unitech.scanservice.scan2key_setting"
        enableServiceIntent.putExtra("scan2key", true)
        sendBroadcast(enableServiceIntent)
    }

    override fun onBackPressed() {

        showExitConfirmDialog()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        menuItemKeyboard = menu.findItem(R.id.main_hide_or_show_keyboard)
        menuItemShipmentHistory = menu.findItem(R.id.main_shipment_history)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {

            R.id.main_hide_or_show_keyboard -> {
                imm?.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0)
            }

            R.id.main_shipment_history-> {
                val intent = Intent(this, ShowShipmentHistoryActivity::class.java)
                startActivity(intent)
            }


        }


        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        selectDrawerItem(item)

        return true
    }

    private fun selectDrawerItem(menuItem: MenuItem) {
        var fragment: Fragment? = null
        var fragmentClass: Class<*>? = null

        var title = ""
        //hide keyboard
        val view = currentFocus

        if (view != null) {
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }

        navView!!.menu.getItem(0).isChecked = false //home
        navView!!.menu.getItem(1).isChecked = false //shipment check
        navView!!.menu.getItem(2).isChecked = false //shipment signature

        when (menuItem.itemId) {
            R.id.nav_home -> {
                currentFrag = CurrentFragment.HOME_FRAGMENT
                fragmentClass = HomeFragment::class.java
                menuItem.isChecked = true

                menuItemShipmentHistory!!.isVisible = false
                title = getString(R.string.nav_guest)

                fabBack!!.visibility = View.GONE
                fabSign!!.visibility = View.GONE
                fabMultiSignList!!.visibility = View.GONE
                fabAddToMultiSign!!.visibility = View.GONE
            }

            R.id.nav_shipment_check -> {
                currentFrag = CurrentFragment.SHIPMENT_CHECK_FRAGMENT
                fragmentClass = ShipmentCheckFragment::class.java
                menuItem.isChecked = true

                menuItemShipmentHistory!!.isVisible = true
                title = getString(R.string.nav_shipment_check)

                fabBack!!.visibility = View.GONE
                fabSign!!.visibility = View.GONE
                fabMultiSignList!!.visibility = View.GONE
                fabAddToMultiSign!!.visibility = View.GONE
            }
            R.id.nav_signature -> {
                currentFrag = CurrentFragment.SHIPMENT_SIGNATURE_FRAGMENT
                fragmentClass = ShipmentSignatureFragment::class.java
                menuItem.isChecked = true

                menuItemShipmentHistory!!.isVisible = false
                title = getString(R.string.nav_signature)

                fabBack!!.visibility = View.GONE
                fabSign!!.visibility = View.GONE
                fabMultiSignList!!.visibility = View.GONE
                fabAddToMultiSign!!.visibility = View.GONE
            }

            R.id.nav_setting -> {
                fabBack!!.visibility = View.GONE
                fabSign!!.visibility = View.GONE
                fabMultiSignList!!.visibility = View.GONE
                fabAddToMultiSign!!.visibility = View.GONE

                val settingIntent = Intent()
                settingIntent.action = Constants.ACTION.ACTION_WEBSERVICE_FTP_IP_ADDRESS_SHOW_PASSWORD_DIALOG
                sendBroadcast(settingIntent)
            }

            R.id.nav_about -> {
                showCurrentVersionDialog()
            }

        }

        if (fragmentClass != null) {
            try {
                fragment = fragmentClass.newInstance() as Fragment
            } catch (e: Exception) {
                e.printStackTrace()
            }


            // Insert the fragment by replacing any existing fragment
            val fragmentManager = supportFragmentManager
            //fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment!!).commitAllowingStateLoss()

            // Highlight the selected item has been done by NavigationView

            // Set action bar title
            if (title.isNotEmpty())
                setTitle(title)
            else
                setTitle(menuItem.title)

            // Close the navigation drawer
            val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
        }



    }

    private fun checkAndRequestPermissions() {

        //int accessNetworkStatePermission = ContextCompat.checkSelfPermission(this,
        //        Manifest.permission.ACCESS_NETWORK_STATE);

        //int accessWiFiStatePermission = ContextCompat.checkSelfPermission(this,
        //        Manifest.permission.ACCESS_WIFI_STATE);

        val readPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val writePermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        var accessMediaPermission = 0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            accessMediaPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_MEDIA_LOCATION)
        }



        val networkPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)

        /*val coarsePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        val bluetoothAdminPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)

        val bluetoothPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)*/

        val accessNetworkStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)

        val accessWiFiStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)

        val changeWifiStatePermissions = ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE)

        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

        val listPermissionsNeeded = ArrayList<String>()

        if (readPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (writePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (accessMediaPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.ACCESS_MEDIA_LOCATION)
            }
        }

        if (networkPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET)
        }

        /*if (coarsePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (bluetoothAdminPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        if (bluetoothPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.BLUETOOTH)
        }*/

        if (accessNetworkStatePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_NETWORK_STATE)
        }

        if (accessWiFiStatePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_WIFI_STATE)
        }

        if (changeWifiStatePermissions != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CHANGE_WIFI_STATE)
        }
        //if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
        //    listPermissionsNeeded.add(android.Manifest.permission.WRITE_CALENDAR);
        //}
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                requestIdMultiplePermission
            )
            //return false;
        } else {
            Log.e(mTAG, "All permission are granted")
            initView()
        }
        //return true;
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {

        Log.d(mTAG, "Permission callback called-------")
        when (requestCode) {
            requestIdMultiplePermission -> {

                val perms: HashMap<String, Int>? = HashMap()

                // Initialize the map with both permissions
                perms!![Manifest.permission.READ_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                {
                    perms[Manifest.permission.ACCESS_MEDIA_LOCATION] = PackageManager.PERMISSION_GRANTED
                }
                perms[Manifest.permission.INTERNET] = PackageManager.PERMISSION_GRANTED
                //perms[Manifest.permission.ACCESS_COARSE_LOCATION] = PackageManager.PERMISSION_GRANTED
                //perms[Manifest.permission.BLUETOOTH_ADMIN] = PackageManager.PERMISSION_GRANTED
                //perms[Manifest.permission.BLUETOOTH] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.ACCESS_NETWORK_STATE] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.ACCESS_WIFI_STATE] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.CHANGE_WIFI_STATE] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.CAMERA] = PackageManager.PERMISSION_GRANTED
                //perms.put(Manifest.permission.ACCESS_WIFI_STATE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                //if (grantResults.size > 0) {
                if (grantResults.isNotEmpty()) {
                    for (i in permissions.indices)
                        perms[permissions[i]] = grantResults[i]
                    // Check for both permissions

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (perms[Manifest.permission.READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.ACCESS_MEDIA_LOCATION] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.INTERNET] == PackageManager.PERMISSION_GRANTED
                            //&& perms[Manifest.permission.ACCESS_COARSE_LOCATION] == PackageManager.PERMISSION_GRANTED
                            //&& perms[Manifest.permission.BLUETOOTH_ADMIN] == PackageManager.PERMISSION_GRANTED
                            //&& perms[Manifest.permission.BLUETOOTH] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.ACCESS_NETWORK_STATE] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.ACCESS_WIFI_STATE] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.CHANGE_WIFI_STATE] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.CAMERA] == PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.d(mTAG, "write permission granted")

                            // process the normal flow
                            //else any one or both the permissions are not granted
                            //init_folder_and_files()
                            //init_setting();
                            initView()

                        } else {
                            Log.d(mTAG, "Some permissions are not granted ask again ")
                            //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                            //                        // shouldShowRequestPermissionRationale will return true
                            //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                            if (ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.ACCESS_MEDIA_LOCATION
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.INTERNET
                                )
                                /*|| ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.BLUETOOTH_ADMIN
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.BLUETOOTH
                                )*/
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.ACCESS_NETWORK_STATE
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.ACCESS_WIFI_STATE
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.CHANGE_WIFI_STATE
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.CAMERA
                                )
                            ) {
                                showDialogOK{ _, which ->
                                    when (which) {
                                        DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                                        DialogInterface.BUTTON_NEGATIVE ->
                                            // proceed with logic by disabling the related features or quit the app.
                                            finish()
                                    }
                                }
                            } else {
                                Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show()
                                //                            //proceed with logic by disabling the related features or quit the app.
                            }//|| ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_NETWORK_STATE )
                            //|| ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_WIFI_STATE )
                            //permission is denied (and never ask again is  checked)
                            //shouldShowRequestPermissionRationale will return false
                        }
                    } else { //Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                        if (perms[Manifest.permission.READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                            //&& perms[Manifest.permission.ACCESS_MEDIA_LOCATION] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.INTERNET] == PackageManager.PERMISSION_GRANTED
                            //&& perms[Manifest.permission.ACCESS_COARSE_LOCATION] == PackageManager.PERMISSION_GRANTED
                            //&& perms[Manifest.permission.BLUETOOTH_ADMIN] == PackageManager.PERMISSION_GRANTED
                            //&& perms[Manifest.permission.BLUETOOTH] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.ACCESS_NETWORK_STATE] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.ACCESS_WIFI_STATE] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.CHANGE_WIFI_STATE] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.CAMERA] == PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.d(mTAG, "write permission granted")

                            // process the normal flow
                            //else any one or both the permissions are not granted
                            //init_folder_and_files()
                            //init_setting();
                            initView()

                        } else {
                            Log.d(mTAG, "Some permissions are not granted ask again ")
                            //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                            //                        // shouldShowRequestPermissionRationale will return true
                            //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                            if (ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                                /*|| ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.ACCESS_MEDIA_LOCATION
                                )*/
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.INTERNET
                                )
                                /*|| ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.BLUETOOTH_ADMIN
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.BLUETOOTH
                                )*/
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.ACCESS_NETWORK_STATE
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.ACCESS_WIFI_STATE
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.CHANGE_WIFI_STATE
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.CAMERA
                                )
                            ) {
                                showDialogOK{ _, which ->
                                    when (which) {
                                        DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                                        DialogInterface.BUTTON_NEGATIVE ->
                                            // proceed with logic by disabling the related features or quit the app.
                                            finish()
                                    }
                                }
                            } else {
                                Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show()
                                //                            //proceed with logic by disabling the related features or quit the app.
                            }//|| ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_NETWORK_STATE )
                            //|| ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_WIFI_STATE )
                            //permission is denied (and never ask again is  checked)
                            //shouldShowRequestPermissionRationale will return false
                        }
                    }


                    //&& perms.get(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED &&
                    //perms.get(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
                }
            }
        }

    }

    private fun showDialogOK(okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
            .setMessage("Warning")
            .setPositiveButton("Ok", okListener)
            .setNegativeButton("Cancel", okListener)
            .create()
            .show()
    }

    private fun toast(message: String) {

        if (toastHandle != null) {
            toastHandle!!.cancel()
        }

        val toast = Toast.makeText(this, HtmlCompat.fromHtml("<h1>$message</h1>", HtmlCompat.FROM_HTML_MODE_COMPACT), Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, 0)
        /*val group = toast.view as ViewGroup
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

    private fun initView() {

        //show menu
        title = getString(R.string.nav_guest)

        var fragment: Fragment? = null
        val fragmentClass: Class<*>
        fragmentClass = HomeFragment::class.java

        try {
            fragment = fragmentClass.newInstance()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val fragmentManager = supportFragmentManager
        //fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment!!).commitAllowingStateLoss()

    }

    fun checkServerErrorString(res: String): Boolean {
        var ret = false
        if (res.contains("System.OutOfMemoryException")) {
            ret = true
        }

        return ret
    }

    fun getReceipt(barcode: ScanBarcode?) {
        // go for : 1.get barcode  2.call get Receipt Api ,3.update list ,4. restore input mode
        //1.

        /*runOnUiThread(Runnable {
            // hideKeyboard();
            mLoadingView.setStatus(LoadingView.LOADING)
        })*/

        if (barcode != null) {
            Log.e(mTAG, "getReceipt poBarcode = "+barcode.poBarcode+ ", poLine = "+barcode.poLine)
            // to call api
            //acState = ReceiptACState.RECEIPT_GETTING_STATE

            /*val para = HttpReceiptGetPara()
            para.pmn01 = barcode.poBarcode
            para.pmn02 = barcode.poLine

            ApiFunc().getReceipt(para, getReceiptCallback)*/
            when {
                barcode.poBarcodeByScan.length >= 16 -> {
                    val para = HttpReceiptGetPara()
                    para.pmn01 = barcode.poBarcode
                    para.pmn02 = barcode.poLine

                    ApiFunc().getReceipt(para, getReceiptCallback)
                }
                barcode.poBarcodeByScan.length == 13 -> {
                    val para = HttpReceiptPointGetPara()
                    para.pmn01 = barcode.poBarcodeByScan
                    para.pmn02 = "0"

                    ApiFunc().getReceiptPoint(para, getReceiptPointCallback)
                }
                else -> {
                    val unknownIntent = Intent()
                    unknownIntent.action = Constants.ACTION.ACTION_RECEIPT_UNKNOWN_BARCODE_LENGTH
                    sendBroadcast(unknownIntent)
                }
            }

        }


    }//getReceipt


    private var getReceiptCallback: Callback = object : Callback {

        override fun onFailure(call: Call, e: IOException) {
            isBarcodeScanning = false
            runOnUiThread(netErrRunnable)

        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            Log.e(mTAG, "onResponse : "+response.body.toString())
            val res = ReceiveTransform.restoreToJsonStr(response.body!!.string())
            //1.get response ,2 error or right , 3 update ui ,4. restore acState 5. update fragment detail
            runOnUiThread {
                try {

                    val retItemReceipt = ItemReceipt.transRJReceiptStrToItemReceipt(res, barcode!!.poBarcode)


                    if (retItemReceipt != null) {
                        if (!retItemReceipt.rjReceipt?.result.equals(ItemReceipt.RESULT_CORRECT)) {
                            Log.e(
                                mTAG,
                                "result = " + retItemReceipt.rjReceipt?.result + " result2 = " + retItemReceipt.rjReceipt?.result2
                            )
                            //can't receive the item
                            //val mess = retItemReceipt.poNumScanTotal + " " + retItemReceipt.rjReceipt?.result2
                            val mess = retItemReceipt.rjReceipt?.result2 as String
                            toastLong(mess)


                            val receiptNoIntent = Intent()
                            receiptNoIntent.action = Constants.ACTION.ACTION_RECEIPT_NO_NOT_EXIST
                            sendBroadcast(receiptNoIntent)
                        }// result  = 0
                        else {
                            // success receive ,update list ,update fragment
                            //if(Fristpmc3.equals("") || Fristpmm02.equals("") || Fristpmc3.equals(itemReceipt.rjReceipt.pmc03) || Fristpmm02.equals(itemReceipt.rjReceipt.pmm02)) {

                            //multi
                            /*if (ReceiptList.size() > 0 ) {
                                ReceiptList.removeAllItem()
                            }

                            addResult = ReceiptList.add(retItemReceipt)
                            itemReceipt = ReceiptList.getReceiptItem(0)*/

                            //single
                            itemReceipt = retItemReceipt

                            Log.e(mTAG, "2")
                            val refreshIntent = Intent()
                            refreshIntent.action = Constants.ACTION.ACTION_RECEIPT_FRAGMENT_REFRESH
                            //refreshIntent.putExtra("RVA06", rva06)
                            mContext!!.sendBroadcast(refreshIntent)

                        }//result = 1
                    } else {
                        Log.e(mTAG, "retItemReceipt = null")

                        toast(getString(R.string.receipt_this_receipt_not_exist))

                        val receiptNoIntent = Intent()
                        receiptNoIntent.action = Constants.ACTION.ACTION_RECEIPT_NO_NOT_EXIST
                        sendBroadcast(receiptNoIntent)
                    }


                } catch (ex: Exception) {

                    Log.e(mTAG, "Server error")

                    val serverErrorIntent = Intent()
                    serverErrorIntent.action = Constants.ACTION.ACTION_SERVER_ERROR
                    sendBroadcast(serverErrorIntent)
                    //system error
                    runOnUiThread {

                        toast(getString(R.string.toast_server_error))
                    }
                }
                isBarcodeScanning = false
            }


        }//onResponse
    }

    private var getReceiptPointCallback: Callback = object : Callback {

        override fun onFailure(call: Call, e: IOException) {
            Log.e(mTAG, "getReceiptPointCallback err msg = $e")
            isBarcodeScanning = false
            val errorArray = e.toString().split(": ")

            if (errorArray.size > 1) {
                Log.e(mTAG, "[1] = ${errorArray[1]}")
            }


            val failIntent = Intent()

            if (errorArray.size > 1 && errorArray[1] == "No route to host" ) {
                failIntent.action = Constants.ACTION.ACTION_CONNECTION_NO_ROUTE_TO_HOST
            } else {
                failIntent.action = Constants.ACTION.ACTION_CONNECTION_TIMEOUT
            }

            runOnUiThread {
                sendBroadcast(failIntent)
            }

        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            Log.e(mTAG, "onResponse : "+response.body.toString())
            val res = ReceiveTransform.restoreToJsonStr(response.body!!.string())
            //Log.e(mTAG, "res = $res")
            //1.get response ,2 error or right , 3 update ui ,4. restore acState 5. update fragment detail
            runOnUiThread {
                try {
                    if (!checkServerErrorString(res)) {
                        val retItemReceipt = ItemReceipt.transRJReceiptStrToItemReceiptPoint(
                            res,
                            barcode!!.poBarcodeByScan
                        )


                        if (retItemReceipt != null) {
                            if (!retItemReceipt.rjReceipt?.result.equals(ItemReceipt.RESULT_CORRECT)) {
                                Log.e(
                                    mTAG,
                                    "result = " + retItemReceipt.rjReceipt?.result + " result2 = " + retItemReceipt.rjReceipt?.result2
                                )
                                //can't receive the item
                                //val mess = retItemReceipt.poNumScanTotal + " " + retItemReceipt.rjReceipt?.result2
                                val mess = retItemReceipt.rjReceipt?.result2 as String
                                toastLong(mess)


                                val receiptNoIntent = Intent()
                                receiptNoIntent.action =
                                    Constants.ACTION.ACTION_RECEIPT_NO_NOT_EXIST
                                sendBroadcast(receiptNoIntent)
                            }// result  = 0
                            else {
                                // success receive ,update list ,update fragment
                                //if(Fristpmc3.equals("") || Fristpmm02.equals("") || Fristpmc3.equals(itemReceipt.rjReceipt.pmc03) || Fristpmm02.equals(itemReceipt.rjReceipt.pmm02)) {

                                //multi
                                /*if (ReceiptList.size() > 0 ) {
                                ReceiptList.removeAllItem()
                            }

                            addResult = ReceiptList.add(retItemReceipt)
                            itemReceipt = ReceiptList.getReceiptItem(0)*/

                                //single
                                itemReceipt = retItemReceipt

                                Log.e(mTAG, "2")
                                val refreshIntent = Intent()
                                refreshIntent.action =
                                    Constants.ACTION.ACTION_RECEIPT_FRAGMENT_REFRESH
                                //refreshIntent.putExtra("RVA06", rva06)
                                mContext!!.sendBroadcast(refreshIntent)

                            }//result = 1
                        } else {
                            Log.e(mTAG, "retItemReceipt = null")

                            toast(getString(R.string.receipt_this_receipt_not_exist))

                            val receiptNoIntent = Intent()
                            receiptNoIntent.action = Constants.ACTION.ACTION_RECEIPT_NO_NOT_EXIST
                            sendBroadcast(receiptNoIntent)
                        }
                    } else { //checkServerErrorString true
                        toast(getString(R.string.toast_server_error))
                    }


                } catch (ex: Exception) {

                    Log.e(mTAG, "Server error")

                    val serverErrorIntent = Intent()
                    serverErrorIntent.action = Constants.ACTION.ACTION_SERVER_ERROR
                    sendBroadcast(serverErrorIntent)
                    //system error
                    runOnUiThread {

                        toast(getString(R.string.toast_server_error))
                    }
                }
                isBarcodeScanning = false
            }


        }//onResponse
    }

    //guest
    fun guestInOrOutMulti(data1: String, data2: String, data3: String, data4: String, data5: String, data6: String) {

        val para = HttpGuestInOrOutMultiPara()

        para.data1 = data1 // (In: 0, Out: 2)
        para.data2 = data2 // plant
        para.data3 = data3 // pmm09 供應商編號
        para.data4 = data4 // 採購單編號
        para.data5 = data5 // 採購單項次
        para.data6 = data6 // 刷退帶刷進日期

        Log.e(mTAG, "data1 = $data1, data2 = $data2, data3 = $data3, data4 = $data4, data5 = $data5, data6 = $data6")

        ApiFunc().guestInOrOutMulti(para, guestInOrOutMultiCallback)
    }

    private var guestInOrOutMultiCallback: Callback = object : Callback {

        override fun onFailure(call: Call, e: IOException) {
            isBarcodeScanning = false

            runOnUiThread(netErrRunnable)
        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            Log.e(mTAG, "onResponse : "+response.body.toString())
            val res = ReceiveTransform.restoreToJsonStr(response.body!!.string())
            Log.e(mTAG, "res = $res")
            //val res = ReceiveTransform.restoreToJsonStr(response.body()!!.string())
            //1.get response ,2 error or right , 3 update ui ,4. restore acState 5. update fragment detail
            runOnUiThread {
                try {

                    val retItemGuest = ItemGuest.transRJGuestStrToItemGuest(res)

                    if (retItemGuest != null) {
                        if (!retItemGuest.rjGuest?.result.equals(ItemGuest.RESULT_CORRECT)) {
                            Log.e(mTAG, "result = " + retItemGuest.rjGuest?.result + " result2 = " + retItemGuest.rjGuest?.result2)
                            //can't receive the item
                            //val mess = retItemReceipt.poNumScanTotal + " " + retItemReceipt.rjReceipt?.result2
                            val mess = retItemGuest.rjGuest?.result2 as String
                            toastLong(mess)


                            val refreshIntent = Intent()
                            refreshIntent.action = Constants.ACTION.ACTION_GUEST_IN_OR_LEAVE_FAILED
                            mContext!!.sendBroadcast(refreshIntent)
                        }// result  = 0
                        else {
                            // success receive ,update list ,update fragment
                            Log.d(mTAG, "guest In or Out success!")

                            val msg = retItemGuest.rjGuest?.data1 as String
                            toastLong(msg)

                            val successIntent = Intent()
                            successIntent.action = Constants.ACTION.ACTION_GUEST_IN_OR_LEAVE_SUCCESS
                            mContext!!.sendBroadcast(successIntent)

                        }//result = 1
                    } else {
                        Log.e(mTAG, "retItemGuest = null")


                    }


                } catch (e: Exception) {
                    Log.e(mTAG, "ex = $e")
                    //system error
                    //toast(getString(R.string.toast_server_error))
                    //val failIntent = Intent()
                    //failIntent.action = Constants.ACTION.ACTION_SERVER_ERROR
                    //sendBroadcast(failIntent)
                }

                isBarcodeScanning = false
            }


        }//onResponse
    }

    fun getGuestMulti(plant: String) {
        Log.e(mTAG, "=== getGuestMulti start ===")
        Log.e(mTAG, "plant = $plant ===")
        val para = HttpGuestNotLeaveGetPara()
        para.data1 = plant
        ApiFunc().getGuestMulti(para, getGuestMultiCallback)
    }

    private var getGuestMultiCallback: Callback = object : Callback {

        override fun onFailure(call: Call, e: IOException) {
            isBarcodeScanning = false
            runOnUiThread(netErrRunnable)
        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            Log.e(mTAG, "onResponse : "+response.body.toString())
            val jsonStr = ReceiveTransform.addToJsonArrayStr(response.body!!.string())
            Log.e(mTAG, "jsonStr = $jsonStr")
            //val res = ReceiveTransform.restoreToJsonStr(response.body()!!.string())
            //1.get response ,2 error or right , 3 update ui ,4. restore acState 5. update fragment detail
            runOnUiThread {
                try {
                    //guestList.clear()
                    when(currentSearchPlant) {
                        "A" -> guestListA.clear()
                        "B" -> guestListB.clear()
                        else -> guestListT.clear()
                    }

                    val rjGuestList = Gson().fromJson(jsonStr, ReceiveTransform.RJGuestList::class.java)
                    Log.e(mTAG, "rjGuestList.dataList.size = " + rjGuestList.dataList.size)

                    if (rjGuestList.dataList.size > 0) {

                        Log.e(mTAG, "=== guestList start ===")

                        if (rjGuestList.dataList.size == 1) {
                            if (rjGuestList.dataList[0].result == "0") { //0 success
                                when(currentSearchPlant) {
                                    "A" -> {
                                        guestListA.add(rjGuestList.dataList[0])
                                        val successIntent = Intent()
                                        //successIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_NEXT_ACTION
                                        successIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_COMPLETE
                                        mContext!!.sendBroadcast(successIntent)
                                    }
                                    "B" -> {
                                        guestListB.add(rjGuestList.dataList[0])
                                        val successIntent = Intent()
                                        successIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_COMPLETE
                                        mContext!!.sendBroadcast(successIntent)
                                    }
                                    else -> {
                                        guestListT.add(rjGuestList.dataList[0])
                                        val successIntent = Intent()
                                        //successIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_NEXT_ACTION
                                        successIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_COMPLETE
                                        mContext!!.sendBroadcast(successIntent)
                                    }
                                }
                                //guestList.add(rjGuestList.dataList[0])

                                /*val successIntent = Intent()
                                successIntent.action = Constants.ACTION.ACTION_GUEST_GET_CURRENT_PLANT_GUEST_SUCCESS
                                mContext!!.sendBroadcast(successIntent)*/
                            } else {

                                val errorIntent = Intent()
                                errorIntent.action = Constants.ACTION.ACTION_GUEST_GET_CURRENT_PLANT_GUEST_FAILED
                                errorIntent.putExtra("result2", rjGuestList.dataList[0].result2)
                                mContext!!.sendBroadcast(errorIntent)



                                /*if (currentSearchPlant.contentEquals(currentPlant)) {
                                    val errorIntent = Intent()
                                    errorIntent.action = Constants.ACTION.ACTION_GUEST_GET_CURRENT_PLANT_GUEST_FAILED
                                    errorIntent.putExtra("result2", rjGuestList.dataList[0].result2)
                                    mContext!!.sendBroadcast(errorIntent)
                                }*/

                                /*when(currentSearchPlant) {
                                    "A" -> {
                                        val nextIntent = Intent()
                                        nextIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_NEXT_ACTION
                                        mContext!!.sendBroadcast(nextIntent)
                                    }
                                    "B" -> {
                                        val completeIntent = Intent()
                                        completeIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_COMPLETE
                                        mContext!!.sendBroadcast(completeIntent)
                                    }
                                    else -> {
                                        val nextIntent = Intent()
                                        nextIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_NEXT_ACTION
                                        mContext!!.sendBroadcast(nextIntent)
                                    }
                                }*/

                            }

                        } else { //size > 1
                            var error = 0
                            for (rjGuest in rjGuestList.dataList) {
                                if (rjGuest.result == "0") { //0 success
                                    when(currentSearchPlant) {
                                        "A" -> guestListA.add(rjGuest)
                                        "B" -> guestListB.add(rjGuest)
                                        else -> guestListT.add(rjGuest)
                                    }
                                    //guestList.add(rjGuest)
                                    //Log.d(mTAG, "rjMaterial = "+rjMaterial.ima02)


                                } else { //failed
                                    error++
                                    Log.e(mTAG, "rjGuest.result = ${rjGuest.result}")
                                }
                            }

                            Log.d(mTAG, "error = $error")
                            when(currentSearchPlant) {
                                "A" -> {
                                    val successIntent = Intent()
                                    //successIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_NEXT_ACTION
                                    successIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_COMPLETE
                                    mContext!!.sendBroadcast(successIntent)
                                }
                                "B" -> {
                                    val successIntent = Intent()
                                    successIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_COMPLETE
                                    mContext!!.sendBroadcast(successIntent)
                                }
                                else -> {
                                    val successIntent = Intent()
                                    //successIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_NEXT_ACTION
                                    successIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_COMPLETE
                                    mContext!!.sendBroadcast(successIntent)
                                }
                            }
                            /*val successIntent = Intent()
                            successIntent.action = Constants.ACTION.ACTION_GUEST_GET_CURRENT_PLANT_GUEST_SUCCESS
                            mContext!!.sendBroadcast(successIntent)*/
                        }


                        Log.e(mTAG, "=== guestList end ===")

                        //val refreshIntent = Intent()
                        //refreshIntent.action = Constants.ACTION.ACTION_GUEST_FRAGMENT_REFRESH
                        //!!.sendBroadcast(refreshIntent)

                    } else { //size == 0
                        when(currentSearchPlant) {
                            "A" -> {
                                val noExistIntent = Intent()
                                //noExistIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_NEXT_ACTION
                                noExistIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_COMPLETE
                                mContext!!.sendBroadcast(noExistIntent)
                            }
                            "B" -> {
                                val noExistIntent = Intent()
                                noExistIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_COMPLETE
                                mContext!!.sendBroadcast(noExistIntent)
                            }
                            else -> {
                                val noExistIntent = Intent()
                                //noExistIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_NEXT_ACTION
                                noExistIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_COMPLETE
                                mContext!!.sendBroadcast(noExistIntent)
                            }
                        }

                        /*Log.e(mTAG, "rjGuestList size = 0")
                        val noExistIntent = Intent()
                        noExistIntent.action = Constants.ACTION.ACTION_GUEST_LIST_CLEAR
                        mContext!!.sendBroadcast(noExistIntent)*/
                    }


                } catch (e: Exception) {
                    Log.e(mTAG, "ex = $e")
                    //system error
                    //toast(getString(R.string.toast_server_error))
                    //val failIntent = Intent()
                    //failIntent.action = Constants.ACTION.ACTION_SERVER_ERROR
                    //sendBroadcast(failIntent)
                }
                isBarcodeScanning = false
            }


        }//onResponse
    }

    fun getShipmentCheckMulti(barcode: ScanBarcode?) {

        if (barcode != null) {
            Log.e(mTAG, "=== getGuestMulti start ===")
            Log.e(mTAG, "barcode poBarcode = ${barcode.poBarcodeByScan},  poLine = ${barcode.poLine} ===")
            val para = HttpShipmentPara()
            para.data1 = barcode.poBarcode
            Log.e(mTAG, "barcode.poLine = ${barcode.poLine.toInt()}")
            para.data2 = barcode.poLine.toInt().toString()
            ApiFunc().getShipmentCheckMulti(para, getShipmentCheckMultiCallback)
        }

    }

    private var getShipmentCheckMultiCallback: Callback = object : Callback {

        override fun onFailure(call: Call, e: IOException) {
            isBarcodeScanning = false
            runOnUiThread(netErrRunnable)
        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            Log.e(mTAG, "onResponse : "+response.body.toString())
            val jsonStr = ReceiveTransform.restoreXmlToJson(response.body!!.string())
            Log.e(mTAG, "jsonStr = $jsonStr")
            //val res = ReceiveTransform.restoreToJsonStr(response.body()!!.string())
            //1.get response ,2 error or right , 3 update ui ,4. restore acState 5. update fragment detail
            runOnUiThread {
                try {
                    shipmentList.clear()
                    val rjShipmentList = Gson().fromJson(jsonStr, ReceiveTransform.RJShipmentList::class.java)
                    Log.e(mTAG, "rjShipmentList.dataList.size = " + rjShipmentList.dataList.size)

                    if (rjShipmentList.dataList.size > 0) {

                        Log.e(mTAG, "=== shipmentList start ===")

                        for (rjShipment in rjShipmentList.dataList) {
                            shipmentList.add(rjShipment)
                        }




                        Log.e(mTAG, "=== shipmentList end ===")

                        val refreshIntent = Intent()
                        refreshIntent.action = Constants.ACTION.ACTION_SHIPMENT_FRAGMENT_REFRESH
                        refreshIntent.putExtra("BARCODE", barcode!!.poBarcodeByScan)
                        mContext!!.sendBroadcast(refreshIntent)

                    } else { //size == 0
                        val noExistIntent = Intent()
                        //noExistIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_NEXT_ACTION
                        noExistIntent.action = Constants.ACTION.ACTION_SHIPMENT_CHECK_RETURN_EMPTY
                        mContext!!.sendBroadcast(noExistIntent)


                    }


                } catch (e: Exception) {
                    Log.e(mTAG, "ex = $e")
                    //system error
                    //toast(getString(R.string.toast_server_error))
                    //val failIntent = Intent()
                    //failIntent.action = Constants.ACTION.ACTION_SERVER_ERROR
                    //sendBroadcast(failIntent)
                }
                isBarcodeScanning = false
            }


        }//onResponse
    }

    fun getShipmentSignatureMulti(shipmentDate: String, shipmentNo: String) {

        Log.e(mTAG, "=== getShipmentSignatureMulti start ===")

        val para = HttpShipmentSignaturePara()
        para.data1 = shipmentDate
        para.data2 = shipmentNo
        ApiFunc().getShipmentSignatureMulti(para, getShipmentSignatureMultiCallback)

    }

    private var getShipmentSignatureMultiCallback: Callback = object : Callback {

        override fun onFailure(call: Call, e: IOException) {
            isBarcodeScanning = false
            runOnUiThread(netErrRunnable)
        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            Log.e(mTAG, "onResponse : "+response.body.toString())
            val jsonStr = ReceiveTransform.restoreXmlToJson(response.body!!.string())
            Log.e(mTAG, "jsonStr = $jsonStr")
            //val res = ReceiveTransform.restoreToJsonStr(response.body()!!.string())
            //1.get response ,2 error or right , 3 update ui ,4. restore acState 5. update fragment detail
            runOnUiThread {
                try {
                    signatureList.clear()
                    val rjSignatureList = Gson().fromJson(jsonStr, ReceiveTransform.RJSignatureList::class.java)
                    Log.e(mTAG, "rjSignatureList.dataList.size = " + rjSignatureList.dataList.size)

                    if (rjSignatureList.dataList.size > 0) {

                        Log.e(mTAG, "=== signatureList start ===")

                        if (rjSignatureList.dataList.size == 1) {

                            if (rjSignatureList.dataList[0].result == "0") { //0 success
                                for (rjSignature in rjSignatureList.dataList) {
                                    signatureList.add(rjSignature)
                                }

                                val refreshIntent = Intent()
                                refreshIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_FRAGMENT_REFRESH
                                //refreshIntent.putExtra("BARCODE", barcode!!.poBarcodeByScan)
                                mContext!!.sendBroadcast(refreshIntent)

                                //show fab
                                fabAddToMultiSign!!.visibility = View.VISIBLE
                                fabMultiSignList!!.visibility = View.VISIBLE
                            } else {

                                toastLong(rjSignatureList.dataList[0].result2)

                                val errorIntent = Intent()
                                errorIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SEARCH_NO_FAILED
                                //errorIntent.putExtra("result2", rjSignatureList.dataList[0].result2)
                                mContext!!.sendBroadcast(errorIntent)


                            }

                        } else {
                            for (rjSignature in rjSignatureList.dataList) {
                                signatureList.add(rjSignature)
                            }

                            val refreshIntent = Intent()
                            refreshIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_FRAGMENT_REFRESH
                            //refreshIntent.putExtra("BARCODE", barcode!!.poBarcodeByScan)
                            mContext!!.sendBroadcast(refreshIntent)

                            Log.e(mTAG, "=== signatureList end ===")
                        }










                    } else { //size == 0
                        val noExistIntent = Intent()
                        noExistIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_RETURN_EMPTY
                        mContext!!.sendBroadcast(noExistIntent)


                    }


                } catch (e: Exception) {
                    Log.e(mTAG, "ex = $e")
                    //system error
                    //toast(getString(R.string.toast_server_error))
                    //val failIntent = Intent()
                    //failIntent.action = Constants.ACTION.ACTION_SERVER_ERROR
                    //sendBroadcast(failIntent)
                }
                isBarcodeScanning = false
            }


        }//onResponse
    }

    fun getShipmentSignatureDetail(shipmentNo: String) {

        Log.e(mTAG, "=== getShipmentSignatureDetail start ===")

        val para = HttpShipmentSignatureDetailPara()
        para.data1 = shipmentNo

        ApiFunc().getShipmentSignatureDetail(para, getShipmentSignatureDetailCallback)

    }

    private var getShipmentSignatureDetailCallback: Callback = object : Callback {

        override fun onFailure(call: Call, e: IOException) {
            isBarcodeScanning = false
            runOnUiThread(netErrRunnable)
        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            Log.e(mTAG, "onResponse : "+response.body.toString())
            val jsonStr = ReceiveTransform.restoreXmlToJson(response.body!!.string())
            Log.e(mTAG, "jsonStr = $jsonStr")
            //val res = ReceiveTransform.restoreToJsonStr(response.body()!!.string())
            //1.get response ,2 error or right , 3 update ui ,4. restore acState 5. update fragment detail
            runOnUiThread {
                try {
                    signatureDetailList.clear()
                    val rjSignatureDetailList = Gson().fromJson(jsonStr, ReceiveTransform.RJSignatureDetailList::class.java)
                    Log.e(mTAG, "rjSignatureDetailList.dataList.size = " + rjSignatureDetailList.dataList.size)

                    if (rjSignatureDetailList.dataList.size > 0) {

                        Log.e(mTAG, "=== signatureDetailList start ===")

                        if (rjSignatureDetailList.dataList.size == 1) {

                            if (rjSignatureDetailList.dataList[0].result == "0") { //0 success
                                for (rjSignatureDetail in rjSignatureDetailList.dataList) {
                                    signatureDetailList.add(rjSignatureDetail)
                                }

                                fabAddToMultiSign!!.visibility = View.GONE
                                fabMultiSignList!!.visibility = View.GONE

                                fabBack!!.visibility = View.VISIBLE
                                //fabSign!!.visibility = View.VISIBLE

                                val refreshIntent = Intent()
                                refreshIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DETAIL_FRAGMENT_REFRESH
                                //refreshIntent.putExtra("BARCODE", barcode!!.poBarcodeByScan)
                                mContext!!.sendBroadcast(refreshIntent)


                            } else {

                                toastLong(rjSignatureDetailList.dataList[0].result2)

                                val errorIntent = Intent()
                                errorIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SEARCH_DETAIL_FAILED
                                //errorIntent.putExtra("result2", rjSignatureList.dataList[0].result2)
                                mContext!!.sendBroadcast(errorIntent)


                            }

                        } else {
                            for (rjSignatureDetail in rjSignatureDetailList.dataList) {
                                signatureDetailList.add(rjSignatureDetail)
                            }

                            fabBack!!.visibility = View.VISIBLE
                            fabSign!!.visibility = View.VISIBLE

                            val refreshIntent = Intent()
                            refreshIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DETAIL_FRAGMENT_REFRESH
                            //refreshIntent.putExtra("BARCODE", barcode!!.poBarcodeByScan)
                            mContext!!.sendBroadcast(refreshIntent)

                            Log.e(mTAG, "=== signatureList end ===")
                        }


                    } else { //size == 0
                        val noExistIntent = Intent()
                        noExistIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DETAIL_RETURN_EMPTY
                        mContext!!.sendBroadcast(noExistIntent)


                    }


                } catch (e: Exception) {
                    Log.e(mTAG, "ex = $e")
                    //system error
                    //toast(getString(R.string.toast_server_error))
                    //val failIntent = Intent()
                    //failIntent.action = Constants.ACTION.ACTION_SERVER_ERROR
                    //sendBroadcast(failIntent)
                }
                isBarcodeScanning = false
            }


        }//onResponse
    }

    fun confirmShipmentSignature(shipmentNo: String, signFileName: String, userName: String, type: String) {
        Log.e(mTAG, "=== confirmShipmentSignature start ===")

        Log.e(mTAG, "shipmentNo = $shipmentNo, signFileName = $signFileName, userName = $userName, type = $type")

        val para = HttpShipmentSignatureConfirmPara()
        para.data1 = shipmentNo
        para.data2 = signFileName
        para.data3 = userName
        para.data4 = type
        ApiFunc().getShipmentSignatureConfirm(para, confirmShipmentSignatureCallback)
    }

    private var confirmShipmentSignatureCallback: Callback = object : Callback {

        override fun onFailure(call: Call, e: IOException) {

            runOnUiThread(netErrRunnable)
        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            Log.e(mTAG, "onResponse : "+response.body.toString())
            //val jsonStr = ReceiveTransform.addToJsonArrayStr(response.body!!.string())
            val res = ReceiveTransform.restoreXmlToJson(response.body!!.string())
            Log.e(mTAG, "res = $res")
            //val res = ReceiveTransform.restoreToJsonStr(response.body()!!.string())
            //1.get response ,2 error or right , 3 update ui ,4. restore acState 5. update fragment detail
            runOnUiThread {
                try {
                    if (!checkServerErrorString(res)) {
                        //==== receive single record start ====
                        val rjShipmentSignatureConfirm: RJShipmentSignatureConfirm = Gson().fromJson(
                            res,
                            RJShipmentSignatureConfirm::class.java
                        ) as RJShipmentSignatureConfirm

                        if (rjShipmentSignatureConfirm.result == "0") {
                            val successIntent = Intent()
                            if (signState == SignState.GUARD_UPLOADED) {
                                if (!isSignMulti) {
                                    successIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_SIGN_CONFIRM_SUCCESS
                                } else {
                                    successIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_SIGN_CONFIRM_SUCCESS
                                }
                            } else if (signState == SignState.DRIVER_CONFIRM) {
                                if (!isSignMulti) {
                                    successIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_SIGN_CONFIRM_SUCCESS
                                } else {
                                    successIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_SUCCESS
                                }
                            }

                            successIntent.putExtra("SEND_ORDER", currentShipmentNo)
                            mContext!!.sendBroadcast(successIntent)
                        } else {
                            val failedIntent = Intent()
                            if (signState == SignState.GUARD_UPLOADED) {
                                if (!isSignMulti) {
                                    failedIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DRIVER_SIGN_CONFIRM_FAILED
                                } else {
                                    failedIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_SIGN_CONFIRM_FAILED
                                }
                            } else if (signState == SignState.DRIVER_CONFIRM) {
                                if (!isSignMulti) {
                                    failedIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_SIGN_CONFIRM_FAILED
                                } else {
                                    failedIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_FAILED
                                }
                            }
                            mContext!!.sendBroadcast(failedIntent)

                            toast(rjShipmentSignatureConfirm.result2)
                        }
                    } else {//checkServerErrorString = true
                        toast(getString(R.string.toast_server_error))
                    }


                } catch (e: Exception) {
                    Log.e(mTAG, "ex = $e")
                    //system error
                    //toast(getString(R.string.toast_server_error))
                    //val failIntent = Intent()
                    //failIntent.action = Constants.ACTION.ACTION_SERVER_ERROR
                    //sendBroadcast(failIntent)
                }
            }


        }//onResponse
    }

    internal var netErrRunnable: Runnable = Runnable {

        isBarcodeScanning = false

        //mLoadingView.setStatus(LoadingView.GONE)
        // Toast.makeText(mContext,getString(R.string.toast_network_error),Toast.LENGTH_LONG).show();
        //showMyToast(getString(R.string.toast_network_error), mContext)
        toast(getString(R.string.toast_network_error))
        val failIntent = Intent()
        failIntent.action = Constants.ACTION.ACTION_NETWORK_FAILED
        sendBroadcast(failIntent)


    }

    private fun showExitConfirmDialog() {

        // get prompts.xml view
        /*LayoutInflater layoutInflater = LayoutInflater.from(Nfc_read_app.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);*/
        val promptView = View.inflate(this@MainActivity, R.layout.confirm_dialog, null)

        val alertDialogBuilder = AlertDialog.Builder(this@MainActivity).create()
        alertDialogBuilder.setView(promptView)

        //final EditText editFileName = (EditText) promptView.findViewById(R.id.editFileName);
        val textViewMsg = promptView.findViewById<TextView>(R.id.textViewDialog)
        val btnCancel = promptView.findViewById<Button>(R.id.btnDialogCancel)
        val btnConfirm = promptView.findViewById<Button>(R.id.btnDialogConfirm)

        textViewMsg.text = getString(R.string.exit_app_msg)
        btnCancel.text = getString(R.string.cancel)
        btnConfirm.text = getString(R.string.confirm)

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
        btnCancel!!.setOnClickListener {
            alertDialogBuilder.dismiss()
        }
        btnConfirm!!.setOnClickListener {
            val drawer : DrawerLayout = findViewById(R.id.drawer_layout)
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START)
            }
            alertDialogBuilder.dismiss()
            //isLogin = false

            finish()


        }
        alertDialogBuilder.show()
    }

    private fun showCurrentVersionDialog() {

        // get prompts.xml view
        /*LayoutInflater layoutInflater = LayoutInflater.from(Nfc_read_app.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);*/
        val promptView = View.inflate(this@MainActivity, R.layout.about_dialog, null)

        val alertDialogBuilder = AlertDialog.Builder(this@MainActivity).create()
        alertDialogBuilder.setView(promptView)

        //final EditText editFileName = (EditText) promptView.findViewById(R.id.editFileName);
        val textViewMsg = promptView.findViewById<TextView>(R.id.textViewDialog)
        val textViewFixMsg = promptView.findViewById<TextView>(R.id.textViewFixHistory)
        val btnCancel = promptView.findViewById<Button>(R.id.btnDialogCancel)
        val btnConfirm = promptView.findViewById<Button>(R.id.btnDialogConfirm)

        textViewMsg.text = getString(R.string.version_string, BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME)
        var msg = "1. 新增每隔1分鐘自動同步\n"
        msg += "2. 新增出貨檢查功能"
        //msg += "3. 新增\"設定\"讓使用者決定手動或自動確認"
        textViewFixMsg.text = msg

        btnCancel.text = getString(R.string.cancel)
        btnCancel.visibility = View.GONE
        btnConfirm.text = getString(R.string.confirm)

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)

        btnConfirm!!.setOnClickListener {

            alertDialogBuilder.dismiss()
        }
        alertDialogBuilder.show()

    }

    private fun showIPSettingPasswordDialog() {

        Log.e(mTAG, "=== showIPSettingPasswordDialog start ===")



        // get prompts.xml view
        /*LayoutInflater layoutInflater = LayoutInflater.from(Nfc_read_app.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);*/
        val promptView = View.inflate(this@MainActivity, R.layout.webserviceftp_ip_setting_dialog, null)

        val alertDialogBuilder = AlertDialog.Builder(this@MainActivity).create()
        alertDialogBuilder.setView(promptView)

        //final EditText editFileName = (EditText) promptView.findViewById(R.id.editFileName);
        val textViewSupplierDialog = promptView.findViewById<TextView>(R.id.textViewSettingDialog)

        //textViewSupplierDialog.setText(R.string.supplexier_enter_password)

        val editTextPassword = promptView.findViewById<EditText>(R.id.editTextPassword)
        //val editTextSupplierNumber = promptView.findViewById<EditText>(R.id.editTextSupplierNumber)
        //editTextSupplierName.hint = ""
        //editTextSupplierNumber.visibility = View.GONE



        val btnCancel = promptView.findViewById<Button>(R.id.btnSettingDialogCancel)
        val btnConfirm = promptView.findViewById<Button>(R.id.btnSettingDialogConfirm)
        //val btnDelete = promptView.findViewById<Button>(R.id.btnSupplierDialogDelete)
        //btnDelete.visibility = View.VISIBLE



        alertDialogBuilder.setCancelable(false)

        //btnDelete!!.setOnClickListener {
        //    alertDialogBuilder.dismiss()
        //}

        btnCancel!!.setOnClickListener {
            alertDialogBuilder.dismiss()
        }
        btnConfirm!!.setOnClickListener {

            if (editTextPassword.text.toString() == "Magton!c") {

                val intent = Intent(mContext, WebserviceFtpActivity::class.java)
                startActivity(intent)




            } else {
                toast(getString(R.string.password_mismatch))
            }



            alertDialogBuilder.dismiss()
        }
        alertDialogBuilder.show()
    }
}
