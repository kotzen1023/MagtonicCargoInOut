package com.magtonic.magtoniccargoinout.ui.data

class ShipmentCheckItem (status: String, reason: String) {
    private var status: String? = status
    private var reason: String? = reason

    fun getStatus(): String? {
        return status
    }

    //fun setStatus(status: String) {
    //    this.status = status
    //}

    fun getReason(): String? {
        return reason
    }

    //fun setReason(reason: String) {
    //    this.reason = reason
    //}
}