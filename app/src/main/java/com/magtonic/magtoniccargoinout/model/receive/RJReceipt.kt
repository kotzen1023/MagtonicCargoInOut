package com.magtonic.magtoniccargoinout.model.receive

class RJReceipt : RJBase() {
    var result2 = ""// != "" when result = 0// it return fail reason
    //說明
    //0.不可收貨:傳回不可收貨的原因說明
    //1.可收貨:傳回空值

    var pmm09 = "供應商編號"//供應商編號 //list_item_receipt_Provider_num_label
    var pmc03 = "None"//供應商名稱 //list_item_receipt_Provider_name_label //--- header field
    var pmn04 = "料件編號" //料件編號 //list_item_receipt_object_code_num_label
    var pmn041 = "品名"//品名 //list_item_receipt_object_name_label
    var ima021 = "規格"//規格 //list_item_receipt_object_spec_label
    var pmn07 = "單位"//單位 //list_item_receipt_object_unit_label
    var ima35 = "倉庫"//倉庫// list_item_receipt_warehouse_label // can modify
    var ima36 = "儲位"//儲位// list_item_receipt_location_label // can modify
    var pmn20 = "未交數量"//未交數量 //list_item_receipt_object_not_get_num_label // can modify
    var pmn02 = "採購單項次"//採購單項次 //list_item_receipt_PO_Line_label
    var pmm02 = "None"//採購單性質 //list_item_receipt_PO_Property_label //--- header field
    var pmnud02 = "None" //檢驗否
    var rvb38 = "批號"

    var rva06 = "收貨日期"//收貨日期
    var rvb01 = "收貨單單號或失敗原因說明"//收貨單單號或失敗原因說明
    var rvb02 = "收貨單項次"//收貨單項次
}