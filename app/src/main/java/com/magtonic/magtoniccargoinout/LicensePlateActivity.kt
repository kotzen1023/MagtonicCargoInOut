package com.magtonic.magtoniccargoinout

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*

import android.graphics.drawable.BitmapDrawable

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore

import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
//import com.google.firebase.ml.vision.FirebaseVision
//import com.google.firebase.ml.vision.common.FirebaseVisionImage
//import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.magtonic.magtoniccargoinout.ui.data.Constants
import java.io.File

import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class LicensePlateActivity : AppCompatActivity() {
    private val mTAG = LicensePlateActivity::class.java.name

    private var toastHandle: Toast? = null
    private var ocrContext: Context? = null

    private var imm: InputMethodManager? = null

    private val imageCaptureCode = 1001
    private var imageUri: Uri? = null
    private lateinit var contentResolverLicense: ContentResolver

    private lateinit var imageViewLicensePlate: ImageView
    private lateinit var textViewLicensePlate: TextView
    private var editTextLicensePlateFront: EditText? = null
    private var editTextLicensePlateRear: EditText? = null

    private var mReceiver: BroadcastReceiver? = null
    private var isRegister = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lincense_plate)
        Log.d(mTAG, "onCreate")

        ocrContext = baseContext
        contentResolverLicense = ocrContext!!.contentResolver

        //get virtual keyboard
        imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

        //for action bar
        val actionBar: ActionBar? = supportActionBar

        actionBar?.setDisplayUseLogoEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.baseline_keyboard_arrow_left_white_48)


        imageViewLicensePlate = findViewById(R.id.imageViewLicensePlate)
        val btnSelectLicensePlate: Button = findViewById(R.id.btnSelectLicensePlate)
        val btnTakePhotoLicensePlate: Button = findViewById(R.id.btnTakePhotoLicensePlate)
        textViewLicensePlate = findViewById(R.id.textViewLicensePlate)
        editTextLicensePlateFront = findViewById(R.id.editTextLicensePlateFront)
        editTextLicensePlateRear = findViewById(R.id.editTextLicensePlateRear)
        val btnConfirmLicensePlate: Button = findViewById(R.id.btnConfirmLicensePlate)

        btnTakePhotoLicensePlate.setOnClickListener {
            openCamera()
        }

        btnSelectLicensePlate.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
        }

        editTextLicensePlateFront!!.setOnEditorActionListener { _, actionId, _ ->

            when(actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    Log.e(mTAG, "IME_ACTION_DONE")

                    val hideIntent = Intent()
                    hideIntent.action = Constants.ACTION.ACTION_LICENSE_PLATE_HIDE_KEYBOARD
                    ocrContext?.sendBroadcast(hideIntent)

                    true
                }

                EditorInfo.IME_ACTION_GO -> {
                    Log.e(mTAG, "IME_ACTION_GO")
                    true
                }

                EditorInfo.IME_ACTION_NEXT -> {
                    Log.e(mTAG, "IME_ACTION_NEXT")

                    val hideIntent = Intent()
                    hideIntent.action = Constants.ACTION.ACTION_LICENSE_PLATE_HIDE_KEYBOARD
                    ocrContext?.sendBroadcast(hideIntent)

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

        editTextLicensePlateRear!!.setOnEditorActionListener { _, actionId, _ ->

            when(actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    Log.e(mTAG, "IME_ACTION_DONE")

                    val hideIntent = Intent()
                    hideIntent.action = Constants.ACTION.ACTION_LICENSE_PLATE_HIDE_KEYBOARD
                    ocrContext?.sendBroadcast(hideIntent)

                    true
                }

                EditorInfo.IME_ACTION_GO -> {
                    Log.e(mTAG, "IME_ACTION_GO")
                    true
                }

                EditorInfo.IME_ACTION_NEXT -> {
                    Log.e(mTAG, "IME_ACTION_NEXT")

                    val hideIntent = Intent()
                    hideIntent.action = Constants.ACTION.ACTION_LICENSE_PLATE_HIDE_KEYBOARD
                    ocrContext?.sendBroadcast(hideIntent)

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

        btnConfirmLicensePlate.setOnClickListener {
            //val regex = "[a-zA-Z0-9]+"
            val ps: Pattern = Pattern.compile("^[a-zA-Z0-9]+$")
            val combine = editTextLicensePlateFront!!.text.toString()+editTextLicensePlateRear!!.text.toString()
            val ms: Matcher = ps.matcher(combine)
             val bs = ms.matches()

            if (editTextLicensePlateFront!!.text.isEmpty() || editTextLicensePlateRear!!.text.isEmpty()) {
                toast(getString(R.string.license_plate_empty))
            } else if (!bs)
                toast(getString(R.string.invalid_license_plate))
            else {
                toast(editTextLicensePlateFront!!.text.toString()+" - "+editTextLicensePlateRear!!.text.toString())

                val uri = imageUri
                Log.e(mTAG, "uri = $uri")

                if (uri != null) {
                    val file = File(uri.path as String)
                    Log.e(mTAG, "file = ${file.absolutePath}")
                }



            }
        }

        val filter: IntentFilter
        @SuppressLint("CommitPrefEdits")
        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != null) {
                    if (intent.action!!.equals(Constants.ACTION.ACTION_LICENSE_PLATE_HIDE_KEYBOARD, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_LICENSE_PLATE_HIDE_KEYBOARD")

                        editTextLicensePlateFront!!.setText(editTextLicensePlateFront!!.text.toString().toUpperCase(
                            Locale.getDefault()))

                        editTextLicensePlateRear!!.setText(editTextLicensePlateRear!!.text.toString().toUpperCase(
                            Locale.getDefault()))

                        imm?.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0)


                    }
                }


            }
        }


        if (!isRegister) {
            filter = IntentFilter()
            //keyboard
            filter.addAction(Constants.ACTION.ACTION_LICENSE_PLATE_HIDE_KEYBOARD)

            ocrContext!!.registerReceiver(mReceiver, filter)
            isRegister = true
            Log.d(mTAG, "registerReceiver mReceiver")
        }
    }

    override fun onDestroy() {
        Log.i(mTAG, "onDestroy")

        if (isRegister && mReceiver != null) {
            try {
                ocrContext!!.unregisterReceiver(mReceiver)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_license_plate, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.license_plate_hide_or_show_keyboard -> {
                imm?.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0)
            }
            android.R.id.home -> {
                finish()
            }
        }


        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            imageViewLicensePlate.setImageURI(data!!.data)

            textViewLicensePlate.text = ""

            /*val bitmap = (imageViewLicensePlate.drawable as BitmapDrawable).bitmap
            val image = FirebaseVisionImage.fromBitmap(bitmap)
            val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

            detector.processImage(image)
                .addOnSuccessListener { firebaseVisionText ->

                    processResultText(firebaseVisionText)
                }
                .addOnFailureListener {

                    textViewLicensePlate.text = getString(R.string.recognize_failed)
                }
            */

        } else if (requestCode == imageCaptureCode && resultCode == Activity.RESULT_OK) {
            imageViewLicensePlate.setImageURI(imageUri)

            if (imageViewLicensePlate.drawable != null) {
                textViewLicensePlate.text = ""

                /*val bitmap = (imageViewLicensePlate.drawable as BitmapDrawable).bitmap
                val image = FirebaseVisionImage.fromBitmap(bitmap)
                val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

                detector.processImage(image)
                    .addOnSuccessListener { firebaseVisionText ->

                        processResultText(firebaseVisionText)
                    }
                    .addOnFailureListener {

                        textViewLicensePlate.text = getString(R.string.recognize_failed)
                    }
                */
            } else {
                Log.e(mTAG,"Select an Image First")
            }
        }
    }

    /*private fun processResultText(resultText: FirebaseVisionText) {
        if (resultText.textBlocks.size == 0) {
            textViewLicensePlate.text = getString(R.string.detect_no_text_found)
            return
        }
        for (block in resultText.textBlocks) {
            val blockText = block.text
            textViewLicensePlate.append(blockText + "\n")
        }

        if (textViewLicensePlate.text.length > 9) {
            toast(getString(R.string.invalid_license_plate))
        } else {
            Log.e(mTAG, "textViewLicensePlate.text = "+textViewLicensePlate.text)


            if (textViewLicensePlate.text.contains("-")) {
                val splitArray = textViewLicensePlate.text.split("-")
                editTextLicensePlateFront!!.setText(splitArray[0])
                editTextLicensePlateRear!!.setText(splitArray[1])
            } else if (textViewLicensePlate.text.contains(" ")) {
                val splitArray = textViewLicensePlate.text.split(" ")
                editTextLicensePlateFront!!.setText(splitArray[0])
                editTextLicensePlateRear!!.setText(splitArray[1])
            } else {
                editTextLicensePlateFront!!.setText(textViewLicensePlate.text.subSequence(0, 4))
                editTextLicensePlateRear!!.setText(textViewLicensePlate.text.subSequence(4, textViewLicensePlate.text.length))
            }



        }
    }*/



    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        imageUri = contentResolverLicense.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, imageCaptureCode)
    }

    private fun toast(message: String) {

        if (toastHandle != null)
            toastHandle!!.cancel()

        toastHandle = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            val toast = Toast.makeText(this, HtmlCompat.fromHtml("<h1>$message</h1>", HtmlCompat.FROM_HTML_MODE_COMPACT), Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, 0)
            toast.show()

            toast
        } else { //Android 11
            val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
            toast.show()

            toast
        }
    }


}