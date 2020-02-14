package com.magtonic.magtoniccargoinout.ui.data

import android.widget.Button

class GuestDetailItem(data1: String, data2: String, data3: String, data4: String, data5: String) {
    private var data1: String? = data1
    private var data2: String? = data2
    private var data3: String? = data3
    private var data4: String? = data4
    private var data5: String? = data5
    private var btnOk: Button? = null

    fun getData1(): String? {
        return data1
    }

    fun setData1(data1: String) {
        this.data1 = data1
    }

    fun getData2(): String? {
        return data2
    }

    fun setData2(data2: String) {
        this.data2 = data2
    }

    fun getData3(): String? {
        return data3
    }

    fun setData3(data3: String) {
        this.data3 = data3
    }

    fun getData4(): String? {
        return data4
    }

    fun setData4(data4: String) {
        this.data4 = data4
    }

    fun getData5(): String? {
        return data5
    }

    fun setData5(data5: String) {
        this.data5 = data5
    }

    fun getBtnOk(): Button? {
        return btnOk
    }

    fun setBtnOk(btnOk: Button) {
        this.btnOk = btnOk
    }
}