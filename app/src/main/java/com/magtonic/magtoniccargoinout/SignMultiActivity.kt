package com.magtonic.magtoniccargoinout

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

import android.os.Bundle
import android.util.Log
import android.view.Gravity

import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.magtonic.magtoniccargoinout.MainActivity.Companion.signatureMultiSignList
import com.magtonic.magtoniccargoinout.ui.data.Constants

import com.magtonic.magtoniccargoinout.ui.data.ShipmentSignatureMultiItemAdapter

class SignMultiActivity : AppCompatActivity() {
    private val mTAG = SignMultiActivity::class.java.name

    private var signMultiContext: Context? = null

    private var mReceiver: BroadcastReceiver? = null
    private var isRegister = false

    private var textViewMultiSign: TextView? = null
    private var listViewMultiSign: ListView? = null

    private var btnPrev: Button?= null
    private var btnClear: Button?= null
    private var btnSign: Button?=null

    private var shipmentSignatureMultiItemAdapter: ShipmentSignatureMultiItemAdapter? = null

    private var toastHandle: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_sign)



        signMultiContext = applicationContext

        textViewMultiSign = findViewById(R.id.textViewMultiSign)
        listViewMultiSign = findViewById(R.id.listViewMultiSign)

        textViewMultiSign!!.text = getString(R.string.shipment_signature_multi_total, signatureMultiSignList.size)

        if (signMultiContext != null) {
            shipmentSignatureMultiItemAdapter = ShipmentSignatureMultiItemAdapter(signMultiContext, R.layout.activity_multi_sign_listview_item, signatureMultiSignList)
            listViewMultiSign!!.adapter = shipmentSignatureMultiItemAdapter
        }

        btnPrev = findViewById(R.id.btnClearMultiPrev)
        btnClear = findViewById(R.id.btnClearMultiSign)
        btnSign = findViewById(R.id.btnSignMultiSign)

        btnPrev!!.setOnClickListener {
            finish()
        }

        btnClear!!.setOnClickListener {

            signatureMultiSignList.clear()

            textViewMultiSign!!.text = getString(R.string.shipment_signature_multi_total, signatureMultiSignList.size)

            if (shipmentSignatureMultiItemAdapter != null) {
                shipmentSignatureMultiItemAdapter!!.notifyDataSetChanged()
            }
        }

        btnSign!!.setOnClickListener {
            if (signatureMultiSignList.size > 0) {
                showSignDialog()
            } else {
                toastLong(getString(R.string.shipment_signature_multi_list_empty_prev))
            }
        }

        //for action bar
        val actionBar: androidx.appcompat.app.ActionBar? = supportActionBar

        if (actionBar != null) {
            actionBar.setDisplayUseLogoEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = getString(R.string.shipment_signature_multi_list)
        }

        val filter: IntentFilter

        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != null) {
                    when {
                        intent.action!!.equals(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_SIGN_CONFIRM_COMPLETE, ignoreCase = true) -> {
                            Log.d(mTAG, "ACTION_SHIPMENT_SIGNATURE_MULTI_SIGN_CONFIRM_COMPLETE")

                            finish()
                        }

                        //multi sign end
                    }

                }
            }
        }

        if (!isRegister) {
            filter = IntentFilter()
            filter.addAction(Constants.ACTION.ACTION_SHIPMENT_SIGNATURE_MULTI_SIGN_CONFIRM_COMPLETE)


            signMultiContext?.registerReceiver(mReceiver, filter)
            isRegister = true
            Log.d(mTAG, "registerReceiver mReceiver")
        }

    }

    override fun onDestroy() {
        Log.i(mTAG, "onDestroy")

        if (isRegister && mReceiver != null) {
            try {
                signMultiContext!!.unregisterReceiver(mReceiver)
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

        finish()
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

    private fun showSignDialog() {
        val promptView = View.inflate(this@SignMultiActivity, R.layout.shipment_signature_dialog, null)

        val alertDialogBuilder = AlertDialog.Builder(this@SignMultiActivity).create()
        alertDialogBuilder.setView(promptView)


        val textViewOutsourcedProcessDialogMsg = promptView.findViewById<TextView>(R.id.textViewOutsourcedProcessDialogMsg)

        val btnCancel = promptView.findViewById<Button>(R.id.btnSignatureDialogCancel)
        val btnConfirm = promptView.findViewById<Button>(R.id.btnSignatureDialogConfirm)

        textViewOutsourcedProcessDialogMsg.text = getString(R.string.shipment_signature_multi_sign_tile, signatureMultiSignList.size)

        //Log.e(mTAG, "Shipment No. = $textViewSignatureDialogShipmentNo")



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

            val intent = Intent(this@SignMultiActivity, SignActivity::class.java)
            intent.putExtra("IS_SIGN_MULTI",true)
            intent.putExtra("SEND_ORDER", "")
            intent.putExtra("TITLE", getString(R.string.nav_signature))
            intent.putExtra("SEND_FRAGMENT", "SHIPMENT_SIGNATURE_FRAGMENT")
            startActivity(intent)


            alertDialogBuilder.dismiss()
        }
        alertDialogBuilder.show()


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
}