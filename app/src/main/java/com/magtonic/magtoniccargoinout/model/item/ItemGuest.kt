package com.magtonic.magtoniccargoinout.model.item

import com.google.gson.Gson
import com.magtonic.magtoniccargoinout.model.receive.RJGuest

class ItemGuest {
    var rjGuest: RJGuest? = RJGuest()

    companion object {
        const val RESULT_CORRECT = "0"

        fun transRJGuestStrToItemGuest(RJGuestStr: String): ItemGuest? {
            val gson = Gson()
            val itemGuest = ItemGuest()
            val rjGuest: RJGuest // = new RJReceipt();
            try {
                rjGuest = gson.fromJson<Any>(RJGuestStr, RJGuest::class.java) as RJGuest
                itemGuest.rjGuest = rjGuest
            } catch (ex: Exception) {
                return null
            }

            return itemGuest
        }//trans_RJReceiptStr_To_ItemReceipt

    }
}