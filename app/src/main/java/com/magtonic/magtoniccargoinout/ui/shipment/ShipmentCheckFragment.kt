package com.magtonic.magtoniccargoinout.ui.shipment

import android.app.Activity
import android.content.*
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.magtonic.magtoniccargoinout.MainActivity
import com.magtonic.magtoniccargoinout.MainActivity.Companion.db
import com.magtonic.magtoniccargoinout.MainActivity.Companion.historyList
import com.magtonic.magtoniccargoinout.MainActivity.Companion.shipmentList
import com.magtonic.magtoniccargoinout.R
import com.magtonic.magtoniccargoinout.persistence.History
import com.magtonic.magtoniccargoinout.ui.data.*
import com.magtonic.magtoniccargoinout.ui.home.HomeFragment
import com.magtonic.magtoniccargoinout.ui.ocr.OcrFragment
import java.text.SimpleDateFormat
import java.util.*

class ShipmentCheckFragment : Fragment() {
    private val mTAG = ShipmentCheckFragment::class.java.name
    private var shipmentCheckContext: Context? = null

    private var toastHandle: Toast? = null


    private var barcodeInput: EditText? = null
    private var linearLayout: LinearLayout? = null
    private var imageView: ImageView? = null
    private var listViewShipmentCheck: ListView? = null

    private var progressBar: ProgressBar? = null
    private var relativeLayout: RelativeLayout? = null
    private var mReceiver: BroadcastReceiver? = null
    private var isRegister = false

    var shipmentCheckList = ArrayList<ShipmenCheckItem>()
    private var shipmentCheckItemAdapter: ShipmenCheckItemAdapter? = null

    private var poBarcode: String = ""
    private var poLine: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shipmentCheckContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d(mTAG, "onCreateView")

        val view = inflater.inflate(R.layout.fragment_shipment_check, container, false)

        relativeLayout = view.findViewById(R.id.shipment_check_list_container)

        linearLayout = view.findViewById(R.id.linearLayoutShipmentCheck)
        progressBar = ProgressBar(shipmentCheckContext, null, android.R.attr.progressBarStyleLarge)
        val params = RelativeLayout.LayoutParams(MainActivity.screenHeight / 4, MainActivity.screenWidth / 4)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)

        val localRelativeLayout: RelativeLayout? = relativeLayout
        if (localRelativeLayout != null) {
            localRelativeLayout.addView(progressBar, params)
        } else {
            Log.e(mTAG, "localRelativeLayout = null")
        }
        progressBar!!.visibility = View.GONE

        barcodeInput = view.findViewById(R.id.editTextShipmentCheck)

        imageView = view.findViewById(R.id.resultImage)
        imageView!!.visibility = View.GONE

        listViewShipmentCheck = view.findViewById(R.id.listViewShipmentCheck)

        if (shipmentCheckContext != null) {
            shipmentCheckItemAdapter = ShipmenCheckItemAdapter(shipmentCheckContext, R.layout.fragment_shipment_check_item, shipmentCheckList)
            listViewShipmentCheck!!.adapter = shipmentCheckItemAdapter
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
                    imageView!!.visibility = View.GONE

                    val hideIntent = Intent()
                    hideIntent.action = Constants.ACTION.ACTION_HIDE_KEYBOARD
                    shipmentCheckContext?.sendBroadcast(hideIntent)

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

                    val searchIntent = Intent()
                    searchIntent.action = Constants.ACTION.ACTION_SHIPMENT_CHECK_ACTION
                    searchIntent.putExtra("INPUT_NO", barcodeInput!!.text.toString().toUpperCase(
                        Locale.getDefault()))
                    shipmentCheckContext?.sendBroadcast(searchIntent)


                    true
                }

                EditorInfo.IME_ACTION_GO -> {
                    Log.e(mTAG, "IME_ACTION_GO")
                    true
                }

                EditorInfo.IME_ACTION_NEXT -> {
                    Log.e(mTAG, "IME_ACTION_NEXT")

                    progressBar!!.visibility = View.VISIBLE
                    imageView!!.visibility = View.GONE

                    val hideIntent = Intent()
                    hideIntent.action = Constants.ACTION.ACTION_HIDE_KEYBOARD
                    shipmentCheckContext?.sendBroadcast(hideIntent)

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



                    val searchIntent = Intent()
                    searchIntent.action = Constants.ACTION.ACTION_SHIPMENT_CHECK_ACTION
                    searchIntent.putExtra("INPUT_NO", barcodeInput!!.text.toString().toUpperCase(
                        Locale.getDefault()))
                    shipmentCheckContext?.sendBroadcast(searchIntent)

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



                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SCAN_BARCODE, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SCAN_BARCODE")

                        poBarcode = intent.getStringExtra("BARCODE") as String
                        poLine = intent.getStringExtra("LINE") as String
                        barcodeInput!!.setText(poBarcode)

                        imageView!!.visibility = View.GONE

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_FRAGMENT_REFRESH, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_FRAGMENT_REFRESH")

                        val barcode = intent.getStringExtra("BARCODE")

                        var history: History
                        val c  = Calendar.getInstance(Locale.getDefault())
                        //val dateTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        val dateTime = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val dateString = date.format(c.time)
                        val dateTimeString = dateTime.format(c.time)

                        shipmentCheckList.clear()

                        progressBar!!.visibility = View.GONE
                        //hide keyboard
                        if (MainActivity.isKeyBoardShow) {
                            val hideIntent = Intent()
                            hideIntent.action = Constants.ACTION.ACTION_HIDE_KEYBOARD
                            shipmentCheckContext!!.sendBroadcast(hideIntent)
                        }

                        var statusSum = 0

                        for (rjShipment in shipmentList) {
                            val shipmentCheckItem = ShipmenCheckItem(rjShipment.result, rjShipment.result2)

                            statusSum += shipmentCheckItem.getStatus()!!.toInt()

                            shipmentCheckList.add(shipmentCheckItem)
                        }

                        Log.e(mTAG, "statusSum = $statusSum")

                        if (shipmentCheckList.size > 0) {
                            if (statusSum == 0) {
                                imageView!!.setImageResource(R.drawable.circle_green)
                            } else {
                                imageView!!.setImageResource(R.drawable.cross_red)
                            }
                            imageView!!.visibility = View.VISIBLE
                        } else {
                            imageView!!.visibility = View.GONE
                        }

                        val firstFour = barcode!!.substring(0, 4)

                        if (firstFour == "AX30") {
                            //db
                            if (db != null) {
                                history = db!!.historyDao().getHistoryByBarcode(barcode as String)

                                if (history != null) {
                                    Log.e(mTAG, "shipmentList.size = ${shipmentList.size}")
                                    history.setWorkOrderState(shipmentList[0].result)
                                    history.setWorkOrderDesc(shipmentList[0].result2)
                                    Log.e(mTAG, "shipmentList[0].result = ${shipmentList[0].result}, shipmentList[0].result2 = ${shipmentList[0].result2}")
                                    if (shipmentList.size == 2) {
                                        Log.e(mTAG, "shipmentList[1].result = ${shipmentList[1].result}, shipmentList[1].result2 = ${shipmentList[1].result2}")
                                        history.setShipmentState(shipmentList[1].result)
                                        history.setShipmentDesc(shipmentList[1].result2)
                                    } else {
                                        history.setShipmentState("")
                                        history.setShipmentDesc("")
                                    }


                                    history.setDatetime(dateTimeString)
                                    history.setDate(dateString)
                                    db!!.historyDao().update(history)
                                } else {
                                    if (shipmentList.size == 2) {
                                        history = History(
                                            barcode,
                                            shipmentList[0].result,
                                            shipmentList[0].result2,
                                            shipmentList[1].result,
                                            shipmentList[1].result2,
                                            dateTimeString,
                                            dateString
                                        )
                                    } else {
                                        history = History(
                                            barcode,
                                            shipmentList[0].result,
                                            shipmentList[0].result2,
                                            "",
                                            "",
                                            dateTimeString,
                                            dateString
                                        )
                                    }

                                    db!!.historyDao().insert(history)
                                }


                                //db!!.historyDao().update(history)



                            } else {
                                Log.e(mTAG, "db = null")
                            }
                        } else {
                            Log.e(mTAG, "Not AX order")
                        }




                        if (shipmentCheckItemAdapter != null) {
                            shipmentCheckItemAdapter?.notifyDataSetChanged()
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
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SCAN_BARCODE)
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_FRAGMENT_REFRESH)


            //filter.addAction(Constants.ACTION.ACTION_RECEIPT_ALREADY_UPLOADED_SEND_TO_FRAGMENT)
            shipmentCheckContext?.registerReceiver(mReceiver, filter)
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
                shipmentCheckContext!!.unregisterReceiver(mReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }

            isRegister = false
            mReceiver = null
            Log.d(mTAG, "unregisterReceiver mReceiver")
        }

        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.i(mTAG, "onActivityCreated")
        super.onActivityCreated(savedInstanceState)

    }

    private fun toast(message: String) {

        if (toastHandle != null)
            toastHandle!!.cancel()

        val toast = Toast.makeText(shipmentCheckContext, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, 0)
        val group = toast.view as ViewGroup
        val textView = group.getChildAt(0) as TextView
        textView.textSize = 30.0f
        toast.show()

        toastHandle = toast
    }


}