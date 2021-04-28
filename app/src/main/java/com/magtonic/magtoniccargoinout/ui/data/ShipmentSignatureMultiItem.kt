package com.magtonic.magtoniccargoinout.ui.data

class ShipmentSignatureMultiItem(shipmentNo: String) {
    private var shipmentNo: String? = shipmentNo
    fun getShipmentNo(): String? {
        return shipmentNo
    }

    fun setShipmentNo(shipmentNo: String) {
        this.shipmentNo = shipmentNo
    }
}