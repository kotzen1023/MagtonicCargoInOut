package com.magtonic.magtoniccargoinout.ui.home

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.Fragment

import com.magtonic.magtoniccargoinout.MainActivity
import com.magtonic.magtoniccargoinout.MainActivity.Companion.currentPlant
import com.magtonic.magtoniccargoinout.MainActivity.Companion.guestListA
import com.magtonic.magtoniccargoinout.MainActivity.Companion.guestListB
import com.magtonic.magtoniccargoinout.MainActivity.Companion.guestListT
import com.magtonic.magtoniccargoinout.R
import com.magtonic.magtoniccargoinout.ui.data.Constants
import com.magtonic.magtoniccargoinout.ui.data.GuestDetailItem
import com.magtonic.magtoniccargoinout.ui.data.GuestDetailItemAdapter

import java.util.*

class HomeFragment : Fragment() {

    private val mTAG = HomeFragment::class.java.name

    private var progressBar: ProgressBar? = null
    private var relativeLayout: RelativeLayout? = null
    private var guestDetailItemAdapter: GuestDetailItemAdapter? = null
    private var barcodeInput: EditText? = null
    private var linearLayout: LinearLayout? = null

    var guestDetailList = ArrayList<GuestDetailItem>()

    private var listView: ListView?= null

    private var factoryList: ArrayList<String> = ArrayList()

    private var mReceiver: BroadcastReceiver? = null
    private var isRegister = false

    private var toastHandle: Toast? = null

    private val colorCodeBlue = Color.parseColor("#1976D2")
    private var poBarcode: String = ""
    private var poLine: String = ""

    //private lateinit var homeViewModel: HomeViewModel

    companion object {
        private var handler: MyHandler? = null
        private var guestContext: Context? = null

        private var mTimer: Timer? = null
        private var mTimerTask //計時任務，判斷是否未操作時間到達3s
                : MyTimerTask? = null
        private var mLastActionTime //上一次操作時間
                : Long = 0



        private class MyTimerTask : TimerTask() {
            override fun run() { //3s未操作,則結束計時
                if (System.currentTimeMillis() - mLastActionTime > 60000) { //結束計時
                    removeTimer()
                    // 停止計時任務
                    stopTimer()

                    val getIntent = Intent()
                    getIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_LIST_ACTION
                    guestContext!!.sendBroadcast(getIntent)

                } else {
                    System.currentTimeMillis() - mLastActionTime
                    //Log.e("=====", "${(System.currentTimeMillis() - mLastActionTime)/1000}")
                }
            }
        }

        /*private var handler: Handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what === 1) { //回到主執行緒執行結束操作
                    //Log.e("=====", "結束計時")

                    val getIntent = Intent()
                    getIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_LIST_ACTION
                    guestContext!!.sendBroadcast(getIntent)
                }
            }
        }*/

        //private class MyHandler(context: Context) : Handler() {
        private class MyHandler : Handler() {
            //private var mFragment: WeakReference<Context> = WeakReference(context)

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.what == 1) { //回到主執行緒執行結束操作
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        guestContext = context

        handler = MyHandler()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d(mTAG, "onCreateView")

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        relativeLayout = view.findViewById(R.id.guest_list_container)
        linearLayout = view.findViewById(R.id.linearLayoutGuest)
        progressBar = ProgressBar(guestContext, null, android.R.attr.progressBarStyleLarge)
        val params = RelativeLayout.LayoutParams(MainActivity.screenHeight / 4, MainActivity.screenWidth / 4)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)

        val localRelativeLayout: RelativeLayout? = relativeLayout
        if (localRelativeLayout != null) {
            localRelativeLayout.addView(progressBar, params)
        } else {
            Log.e(mTAG, "localRelativeLayout = null")
        }
        progressBar!!.visibility = View.GONE
        barcodeInput = view.findViewById(R.id.editTextGuest)

        val spinner = view.findViewById<Spinner>(R.id.guestItemSpinner)

        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.e(mTAG, "position: $position")

                stopTimer()

                progressBar!!.visibility = View.VISIBLE

                guestDetailList.clear()
                if (guestDetailItemAdapter != null) {
                    guestDetailItemAdapter?.notifyDataSetChanged()
                }

                currentPlant =
                    when(position) {
                        1 -> "A"
                        2 -> "B"
                        else -> "T"
                    }



                val getIntent = Intent()
                getIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_LIST_ACTION
                guestContext!!.sendBroadcast(getIntent)

                /*val getIntent = Intent()
                getIntent.action = Constants.ACTION.ACTION_GUEST_GET_CURRENT_PLANT_GUEST_LIST
                getIntent.putExtra("PLANT", currentPlant)
                guestContext!!.sendBroadcast(getIntent)*/
            }

        }

        factoryList.add("本廠")
        factoryList.add("A廠")
        factoryList.add("B廠")

        val factoryAdapter = ArrayAdapter(guestContext as Context, R.layout.myspinner, factoryList)
        spinner.adapter = factoryAdapter

        when(currentPlant) {
            "A" -> spinner.setSelection(1)
            "B" -> spinner.setSelection(2)
            else -> spinner.setSelection(0)
        }

        listView = view!!.findViewById(R.id.listViewGuest)
        listView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            Log.d(mTAG, "click $position")

        }

        if (guestContext != null) {


            guestDetailItemAdapter = GuestDetailItemAdapter(guestContext, R.layout.fragment_guest_item, guestDetailList)
            //listView.setAdapter(receiptDetailItemAdapter)
            listView!!.adapter = guestDetailItemAdapter
        }

        linearLayout!!.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            linearLayout!!.getWindowVisibleDisplayFrame(r)
            val screenHeight = linearLayout!!.rootView.height
            val keypadHeight = screenHeight - r.bottom
            MainActivity.isKeyBoardShow = (keypadHeight > screenHeight * 0.15)
        }

        barcodeInput!!.setOnEditorActionListener { _, actionId, _ ->

            when(actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    Log.e(mTAG, "IME_ACTION_DONE")

                    val hideIntent = Intent()
                    hideIntent.action = Constants.ACTION.ACTION_HIDE_KEYBOARD
                    guestContext?.sendBroadcast(hideIntent)

                    /*val guest = findGuestInAllList(barcodeInput!!.text.toString())

                    if (guest.data1.isNotEmpty() && guest.data2.isNotEmpty()) {
                        showGuestInDialog(false, barcodeInput!!.text.toString(), guest.data1)
                    } else {
                        showGuestInDialog(true, barcodeInput!!.text.toString(), "")
                    }*/



                    /*progressBar!!.visibility = View.VISIBLE

                    guestDetailList.clear()
                    if (guestDetailItemAdapter != null) {
                        guestDetailItemAdapter?.notifyDataSetChanged()
                    }*/





                    true
                }

                EditorInfo.IME_ACTION_GO -> {
                    Log.e(mTAG, "IME_ACTION_GO")
                    true
                }

                EditorInfo.IME_ACTION_NEXT -> {
                    Log.e(mTAG, "IME_ACTION_NEXT")

                    val hideIntent = Intent()
                    hideIntent.action = Constants.ACTION.ACTION_HIDE_KEYBOARD
                    guestContext?.sendBroadcast(hideIntent)

                    /*val guest = findGuestInAllList(barcodeInput!!.text.toString())

                    if (guest.data1.isNotEmpty() && guest.data2.isNotEmpty()) {
                        showGuestInDialog(false, barcodeInput!!.text.toString(), guest.data1)
                    } else {
                        showGuestInDialog(true, barcodeInput!!.text.toString(), "")
                    }*/
                    /*progressBar!!.visibility = View.VISIBLE

                    guestDetailList.clear()
                    if (guestDetailItemAdapter != null) {
                        guestDetailItemAdapter?.notifyDataSetChanged()
                    }*/



                    /*val searchIntent = Intent()
                    searchIntent.action = Constants.ACTION.ACTION_USER_INPUT_SEARCH
                    searchIntent.putExtra("INPUT_NO", barcodeInput!!.text.toString().toUpperCase(
                        Locale.getDefault()))
                    guestContext?.sendBroadcast(searchIntent)*/

                    true
                }

                EditorInfo.IME_ACTION_SEND -> {
                    Log.e(mTAG, "IME_ACTION_SEND")
                    true
                }

                else -> {
                    false
                }
            }




        }

        val filter: IntentFilter

        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != null) {
                    if (intent.action!!.equals(Constants.ACTION.ACTION_BARCODE_NULL, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_BARCODE_NULL")



                        progressBar!!.visibility = View.GONE


                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_NETWORK_FAILED, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_NETWORK_FAILED")

                        progressBar!!.visibility = View.GONE



                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_CONNECTION_TIMEOUT, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_CONNECTION_TIMEOUT")

                        progressBar!!.visibility = View.GONE



                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SERVER_ERROR, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SERVER_ERROR")

                        progressBar!!.visibility = View.GONE



                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GUEST_SCAN_BARCODE, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_GUEST_SCAN_BARCODE")

                        poBarcode = intent.getStringExtra("BARCODE") as String
                        poLine = intent.getStringExtra("LINE") as String
                        barcodeInput!!.setText(poBarcode)

                        stopTimer()

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GUEST_FRAGMENT_REFRESH, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_GUEST_FRAGMENT_REFRESH")

                        guestDetailList.clear()

                        progressBar!!.visibility = View.GONE
                        //hide keyboard
                        if (MainActivity.isKeyBoardShow) {
                            val hideIntent = Intent()
                            hideIntent.action = Constants.ACTION.ACTION_HIDE_KEYBOARD
                            guestContext!!.sendBroadcast(hideIntent)
                        }

                        when(currentPlant) {
                            "A" -> {
                                for (rjGuest in guestListA) {
                                    //val date = rjGuest.data3.split(" ")

                                    //val timeString = date[0]+"\n"+rjGuest.data4
                                    val guestDetailItem = GuestDetailItem(rjGuest.data1, rjGuest.data2, rjGuest.data3, rjGuest.data4, rjGuest.data5)
                                    guestDetailList.add(guestDetailItem)
                                }
                            }
                            "B" -> {
                                for (rjGuest in guestListB) {
                                    //val date = rjGuest.data3.split(" ")

                                    //val timeString = date[0]+"\n"+rjGuest.data4
                                    val guestDetailItem = GuestDetailItem(rjGuest.data1, rjGuest.data2, rjGuest.data3, rjGuest.data4, rjGuest.data5)
                                    guestDetailList.add(guestDetailItem)
                                }
                            }
                            else -> {
                                for (rjGuest in guestListT) {
                                    //val date = rjGuest.data3.split(" ")

                                    //val timeString = date[0]+"\n"+rjGuest.data4
                                    val guestDetailItem = GuestDetailItem(rjGuest.data1, rjGuest.data2, rjGuest.data3, rjGuest.data4, rjGuest.data5)
                                    guestDetailList.add(guestDetailItem)
                                }
                            }
                        }



                        if (guestDetailItemAdapter != null) {
                            guestDetailItemAdapter?.notifyDataSetChanged()
                        }

                        startTimer()

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GUEST_LIST_CLEAR, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_GUEST_LIST_CLEAR")

                        guestDetailList.clear()

                        progressBar!!.visibility = View.GONE
                        //hide keyboard
                        if (MainActivity.isKeyBoardShow) {
                            val hideIntent = Intent()
                            hideIntent.action = Constants.ACTION.ACTION_HIDE_KEYBOARD
                            guestContext!!.sendBroadcast(hideIntent)
                        }

                        if (guestDetailItemAdapter != null) {
                            guestDetailItemAdapter?.notifyDataSetChanged()
                        }

                        startTimer()
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GUEST_IN_OR_LEAVE_FAILED, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_GUEST_IN_OR_LEAVE_FAILED")

                        progressBar!!.visibility = View.GONE

                        startTimer()
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GUEST_IN_OR_LEAVE_SUCCESS, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_GUEST_IN_OR_LEAVE_SUCCESS")

                        val getIntent = Intent()
                        getIntent.action = Constants.ACTION.ACTION_GUEST_SEARCH_GUEST_LIST_ACTION
                        guestContext!!.sendBroadcast(getIntent)

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GUEST_SHOW_LEAVE_ACTION, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_GUEST_SHOW_LEAVE_ACTION")

                        val plant = intent.getStringExtra("PLANT")
                        val guestNo = intent.getStringExtra("GUEST_NO")
                        val inDate = intent.getStringExtra("IN_DATE")
                        val inTime = intent.getStringExtra("IN_TIME")


                        showGuestInDialog(plant as String, guestNo as String, inDate as String, inTime as String)
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_RECEIPT_FRAGMENT_REFRESH, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_RECEIPT_FRAGMENT_REFRESH")


                        //progressBar!!.visibility = View.GONE
                        //hide keyboard
                        if (MainActivity.isKeyBoardShow) {
                            val hideIntent = Intent()
                            hideIntent.action = Constants.ACTION.ACTION_HIDE_KEYBOARD
                            guestContext!!.sendBroadcast(hideIntent)
                        }


                        Log.e(mTAG, "pmm09 = " + MainActivity.itemReceipt!!.rjReceipt!!.pmm09 +"pmc03 = "+MainActivity.itemReceipt!!.rjReceipt!!.pmc03)

                        toast(MainActivity.itemReceipt!!.rjReceipt!!.pmm09 +"-"+MainActivity.itemReceipt!!.rjReceipt!!.pmc03)

                        //val barcode: String = intent.getStringExtra("BARCODE") as String
                        //barcodeInput!!.setText(MainActivity.itemReceipt!!.rjReceipt!!.pmm09)

                        //val guest = findGuestInAllList(MainActivity.itemReceipt!!.rjReceipt!!.pmm09)


                        val guestInIntent = Intent()
                        guestInIntent.action = Constants.ACTION.ACTION_GUEST_IN_OR_LEAVE_ACTION
                        guestInIntent.putExtra("DATA1", "0") // In
                        guestInIntent.putExtra("DATA2", currentPlant) //廠區
                        guestInIntent.putExtra("DATA3", MainActivity.itemReceipt!!.rjReceipt!!.pmm09) //供應商編號
                        guestInIntent.putExtra("DATA4", poBarcode) //採購單號
                        guestInIntent.putExtra("DATA5", poLine) //項次
                        guestInIntent.putExtra("DATA6", "") //刷進留空
                        guestContext!!.sendBroadcast(guestInIntent)

                        /*if (guest.data1.isNotEmpty() && guest.data2.isNotEmpty()) {
                            showGuestInDialog(false, MainActivity.itemReceipt!!.rjReceipt!!.pmm09, guest.data1)
                        } else {
                            showGuestInDialog(true, MainActivity.itemReceipt!!.rjReceipt!!.pmm09, "")
                        }*/

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_RECEIPT_NO_NOT_EXIST, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_RECEIPT_NO_NOT_EXIST")

                        progressBar!!.visibility = View.GONE

                        startTimer()
                    }

                }
            }
        }

        if (!isRegister) {
            filter = IntentFilter()
            filter.addAction(Constants.ACTION.ACTION_BARCODE_NULL)
            filter.addAction(Constants.ACTION.ACTION_NETWORK_FAILED)
            filter.addAction(Constants.ACTION.ACTION_CONNECTION_TIMEOUT)
            filter.addAction(Constants.ACTION.ACTION_SERVER_ERROR)
            filter.addAction(Constants.ACTION.ACTION_GUEST_SCAN_BARCODE)
            filter.addAction(Constants.ACTION.ACTION_GUEST_FRAGMENT_REFRESH)
            filter.addAction(Constants.ACTION.ACTION_GUEST_LIST_CLEAR)
            filter.addAction(Constants.ACTION.ACTION_GUEST_IN_OR_LEAVE_FAILED)
            filter.addAction(Constants.ACTION.ACTION_GUEST_IN_OR_LEAVE_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_GUEST_SHOW_LEAVE_ACTION)
            filter.addAction(Constants.ACTION.ACTION_RECEIPT_FRAGMENT_REFRESH)
            filter.addAction(Constants.ACTION.ACTION_RECEIPT_NO_NOT_EXIST)

            //filter.addAction(Constants.ACTION.ACTION_RECEIPT_ALREADY_UPLOADED_SEND_TO_FRAGMENT)
            guestContext?.registerReceiver(mReceiver, filter)
            isRegister = true
            Log.d(mTAG, "registerReceiver mReceiver")
        }

        /*val getIntent = Intent()
        getIntent.action = Constants.ACTION.ACTION_GUEST_GET_CURRENT_PLANT_GUEST_LIST
        getIntent.putExtra("PLANT", currentPlant)
        guestContext!!.sendBroadcast(getIntent)*/

        mLastActionTime = System.currentTimeMillis()

        return view
    }

    override fun onDestroy() {
        Log.i(mTAG, "onDestroy")

        super.onDestroy()
    }

    override fun onDestroyView() {
        Log.i(mTAG, "onDestroyView")

        if (isRegister && mReceiver != null) {
            try {
                guestContext!!.unregisterReceiver(mReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }

            isRegister = false
            mReceiver = null
            Log.d(mTAG, "unregisterReceiver mReceiver")
        }

        stopTimer()

        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.i(mTAG, "onActivityCreated")
        super.onActivityCreated(savedInstanceState)

    }

    private fun toast(message: String) {

        if (toastHandle != null)
            toastHandle!!.cancel()

        val toast = Toast.makeText(guestContext, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, 0)
        val group = toast.view as ViewGroup
        val textView = group.getChildAt(0) as TextView
        textView.textSize = 30.0f
        toast.show()

        toastHandle = toast
    }

    private fun showGuestInDialog(plant: String, guestNo: String, inDate: String, inTime: String) {

        Log.e(mTAG, "=== showGuestInDialog start ===")

        Log.e(mTAG, "guestNo = $guestNo, current_plant = $currentPlant, inDate = $inDate, inTime = $inTime")

        // get prompts.xml view
        /*LayoutInflater layoutInflater = LayoutInflater.from(Nfc_read_app.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);*/
        val promptView = View.inflate(guestContext, R.layout.guest_in_or_out_dialog, null)

        val alertDialogBuilder = AlertDialog.Builder(guestContext).create()
        alertDialogBuilder.setView(promptView)

        //final EditText editFileName = (EditText) promptView.findViewById(R.id.editFileName);
        val textViewGuestDialog = promptView.findViewById<TextView>(R.id.textViewGuestDialog)
        val spinnerGuestDialog = promptView.findViewById<Spinner>(R.id.spinnerGuestDialog)
        val textViewNoGuestDialog = promptView.findViewById<TextView>(R.id.textViewNoGuestDialog)
        val btnCancelGuestDialog = promptView.findViewById<Button>(R.id.btnCancelGuestDialog)
        val btnConfirmGuestDialog = promptView.findViewById<Button>(R.id.btnConfirmGuestDialog)
        val textViewInDate = promptView.findViewById<TextView>(R.id.textViewInDate)
        val textViewInTime = promptView.findViewById<TextView>(R.id.textViewInTime)

        textViewGuestDialog.text = getString(R.string.guest_in_title)

        val factoryAdapter = ArrayAdapter(guestContext as Context, R.layout.myspinner, factoryList)
        spinnerGuestDialog.adapter = factoryAdapter

        var inPlant = currentPlant

        if (plant.isNotEmpty()) {
            inPlant = plant

            when(plant) {
                "A" -> spinnerGuestDialog.setSelection(1)
                "B" -> spinnerGuestDialog.setSelection(2)
                else -> spinnerGuestDialog.setSelection(0)
            }
        } else {
            when(currentPlant) {
                "A" -> spinnerGuestDialog.setSelection(1)
                "B" -> spinnerGuestDialog.setSelection(2)
                else -> spinnerGuestDialog.setSelection(0)
            }
        }





        spinnerGuestDialog?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.e(mTAG, "position: $position")

                inPlant = when(position) {
                    1 -> "A"
                    2 -> "B"
                    else -> "T"
                }

            }

        }



        textViewNoGuestDialog.text = guestNo
        textViewNoGuestDialog.setTextColor(colorCodeBlue)

        textViewInDate.text = inDate
        textViewInDate.setTextColor(colorCodeBlue)
        textViewInTime.text = inTime
        textViewInTime.setTextColor(colorCodeBlue)

        btnCancelGuestDialog.text = getString(R.string.cancel)
        //if (isIn)
        //    btnConfirmGuestDialog.text = getString(R.string.btn_guest_in)
        //else
        btnConfirmGuestDialog.text = getString(R.string.btn_guest_leave)

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
        btnCancelGuestDialog!!.setOnClickListener {
            alertDialogBuilder.dismiss()
        }
        btnConfirmGuestDialog!!.setOnClickListener {

            progressBar!!.visibility = View.VISIBLE

            val guestInIntent = Intent()
            guestInIntent.action = Constants.ACTION.ACTION_GUEST_IN_OR_LEAVE_ACTION
            guestInIntent.putExtra("DATA1", "2") //out
            guestInIntent.putExtra("DATA2", inPlant) //plant
            guestInIntent.putExtra("DATA3", guestNo) //plant
            guestInIntent.putExtra("DATA4", "") //pmn01
            guestInIntent.putExtra("DATA5", "") //pmn02
            guestInIntent.putExtra("DATA6", inDate) //plant
            guestContext!!.sendBroadcast(guestInIntent)

            alertDialogBuilder.dismiss()
        }
        alertDialogBuilder.show()

        Log.e(mTAG, "=== showGuestInDialog end ===")
    }
}