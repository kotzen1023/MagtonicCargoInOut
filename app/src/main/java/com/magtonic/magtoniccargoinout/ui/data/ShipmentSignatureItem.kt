package com.magtonic.magtoniccargoinout.ui.data

class ShipmentSignatureItem (shipmentNo: String, customerNo: String, orderNo: String) {
    private var shipmentNo: String? = shipmentNo
    private var customerNo: String? = customerNo
    private var orderNo: String? = orderNo

    fun getShipmentNo(): String? {
        return shipmentNo
    }

    fun setShipmentNo(shipmentNo: String) {
        this.shipmentNo = shipmentNo
    }

    fun getCustomerNo(): String? {
        return customerNo
    }

    fun setCustomerNo(customerNo: String) {
        this.customerNo = customerNo
    }

    fun getOrderNo(): String? {
        return orderNo
    }

    fun setOrderNo(orderNo: String) {
        this.orderNo = orderNo
    }
}