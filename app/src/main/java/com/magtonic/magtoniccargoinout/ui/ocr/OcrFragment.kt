package com.magtonic.magtoniccargoinout.ui.ocr

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent

import android.net.Uri
import android.os.Build

import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
//import com.google.firebase.ml.vision.FirebaseVision
//import com.google.firebase.ml.vision.common.FirebaseVisionImage
//import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.magtonic.magtoniccargoinout.R


class OcrFragment : Fragment() {
    private val mTAG = OcrFragment::class.java.name
    private var ocrContext: Context? = null

    private var toastHandle: Toast? = null

    lateinit var imageView: ImageView
    lateinit var editText: EditText
    lateinit var btnSelect: Button
    lateinit var btnRecognize: Button
    lateinit var btnTakePhoto: Button

    private val ImageCaptureCode = 1001
    var imageUri: Uri? = null
    lateinit var contentResolver: ContentResolver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ocrContext = context
        contentResolver = ocrContext!!.contentResolver

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d(mTAG, "onCreateView")

        val view = inflater.inflate(R.layout.fragment_ocr, container, false)

        imageView = view.findViewById(R.id.imageView)
        editText = view.findViewById(R.id.editText)
        btnSelect = view.findViewById(R.id.btnSelect)
        btnRecognize = view.findViewById(R.id.btnRecognize)
        btnTakePhoto = view.findViewById(R.id.btnTakePhoto)

        btnSelect.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
        }

        btnRecognize.setOnClickListener {
            if (imageView.drawable != null) {
                editText.setText("")
                btnRecognize.isEnabled = false
                /*val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                val image = FirebaseVisionImage.fromBitmap(bitmap)
                val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

                detector.processImage(image)
                    .addOnSuccessListener { firebaseVisionText ->
                        btnRecognize.isEnabled = true
                        processResultText(firebaseVisionText)
                    }
                    .addOnFailureListener {
                        btnRecognize.isEnabled = true
                        editText.setText(getString(R.string.recognize_failed))
                    }*/
            } else {
                toast("Select an Image First")
            }
        }

        btnTakePhoto.setOnClickListener {
            //if system os is Marshmallow or Above, we need to request runtime permission
            openCamera()
        }

        return view
    }

    override fun onDestroy() {
        Log.i(mTAG, "onDestroy")

        super.onDestroy()
    }

    override fun onDestroyView() {
        Log.i(mTAG, "onDestroyView")



        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.i(mTAG, "onActivityCreated")
        super.onActivityCreated(savedInstanceState)

    }

    private fun toast(message: String) {

        if (toastHandle != null)
            toastHandle!!.cancel()

        toastHandle = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            val toast = Toast.makeText(ocrContext, HtmlCompat.fromHtml("<h1>$message</h1>", HtmlCompat.FROM_HTML_MODE_COMPACT), Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, 0)
            toast.show()

            toast
        } else { //Android 11
            val toast = Toast.makeText(ocrContext, message, Toast.LENGTH_SHORT)
            toast.show()

            toast
        }
    }

    //fun selectImage(v: View) {
    //    val intent = Intent()
    //    intent.type = "image/*"
    //    intent.action = Intent.ACTION_GET_CONTENT
    //    startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
    //}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            imageView.setImageURI(data!!.data)

        } else if (requestCode == ImageCaptureCode && resultCode == Activity.RESULT_OK) {
            imageView.setImageURI(imageUri)
        }
    }

    /*fun startRecognizing(v: View) {
        if (imageView.drawable != null) {
            editText.setText("")
            v.isEnabled = false
            //val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            //val image = FirebaseVisionImage.fromBitmap(bitmap)
            //val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

            //detector.processImage(image)
            //    .addOnSuccessListener { firebaseVisionText ->
            //        v.isEnabled = true
            //        processResultText(firebaseVisionText)
            //    }
            //    .addOnFailureListener {
            //        v.isEnabled = true
            //        editText.setText("Failed")
            //    }
        } else {
            toast("Select an Image First")
        }

    }*/


    /*private fun processResultText(resultText: FirebaseVisionText) {
        if (resultText.textBlocks.size == 0) {
            editText.setText("No Text Found")
            return
        }
        for (block in resultText.textBlocks) {
            val blockText = block.text
            editText.append(blockText + "\n")
        }
    }*/

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, ImageCaptureCode)
    }
}