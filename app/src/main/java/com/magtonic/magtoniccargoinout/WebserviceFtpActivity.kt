package com.magtonic.magtoniccargoinout

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.magtonic.magtoniccargoinout.MainActivity.Companion.base_ip_address
import com.magtonic.magtoniccargoinout.MainActivity.Companion.ftp_ip_address
import com.magtonic.magtoniccargoinout.MainActivity.Companion.iep_ip_address
import com.magtonic.magtoniccargoinout.ui.data.Constants

class WebserviceFtpActivity : AppCompatActivity() {
    private val mTAG = WebserviceFtpActivity::class.java.name

    var pref: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    private val fileName = "Preference"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webservice_ftp)

        //for action bar
        val actionBar: androidx.appcompat.app.ActionBar? = supportActionBar

        if (actionBar != null) {

            actionBar.setDisplayUseLogoEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
            //actionBar.title = getString(R.string.nav_outsourced)
            actionBar.title = title
        }



        val baseIpArray  = base_ip_address.split('.')

        val baseIpAddress1 = findViewById<EditText>(R.id.base_ip_addr_1)
        baseIpAddress1.setText(baseIpArray[0])

        val baseIpAddress2 = findViewById<EditText>(R.id.base_ip_addr_2)
        baseIpAddress2.setText(baseIpArray[1])

        val baseIpAddress3 = findViewById<EditText>(R.id.base_ip_addr_3)
        baseIpAddress3.setText(baseIpArray[2])

        val baseIpAddress4 = findViewById<EditText>(R.id.base_ip_addr_4)
        baseIpAddress4.setText(baseIpArray[3])

        val iepIpArray  = iep_ip_address.split('.')

        val iepIpAddress1 = findViewById<EditText>(R.id.iep_ip_addr_1)
        iepIpAddress1.setText(iepIpArray[0])

        val iepIpAddress2 = findViewById<EditText>(R.id.iep_ip_addr_2)
        iepIpAddress2.setText(iepIpArray[1])

        val iepIpAddress3 = findViewById<EditText>(R.id.iep_ip_addr_3)
        iepIpAddress3.setText(iepIpArray[2])

        val iepIpAddress4 = findViewById<EditText>(R.id.iep_ip_addr_4)
        iepIpAddress4.setText(iepIpArray[3])

        val ftpIpArray  = ftp_ip_address.split('.')

        val ftpIpAddress1 = findViewById<EditText>(R.id.ftp_ip_addr_1)
        ftpIpAddress1.setText(ftpIpArray[0])

        val ftpIpAddress2 = findViewById<EditText>(R.id.ftp_ip_addr_2)
        ftpIpAddress2.setText(ftpIpArray[1])

        val ftpIpAddress3 = findViewById<EditText>(R.id.ftp_ip_addr_3)
        ftpIpAddress3.setText(ftpIpArray[2])

        val ftpIpAddress4 = findViewById<EditText>(R.id.ftp_ip_addr_4)
        ftpIpAddress4.setText(ftpIpArray[3])

        val btnConfirmWebserviceFtp = findViewById<Button>(R.id.btnConfirmWebserviceFtp)
        btnConfirmWebserviceFtp.setOnClickListener {
            base_ip_address = "${baseIpAddress1.text}.${baseIpAddress2.text}.${baseIpAddress3.text}.${baseIpAddress4.text}"
            iep_ip_address = "${iepIpAddress1.text}.${iepIpAddress2.text}.${iepIpAddress3.text}.${iepIpAddress4.text}"
            ftp_ip_address = "${ftpIpAddress1.text}.${ftpIpAddress2.text}.${ftpIpAddress3.text}.${ftpIpAddress4.text}"

            val saveIntent = Intent()
            saveIntent.action = Constants.ACTION.ACTION_WEBSERVICE_FTP_IP_ADDRESS_UPDATE_ACTION
            sendBroadcast(saveIntent)

            finish()
            /*editor = pref!!.edit()
            editor!!.putString("BASE_IP_ADDRESS", base_ip_address)
            editor!!.putString("REAL_IP_ADDRESS", real_ip_address)
            editor!!.putString("IEP_IP_ADDRESS", iep_ip_address)
            editor!!.putString("FTP_IP_ADDRESS", ftp_ip_address)
            editor!!.apply()*/
        }

    }

    override fun onDestroy() {
        Log.i(mTAG, "onDestroy")



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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.

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
}