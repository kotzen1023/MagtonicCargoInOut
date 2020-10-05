package com.magtonic.magtoniccargoinout.model.item

import com.google.gson.Gson
import com.magtonic.magtoniccargoinout.model.receive.RJGuest
import com.magtonic.magtoniccargoinout.model.receive.RJShipment

class ItemShipment {
    var rjShipment: RJShipment? = RJShipment()

    companion object {
        const val RESULT_CORRECT = "0"

        fun transRJShipmentStrToItemShipment(RJShipmentStr: String): ItemShipment? {
            val gson = Gson()
            val itemShipment = ItemShipment()
            val rjShipment: RJShipment // = new RJReceipt();
            try {
                rjShipment = gson.fromJson<Any>(RJShipmentStr, RJShipment::class.java) as RJShipment
                itemShipment.rjShipment = rjShipment
            } catch (ex: Exception) {
                return null
            }

            return itemShipment
        }//trans_RJReceiptStr_To_ItemReceipt

    }
}