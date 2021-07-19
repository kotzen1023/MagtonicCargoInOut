package com.magtonic.magtoniccargoinout.ui.signature

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.magtonic.magtoniccargoinout.MainActivity
import com.magtonic.magtoniccargoinout.MainActivity.Companion.currentDate
import com.magtonic.magtoniccargoinout.MainActivity.Companion.isShipmentSignatureInDetail
import com.magtonic.magtoniccargoinout.MainActivity.Companion.signatureDetailList
import com.magtonic.magtoniccargoinout.MainActivity.Companion.signatureMultiSignList
import com.magtonic.magtoniccargoinout.R
import com.magtonic.magtoniccargoinout.SignActivity
import com.magtonic.magtoniccargoinout.ui.data.*

import java.text.SimpleDateFormat
import java.util.*

class ShipmentSignatureFragment : Fragment(), LifecycleObserver {
    private val mTAG = ShipmentSignatureFragment::class.java.name
    private var shipmentSignatureContext: Context? = null

    //private var toastHandle: Toast? = null

    private var textViewDate: TextView?= null
    private var datePickBtn: Button?= null
    private var barcodeInput: EditText? = null
    private var linearLayout: LinearLayout? = null
    private var linearLayoutShipmentSignatureHeader: LinearLayout? = null
    private var leftArrow: ImageView? = null
    private var shipmentSignatureHeader: TextView? = null
    private var shipmentSignatureContent: TextView? = null
    private var shipmentSignatureShipmentNo: TextView? = null
    private var viewLine: View? = null
    //private var imageView: ImageView? = null
    private var listViewShipmentSignature: ListView? = null
    private var listViewShipmentSignatureDetail: ListView? = null

    private var progressBar: ProgressBar? = null
    private var relativeLayout: RelativeLayout? = null
    private var mReceiver: BroadcastReceiver? = null
    private var isRegister = false

    var shipmentSignatureList = ArrayList<ShipmentSignatureItem>()
    var shipmentSignatureDetailList = ArrayList<SignatureDetailItem>()
    private var shipmentSignatureItemAdapter: ShipmentSignatureItemAdapter? = null
    private var signatureDetailItemAdapter: SignatureDetailItemAdapter? = null

    private var poBarcode: String = ""
    //private var poLine: String = ""

    private var currentSelectShipmentNo: Int = -1
    private var currentSendOrder: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shipmentSignatureContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d(mTAG, "onCreateView")

        signatureMultiSignList.clear()

        val view = inflater.inflate(R.layout.fragment_shipment_signature, container, false)

        relativeLayout = view.findViewById(R.id.shipment_signature_container)

        linearLayout = view.findViewById(R.id.linearLayoutShipmentSignature)
        progressBar = ProgressBar(shipmentSignatureContext, null, android.R.attr.progressBarStyleLarge)
        val params = RelativeLayout.LayoutParams(MainActivity.screenHeight / 4, MainActivity.screenWidth / 4)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)

        val localRelativeLayout: RelativeLayout? = relativeLayout
        if (localRelativeLayout != null) {
            localRelativeLayout.addView(progressBar, params)
        } else {
            Log.e(mTAG, "localRelativeLayout = null")
        }
        progressBar!!.visibility = View.GONE

        datePickBtn = view.findViewById(R.id.btnDatePickShipmentSignature)
        textViewDate = view.findViewById(R.id.textViewDateShipmentSignature)

        //set today
        currentDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(
            Date()
        )

        Log.e(mTAG, "currentDate = $currentDate")
        textViewDate!!.text = currentDate

        datePickBtn!!.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)



            DatePickerDialog(shipmentSignatureContext as Context, { _, pickYear, pickMonth, pickDay ->
                run {
                    //val format = "你設定的日期為:${setDateFormat(year, month, day)}"

                    //Log.e(mTAG, "format  = $format")
                    //date_text.text = format
                    val date = setDateFormat(pickYear, pickMonth, pickDay)
                    Log.e(mTAG, "date  = $date")
                    textViewDate!!.text = date

                    /*progressBar!!.visibility = View.VISIBLE
                    linearLayoutShipmentSignatureHeader!!.visibility = View.INVISIBLE
                    leftArrow!!.visibility = View.GONE
                    shipmentSignatureHeader!!.visibility = View.VISIBLE
                    shipmentSignatureContent!!.visibility = View.VISIBLE
                    shipmentSignatureShipmentNo!!.visibility = View.GONE
                    viewLine!!.visibility = View.GONE

                    shipmentSignatureList.clear()
                    if (shipmentSignatureItemAdapter != null) {
                        shipmentSignatureItemAdapter!!.notifyDataSetChanged()
                    }

                    val searchIntent = Intent()
                    searchIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SEARCH_NO_ACTION
                    searchIntent.putExtra("INPUT_DATE", date)
                    if (barcodeInput!!.text.toString().isNotEmpty()) {
                        searchIntent.putExtra("INPUT_NO", barcodeInput!!.text.toString().toUpperCase(
                            Locale.getDefault()))
                    } else {
                        searchIntent.putExtra("INPUT_NO", "")
                    }

                    shipmentSignatureContext?.sendBroadcast(searchIntent)
                    */
                }
            }, year, month, day).show()

        }
        barcodeInput = view.findViewById(R.id.editTextShipmentSignatureNo)
        linearLayoutShipmentSignatureHeader = view.findViewById(R.id.linearLayoutShipmentSignatureHeader)
        leftArrow = view.findViewById(R.id.imageViewPrev)
        leftArrow!!.setOnClickListener {
            val backIntent = Intent()
            backIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_BACK_TO_SHIPMENT_NO_LIST
            shipmentSignatureContext!!.sendBroadcast(backIntent)

            val hideIntent = Intent()
            hideIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_HIDE_FAB_BACK
            shipmentSignatureContext!!.sendBroadcast(hideIntent)
        }
        shipmentSignatureHeader = view.findViewById(R.id.shipmentSignatureHeader)
        shipmentSignatureContent = view.findViewById(R.id.shipmentSignatureContent)
        shipmentSignatureShipmentNo = view.findViewById(R.id.shipmentSignatureShipmentNo)

        viewLine = view.findViewById(R.id.viewLine)

        listViewShipmentSignature = view.findViewById(R.id.listViewShipmentSignature)
        listViewShipmentSignature!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            Log.d(mTAG, "click $position")

            currentSelectShipmentNo = position

            shipmentSignatureShipmentNo!!.text = shipmentSignatureList[position].getShipmentNo()

            currentSendOrder = shipmentSignatureList[position].getShipmentNo() as String

            if (shipmentSignatureDetailList.size > 0) {
                shipmentSignatureDetailList.clear()
            }

            if (signatureDetailItemAdapter != null) {
                signatureDetailItemAdapter!!.notifyDataSetChanged()
            }

            val moreDetailIntent = Intent()
            moreDetailIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SEARCH_DETAIL_ACTION
            moreDetailIntent.putExtra("SHIPMENT_NO", shipmentSignatureList[position].getShipmentNo())
            shipmentSignatureContext?.sendBroadcast(moreDetailIntent)


        }
        listViewShipmentSignatureDetail = view.findViewById(R.id.listViewShipmentSignatureDetail)

        if (shipmentSignatureContext != null) {
            shipmentSignatureItemAdapter = ShipmentSignatureItemAdapter(shipmentSignatureContext, R.layout.fragment_shipment_signature_item, shipmentSignatureList)
            listViewShipmentSignature!!.adapter = shipmentSignatureItemAdapter

            signatureDetailItemAdapter = SignatureDetailItemAdapter(shipmentSignatureContext, R.layout.fragment_shipment_signature_detail_item, shipmentSignatureDetailList)
            listViewShipmentSignatureDetail!!.adapter = signatureDetailItemAdapter
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

                    progressBar!!.visibility = View.VISIBLE
                    linearLayoutShipmentSignatureHeader!!.visibility = View.INVISIBLE
                    leftArrow!!.visibility = View.GONE
                    shipmentSignatureHeader!!.visibility = View.VISIBLE
                    shipmentSignatureContent!!.visibility = View.VISIBLE
                    shipmentSignatureShipmentNo!!.visibility = View.GONE
                    viewLine!!.visibility = View.GONE

                    shipmentSignatureList.clear()
                    if (shipmentSignatureItemAdapter != null) {
                        shipmentSignatureItemAdapter!!.notifyDataSetChanged()
                    }

                    val hideIntent = Intent()
                    hideIntent.action = Constants.ACTION.ACTION_HIDE_KEYBOARD
                    shipmentSignatureContext?.sendBroadcast(hideIntent)

                    val searchIntent = Intent()
                    searchIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SEARCH_NO_ACTION
                    searchIntent.putExtra("INPUT_DATE", textViewDate!!.text.toString())
                    if (barcodeInput!!.text.toString().isEmpty()) {
                        searchIntent.putExtra("INPUT_NO", "")
                    } else {
                        searchIntent.putExtra("INPUT_NO", barcodeInput!!.text.toString().uppercase(
                            Locale.getDefault()
                        )
                        )

                    }
                    shipmentSignatureContext?.sendBroadcast(searchIntent)

                    true
                }

                EditorInfo.IME_ACTION_GO -> {
                    Log.e(mTAG, "IME_ACTION_GO")
                    true
                }

                EditorInfo.IME_ACTION_NEXT -> {
                    Log.e(mTAG, "IME_ACTION_NEXT")

                    progressBar!!.visibility = View.VISIBLE
                    linearLayoutShipmentSignatureHeader!!.visibility = View.INVISIBLE
                    leftArrow!!.visibility = View.GONE
                    shipmentSignatureHeader!!.visibility = View.VISIBLE
                    shipmentSignatureContent!!.visibility = View.VISIBLE
                    shipmentSignatureShipmentNo!!.visibility = View.GONE
                    viewLine!!.visibility = View.GONE

                    shipmentSignatureList.clear()
                    if (shipmentSignatureItemAdapter != null) {
                        shipmentSignatureItemAdapter!!.notifyDataSetChanged()
                    }

                    val hideIntent = Intent()
                    hideIntent.action = Constants.ACTION.ACTION_HIDE_KEYBOARD
                    shipmentSignatureContext?.sendBroadcast(hideIntent)

                    val searchIntent = Intent()
                    searchIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SEARCH_NO_ACTION
                    searchIntent.putExtra("INPUT_DATE", textViewDate!!.text.toString())
                    if (barcodeInput!!.text.toString().isEmpty()) {
                        searchIntent.putExtra("INPUT_NO", "")
                    } else {
                        searchIntent.putExtra("INPUT_NO", barcodeInput!!.text.toString().uppercase(
                            Locale.getDefault()
                        )
                        )
                    }
                    shipmentSignatureContext?.sendBroadcast(searchIntent)

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



                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SCAN_BARCODE, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_SCAN_BARCODE")

                        progressBar!!.visibility = View.VISIBLE
                        linearLayoutShipmentSignatureHeader!!.visibility = View.INVISIBLE
                        listViewShipmentSignature!!.visibility = View.VISIBLE
                        listViewShipmentSignatureDetail!!.visibility = View.GONE
                        leftArrow!!.visibility = View.GONE
                        shipmentSignatureHeader!!.visibility = View.VISIBLE
                        shipmentSignatureContent!!.visibility = View.VISIBLE
                        shipmentSignatureShipmentNo!!.visibility = View.GONE
                        viewLine!!.visibility = View.GONE

                        shipmentSignatureList.clear()
                        if (shipmentSignatureItemAdapter != null) {
                            shipmentSignatureItemAdapter!!.notifyDataSetChanged()
                        }

                        poBarcode = intent.getStringExtra("BARCODE") as String

                        barcodeInput!!.setText(poBarcode)

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SEARCH_NO_FAILED, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_SEARCH_NO_FAILED")

                        progressBar!!.visibility = View.GONE

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_FRAGMENT_REFRESH, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_FRAGMENT_REFRESH")

                        linearLayoutShipmentSignatureHeader!!.visibility = View.VISIBLE
                        viewLine!!.visibility = View.VISIBLE
                        listViewShipmentSignature!!.visibility = View.VISIBLE
                        listViewShipmentSignatureDetail!!.visibility = View.GONE
                        shipmentSignatureList.clear()

                        progressBar!!.visibility = View.GONE
                        //hide keyboard
                        if (MainActivity.isKeyBoardShow) {
                            val hideIntent = Intent()
                            hideIntent.action = Constants.ACTION.ACTION_HIDE_KEYBOARD
                            shipmentSignatureContext!!.sendBroadcast(hideIntent)
                        }

                        var statusSum = 0

                        for (rjSignature in MainActivity.signatureList) {
                            val shipmentSignatureItem = ShipmentSignatureItem(rjSignature.data1, rjSignature.data2, rjSignature.data3)

                            statusSum += rjSignature.result.toInt()

                            shipmentSignatureList.add(shipmentSignatureItem)
                        }

                        if (shipmentSignatureItemAdapter != null) {
                            shipmentSignatureItemAdapter?.notifyDataSetChanged()
                        }

                        currentSendOrder = shipmentSignatureList[0].getShipmentNo() as String
                        Log.e(mTAG, "currentSendOrder = $currentSendOrder")

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SEARCH_DETAIL_FAILED, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_SEARCH_DETAIL_FAILED")

                        progressBar!!.visibility = View.GONE

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DETAIL_FRAGMENT_REFRESH, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_DETAIL_FRAGMENT_REFRESH")

                        shipmentSignatureHeader!!.visibility = View.GONE
                        shipmentSignatureContent!!.visibility = View.GONE
                        shipmentSignatureShipmentNo!!.visibility = View.VISIBLE
                        leftArrow!!.visibility = View.VISIBLE

                        listViewShipmentSignature!!.visibility = View.GONE
                        listViewShipmentSignatureDetail!!.visibility = View.VISIBLE

                        progressBar!!.visibility = View.GONE
                        //hide keyboard
                        if (MainActivity.isKeyBoardShow) {
                            val hideIntent = Intent()
                            hideIntent.action = Constants.ACTION.ACTION_HIDE_KEYBOARD
                            shipmentSignatureContext!!.sendBroadcast(hideIntent)
                        }

                        shipmentSignatureDetailList.clear()

                        if (signatureDetailList.size > 0) {
                            //val item0 = IssuanceLookupDetailItem("發料單號", sendOrder as String)
                            //issuanceLookupDetailList.add(item0)
                            val item1 = SignatureDetailItem("出貨單號", signatureDetailList[0].data1)
                            shipmentSignatureDetailList.add(item1)
                            val item2 = SignatureDetailItem("項次", signatureDetailList[0].data2)
                            shipmentSignatureDetailList.add(item2)
                            val item3 = SignatureDetailItem("訂單編號", signatureDetailList[0].data3)
                            shipmentSignatureDetailList.add(item3)
                            val item4 = SignatureDetailItem("訂單項次", signatureDetailList[0].data4)
                            shipmentSignatureDetailList.add(item4)
                            val item5 = SignatureDetailItem("料件編號", signatureDetailList[0].data5)
                            shipmentSignatureDetailList.add(item5)
                            val item6 = SignatureDetailItem("品名", signatureDetailList[0].data6)
                            shipmentSignatureDetailList.add(item6)
                            val item7 = SignatureDetailItem("數量", signatureDetailList[0].data7)
                            shipmentSignatureDetailList.add(item7)


                        }

                        isShipmentSignatureInDetail = 1

                        if (signatureDetailItemAdapter != null) {
                            signatureDetailItemAdapter?.notifyDataSetChanged()
                        }

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_BACK_TO_SHIPMENT_NO_LIST, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_BACK_TO_SHIPMENT_NO_LIST")

                        shipmentSignatureHeader!!.visibility = View.VISIBLE
                        shipmentSignatureContent!!.visibility = View.VISIBLE
                        shipmentSignatureShipmentNo!!.visibility = View.GONE
                        leftArrow!!.visibility = View.GONE

                        listViewShipmentSignature!!.visibility = View.VISIBLE
                        listViewShipmentSignatureDetail!!.visibility = View.GONE

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SHOW_SIGN_DIALOG_ACTION, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_SHOW_SIGN_DIALOG_ACTION")

                        showSignDialog()

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_SIGN_CONFIRM_SUCCESS, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_GUARD_SIGN_UPLOAD_SUCCESS")

                        /*val sendOrder = intent.getStringExtra("SEND_ORDER")

                        toast(getString(R.string.outsourced_process_sign_confirm, sendOrder))

                        Log.d(mTAG, "sendOrder = $sendOrder")


                        for (i in 0 until outsourcedProcessListBySupplier.size) {
                            Log.e(mTAG, outsourcedProcessListBySupplier[i].getData1())
                            if (outsourcedProcessListBySupplier[i].getData2() == sendOrder) {
                                outsourcedProcessListBySupplier[i].setIsSigned(true)
                            }
                            Log.e(mTAG, "outsourcedProcessListBySupplier[$i] = ${outsourcedProcessListBySupplier[i].getIsSigned()}")
                        }

                        listViewBySupplier!!.invalidateViews()

                        val backIntent = Intent()
                        backIntent.action = Constants.ACTION.ACTION_OUTSOURCED_PROCESS_BACK_TO_SUPPLIER_LIST
                        outsourcedProcessContext!!.sendBroadcast(backIntent)

                        val hideIntent = Intent()
                        hideIntent.action = Constants.ACTION.ACTION_OUTSOURCED_PROCESS_HIDE_FAB_BACK
                        outsourcedProcessContext!!.sendBroadcast(hideIntent)*/
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_ADD_SHIPMENT_NO_TO_MULTI_ACTION, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_ADD_SHIPMENT_NO_TO_MULTI_ACTION")

                        if (currentSendOrder.isNotEmpty()) {
                            var found = false

                            for (shipmentNo in signatureMultiSignList) {
                                if (shipmentNo.getShipmentNo() == currentSendOrder) {
                                    found = true
                                    break
                                }
                            }

                            if (!found) {
                                val shipmentSignatureMultiItem = ShipmentSignatureMultiItem(currentSendOrder)
                                signatureMultiSignList.add(shipmentSignatureMultiItem)

                                val addIntent = Intent()
                                addIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_ADD_SHIPMENT_NO_TO_MULTI_SUCCESS
                                shipmentSignatureContext!!.sendBroadcast(addIntent)
                            } else {
                                val existIntent = Intent()
                                existIntent.action = Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_ADD_SHIPMENT_NO_TO_MULTI_EXIST
                                shipmentSignatureContext!!.sendBroadcast(existIntent)
                            }
                        } else {
                            Log.e(mTAG, "Please scan first")
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_SIGN_CONFIRM_COMPLETE, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_SIGN_CONFIRM_COMPLETE")

                        shipmentSignatureList.clear()
                        if (shipmentSignatureItemAdapter != null) {
                            shipmentSignatureItemAdapter?.notifyDataSetChanged()
                        }
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
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SCAN_BARCODE)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SEARCH_NO_FAILED)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_FRAGMENT_REFRESH)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SEARCH_DETAIL_FAILED)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_DETAIL_FRAGMENT_REFRESH)

            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_BACK_TO_SHIPMENT_NO_LIST)

            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_SHOW_SIGN_DIALOG_ACTION)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_GUARD_SIGN_CONFIRM_SUCCESS)
            //multi
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_ADD_SHIPMENT_NO_TO_MULTI_ACTION)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_SIGN_CONFIRM_COMPLETE)

            shipmentSignatureContext?.registerReceiver(mReceiver, filter)
            isRegister = true
            Log.d(mTAG, "registerReceiver mReceiver")
        }



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
                shipmentSignatureContext!!.unregisterReceiver(mReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }

            isRegister = false
            mReceiver = null
            Log.d(mTAG, "unregisterReceiver mReceiver")
        }

        super.onDestroyView()
    }

    /*override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.i(mTAG, "onActivityCreated")
        super.onActivityCreated(savedInstanceState)

    }*/

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onCreated(){
        Log.i(mTAG,"reached the State.Created")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        lifecycle.addObserver(this)
    }

    override fun onDetach() {
        super.onDetach()
        lifecycle.removeObserver(this)
    }

    private fun setDateFormat(year: Int, month: Int, day: Int): String {
        return "$year/${month + 1}/$day"
    }

    private fun showSignDialog() {
        val promptView = View.inflate(shipmentSignatureContext, R.layout.shipment_signature_dialog, null)

        val alertDialogBuilder = AlertDialog.Builder(shipmentSignatureContext).create()
        alertDialogBuilder.setView(promptView)


        val textViewSignatureDialogShipmentNo = promptView.findViewById<TextView>(R.id.textViewSignatureDialogShipmentNo)

        val btnCancel = promptView.findViewById<Button>(R.id.btnSignatureDialogCancel)
        val btnConfirm = promptView.findViewById<Button>(R.id.btnSignatureDialogConfirm)

        textViewSignatureDialogShipmentNo.text = currentSendOrder

        Log.e(mTAG, "Shipment No. = $textViewSignatureDialogShipmentNo")



        btnCancel.text = getString(R.string.cancel)
        btnConfirm.text = getString(R.string.confirm)

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
        btnCancel!!.setOnClickListener {
            /*val noModifyIntent = Intent()
            noModifyIntent.action = Constants.ACTION.ACTION_OUTSOURCED_PROCESS_NO_CHANGED
            noModifyIntent.putExtra("INDEX", position)
            outsourcedProcessContext!!.sendBroadcast(noModifyIntent)*/

            alertDialogBuilder.dismiss()
        }
        btnConfirm!!.setOnClickListener {

            val intent = Intent(shipmentSignatureContext, SignActivity::class.java)
            intent.putExtra("SEND_ORDER", currentSendOrder)
            intent.putExtra("TITLE", getString(R.string.nav_signature))
            intent.putExtra("SEND_FRAGMENT", "SHIPMENT_SIGNATURE_FRAGMENT")
            startActivity(intent)


            alertDialogBuilder.dismiss()
        }
        alertDialogBuilder.show()


    }
}