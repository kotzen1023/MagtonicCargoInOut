package com.magtonic.magtoniccargoinout.model.item

import com.google.gson.Gson
import com.magtonic.magtoniccargoinout.model.receive.RJReceipt
import com.magtonic.magtoniccargoinout.model.sys.ScanBarcode

class ItemReceipt {
    enum class ItemState {
        INITIAL,
        UPLOADED,
        UPLOAD_FAILED,
        CONFIRMED,
        CONFIRM_FAILED

    }

    //val RESULT_CORRECT = "1"

    var rjReceipt: RJReceipt? = RJReceipt()
    //var rjHistory: RJHistory? = RJHistory()


    //ui display Purchase number is poNumSplit + PONum_Line
    // PO
    // public int viewType = 0;// adapter use ,0 for header ,1 for content

    var poNumSplit = ""//from index 0 to poNumScanTotal.size() - 3 -1(from 0 start)
    //public  String PONum_line; //poNumScanTotal Final 3 byte :採購單項次 already in RJRecepit
    var poNumScanTotal = ""
    //採購單號 = poNumSplit + line
    var poLineInt: Int = 0

    var receiveNum = ""//// 收貨單單號 map to field rva01 in RJReceiptUpload  class
    var receiveLine = ""//

    var state = ItemState.INITIAL // need it to add print ,


    //static execute method
    companion object {
        const val RESULT_CORRECT = "1"

        fun transRJReceiptStrToItemReceipt(RJReceiptStr: String, poNumSplit: String): ItemReceipt? {
            val gson = Gson()
            val itemReceipt = ItemReceipt()
            val rjReceipt: RJReceipt // = new RJReceipt();
            try {
                rjReceipt = gson.fromJson<Any>(RJReceiptStr, RJReceipt::class.java) as RJReceipt
                itemReceipt.rjReceipt = rjReceipt
                itemReceipt.poNumSplit = poNumSplit
                itemReceipt.poNumScanTotal = poNumSplit + itemReceipt.rjReceipt!!.pmm02
                itemReceipt.poLineInt = ScanBarcode.removeLeadingZeroes(itemReceipt.rjReceipt!!.pmn02)



            } catch (ex: Exception) {
                return null
            }

            return itemReceipt
        }//trans_RJReceiptStr_To_ItemReceipt

        fun transRJReceiptStrToItemReceiptPoint(RJReceiptStr: String, poNumScanTotal: String): ItemReceipt? {
            val gson = Gson()
            val itemReceipt = ItemReceipt()
            val rjReceipt: RJReceipt // = new RJReceipt();
            try {
                rjReceipt = gson.fromJson<Any>(RJReceiptStr, RJReceipt::class.java) as RJReceipt
                itemReceipt.rjReceipt = rjReceipt
                itemReceipt.poNumSplit = poNumScanTotal
                itemReceipt.poNumScanTotal = poNumScanTotal
                itemReceipt.poLineInt = 0



            } catch (ex: Exception) {
                return null
            }

            return itemReceipt
        }

        /*fun trans_RJHistoryStr_To_ItemReceipt(RJReceiptStr: String, poNumSplit: String, poLineInt: String): ItemReceipt? {
            val gson = Gson()
            val itemReceipt = ItemReceipt()
            val rjHistory: RJHistory // = new RJReceipt();
            try {
                rjHistory = gson.fromJson<Any>(RJReceiptStr, RJHistory::class.java) as RJHistory
                itemReceipt.rjHistory = rjHistory
                itemReceipt.poNumSplit = poNumSplit
                itemReceipt.poNumScanTotal = poNumSplit + poLineInt
                itemReceipt.poLineInt = Integer.valueOf(poLineInt)
                itemReceipt.receiveLine = itemReceipt.rjHistory!!.rvb02
                itemReceipt.receiveNum = itemReceipt.rjHistory!!.rvb01
                itemReceipt.state = ItemState.HISTORY_TAG
                return itemReceipt

            } catch (ex: Exception) {
                return null
            }


        }//trans_RJReceiptStr_To_ItemReceipt


        fun trans_RJReceiptToTtemReceipt(receipt: RJReceipt, poNumSplit: String): ItemReceipt {

            val itemReceipt = ItemReceipt()
            itemReceipt.rjReceipt = receipt
            itemReceipt.poNumSplit = poNumSplit
            itemReceipt.poNumScanTotal = poNumSplit + itemReceipt.rjReceipt!!.pmm02


            return itemReceipt
        }*/
    }
}