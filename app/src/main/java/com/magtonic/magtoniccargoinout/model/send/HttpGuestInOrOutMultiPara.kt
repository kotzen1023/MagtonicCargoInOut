package com.magtonic.magtoniccargoinout.model.send

class HttpGuestInOrOutMultiPara {
    var cmd = "0"
    var data1 = "" //state 狀態(0.刷進、2.刷出)
    var data2 = "" //plant 廠區代號
    var data3 = "" //pmm09 供應商編號
    var data4 = "" //barcode 採購單編號
    var data5 = "" //  採購單項次
    var data6 = "" //刷進留空，刷退帶刷進日期
}