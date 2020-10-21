package com.magtonic.magtoniccargoinout.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = History.TABLE_NAME)
class History(barcode: String, workOrderState: String, workOrderDesc: String, shipmentState: String, shipmentDesc: String, datetime: String, date: String) {
    companion object {
        const val TABLE_NAME = "shipcheck"
    }

    @PrimaryKey(autoGenerate = true)
    private var id: Int = 0

    @ColumnInfo(name = "barcode")
    private var barcode: String? = ""

    @ColumnInfo(name = "workOrderState") // column name will be "list_title" instead of "title" in table
    private var workOrderState: String? = ""

    @ColumnInfo(name = "workOrderDesc")
    private var workOrderDesc: String? = ""

    @ColumnInfo(name = "shipmentState") // column name will be "list_title" instead of "title" in table
    private var shipmentState: String? = ""

    @ColumnInfo(name = "shipmentDesc")
    private var shipmentDesc: String? = ""

    @ColumnInfo(name = "datetime")
    private var datetime: String? = ""

    @ColumnInfo(name = "date")
    private var date: String? = ""

    init {
        this.barcode = barcode
        this.workOrderState = workOrderState
        this.workOrderDesc = workOrderDesc
        this.shipmentState = shipmentState
        this.shipmentDesc = shipmentDesc
        this.datetime = datetime
        this.date = date
    }

    fun getId(): Int {
        return id
    }

    fun setId(id : Int) {
        this.id = id
    }

    fun getBarcode(): String? {
        return barcode
    }

    fun getWorkOrderState(): String? {
        return workOrderState
    }

    fun setWorkOrderState(workOrderState: String) {
        this.workOrderState = workOrderState
    }

    fun getWorkOrderDesc(): String? {
        return workOrderDesc
    }

    fun setWorkOrderDesc(workOrderDesc: String) {
        this.workOrderDesc = workOrderDesc
    }

    fun getShipmentState(): String? {
        return shipmentState
    }

    fun setShipmentState(shipmentState: String) {
        this.shipmentState = shipmentState
    }

    fun getShipmentDesc(): String? {
        return shipmentDesc
    }

    fun setShipmentDesc(shipmentDesc: String) {
        this.shipmentDesc = shipmentDesc
    }

    fun getDatetime(): String? {
        return datetime
    }

    fun setDatetime(datetime: String) {
        this.datetime = datetime
    }

    fun getDate(): String? {
        return date
    }

    fun setDate(date: String) {
        this.date = date
    }
}