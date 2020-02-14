package com.magtonic.magtoniccargoinout.model.sys

import android.util.Log

class ScanBarcode {
    var poBarcodeByScan: String =""
    var poBarcode: String = ""// poBarcodeByScan from index 0 to length - 4// pmn01
    var poLine: String = ""// poBarcodeByScan final three byte// pmn02
    var poLineInt: Int = 0

    companion object {
        private val mTAG = ScanBarcode::class.java.name

        fun setPoBarcodeByScanTransform(poCompleteStr: String): ScanBarcode? {

            val scanBarcode = ScanBarcode()
            try {
                scanBarcode.poBarcode = poCompleteStr.substring(0, poCompleteStr.length - 3)
                scanBarcode.poLine = poCompleteStr.substring(poCompleteStr.length - 3)
                scanBarcode.poLineInt = removeLeadingZeroes(scanBarcode.poLine)
                scanBarcode.poBarcodeByScan = poCompleteStr

            } catch (ex: Exception) {
                return null
            }

            return scanBarcode
        }

        fun removeLeadingZeroes(value: String): Int {
            var temp = 0

            Log.e(mTAG, "value = $value")

            if (value.isNotEmpty()) {

                try {
                    temp = Integer.valueOf(value)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

            return temp
        }
    }
}