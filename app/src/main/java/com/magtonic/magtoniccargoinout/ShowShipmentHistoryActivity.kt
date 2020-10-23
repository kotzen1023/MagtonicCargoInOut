package com.magtonic.magtoniccargoinout

import android.app.DatePickerDialog

import android.content.Context
import android.content.Intent

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem

import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import com.magtonic.magtoniccargoinout.data.HistoryAdapter
import com.magtonic.magtoniccargoinout.persistence.History

import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ShowShipmentHistoryActivity : AppCompatActivity() {
    private val mTAG = ShowShipmentHistoryActivity::class.java.name

    private var mContext: Context? = null

    //private var mReceiver: BroadcastReceiver? = null
    //private var isRegister = false

    private var textViewDate: TextView?= null
    private var datePickBtn: Button?= null

    private var listView: ListView? = null
    private var historyAdapter: HistoryAdapter? = null
    private var dateSelectHistoryList: ArrayList<History> ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shipmentcheck_history)

        mContext = applicationContext
        listView = findViewById(R.id.listViewShipmentHistory)
        datePickBtn = findViewById(R.id.btnDatePick)
        textViewDate = findViewById(R.id.textViewDate)

        datePickBtn!!.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(this, { _, year, month, day ->
                run {
                    //val format = "你設定的日期為:${setDateFormat(year, month, day)}"

                    //Log.e(mTAG, "format  = $format")
                    //date_text.text = format
                    val date = setDateFormat(year, month, day)
                    Log.e(mTAG, "date  = $date")
                    textViewDate!!.text = date

                    if (dateSelectHistoryList != null) {
                        dateSelectHistoryList!!.clear()
                    } else {
                        dateSelectHistoryList = ArrayList()
                    }

                    if (historyAdapter != null) {
                        historyAdapter?.notifyDataSetChanged()

                    }

                    if (MainActivity.db != null) {


                        dateSelectHistoryList = MainActivity.db!!.historyDao().getHistoryByDate(date) as ArrayList<History>

                        Log.e(mTAG, "size = ${dateSelectHistoryList!!.size}")

                        if (dateSelectHistoryList!!.size > 0) {

                            dateSelectHistoryList = dateSelectHistoryList!!.sortedBy { it.getId() }.reversed() as ArrayList<History>

                        }



                        historyAdapter = dateSelectHistoryList?.let {
                            HistoryAdapter(mContext, R.layout.shipmentcheck_list_item,
                                it
                            )
                        }
                        listView!!.adapter = historyAdapter
                    }




                }
            }, year, month, day).show()
        }





        if (mContext != null) {

            val currentDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                Date()
            )

            Log.e(mTAG, "currentDate = $currentDate")
            textViewDate!!.text = currentDate
            //val item0 = ReceiptConfirmFailLog("123", "AP22-19030050", "2020-06-01", "10:10:10", "test")
            //confirmFailLogList.add(item0)

            if (MainActivity.db != null) {
                if (dateSelectHistoryList != null) {
                    dateSelectHistoryList!!.clear()
                } else {
                    dateSelectHistoryList = ArrayList()
                }
            }

            dateSelectHistoryList = MainActivity.db!!.historyDao().getHistoryByDate(currentDate) as ArrayList<History>

            if (dateSelectHistoryList != null && dateSelectHistoryList!!.size > 0) {
                dateSelectHistoryList = dateSelectHistoryList!!.sortedBy { it.getTimeStamp() }.reversed() as ArrayList<History>
            }
            //Collections.reverse(dateSelectHistoryList)

            for (i in 0 until dateSelectHistoryList!!.size) {
                Log.e(mTAG, "dateSelectHistoryList[$i] = ${dateSelectHistoryList!![i].getBarcode()} timestamp = ${dateSelectHistoryList!![i].getTimeStamp()} ")
            }


            historyAdapter = dateSelectHistoryList?.let {
                HistoryAdapter(mContext, R.layout.shipmentcheck_list_item,
                    it
                )
            }
            listView!!.adapter = historyAdapter


        }

        //for action bar
        val actionBar: androidx.appcompat.app.ActionBar? = supportActionBar

        if (actionBar != null) {

            actionBar.setDisplayUseLogoEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = getString(R.string.shipment_history)

        }


        /*val filter: IntentFilter

        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != null) {
                    if (intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SEARCH_HISTORY_BY_DATE_ACTION, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SHIPMENT_SEARCH_HISTORY_BY_DATE_ACTION")

                        val date = intent.getStringExtra("DATE")



                    }

                }
            }
        }

        if (!isRegister) {
            filter = IntentFilter()
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SEARCH_HISTORY_BY_DATE_ACTION)
            mContext?.registerReceiver(mReceiver, filter)
            isRegister = true
            Log.d(mTAG, "registerReceiver mReceiver")
        }*/
    }

    override fun onDestroy() {
        Log.i(mTAG, "onDestroy")

        /*if (isRegister && mReceiver != null) {
            try {
                mContext!!.unregisterReceiver(mReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }

            isRegister = false
            mReceiver = null
            Log.d(mTAG, "unregisterReceiver mReceiver")
        }*/

        super.onDestroy()
    }

    override fun onResume() {
        Log.i(mTAG, "onResume")
        super.onResume()
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

        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //menuInflater.inflate(R.menu.sign, menu)



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
        }


        return true
    }

    private fun setDateFormat(year: Int, month: Int, day: Int): String {
        return "$year-${month + 1}-$day"
    }
}