package com.magtonic.magtoniccargoinout.ui.data

class Constants {

    class WebServiceIpAddress {
        companion object {
            const val BASE_IP : String = "http://192.1.1.50/asmx/webservice.asmx/"
            const val IEP_IP : String = "http://192.1.1.121/webs.asmx/"
        }
    }

    class FtpInfo {
        companion object {
            const val IP_ADDRESS : String = "192.1.1.121"
            const val PORT : Int = 21
            //const val OUTSOURCED_USER : String = "iepftp"
            //const val OUTSOURCED_PASSWORD : String = "T69924056Ftp"
            //const val RETURN_OF_GOODS_USER : String = "rvupftp"
            //const val RETURN_OF_GOODS_PASSWORD : String = "T69924056Ftp"
            const val SHIPMENT_USER : String = "ogapftp"
            const val SHIPMENT_PASSWORD : String = "T69924056Ftp"
        }
    }

    class ACTION {
        companion object {
            
            const val ACTION_HIDE_KEYBOARD : String = "com.magtonic.MagtonicCargoInOut.HideKeyboardAction"
            const val ACTION_CONNECTION_TIMEOUT : String = "com.magtonic.MagtonicCargoInOut.ConnectionTimeOut"
            const val ACTION_CONNECTION_NO_ROUTE_TO_HOST : String = "com.magtonic.MagtonicCargoInOut.ConnectionNoRouteToHost"
            const val ACTION_SERVER_ERROR : String = "com.magtonic.MagtonicCargoInOut.ServerError"
            
            const val ACTION_NETWORK_FAILED : String = "com.magtonic.MagtonicCargoInOut.ActionNetworkFailed"
            const val ACTION_WIFI_STATE_CHANGED : String = "com.magtonic.MagtonicCargoInOut.ActionWifiStateChanged"
            const val ACTION_USER_INPUT_SEARCH : String = "com.magtonic.MagtonicCargoInOut.UserInputSearch"
            const val ACTION_BARCODE_NULL : String = "com.magtonic.MagtonicCargoInOut.BarcodeNull"

            const val ACTION_HOME_GO_GUEST_ACTION : String = "com.magtonic.MagtonicCargoInOut.HomeGoGuestAction"
            const val ACTION_HOME_GO_SHIPMENT_CHECK_ACTION : String = "com.magtonic.MagtonicCargoInOut.HomeGoShipmentCheckAction"
            //receipt
            const val ACTION_RECEIPT_FRAGMENT_REFRESH : String ="com.magtonic.MagtonicCargoInOut.ReceiptFragmentRefresh"

            const val ACTION_RECEIPT_NO_NOT_EXIST : String = "com.magtonic.MagtonicCargoInOut.ReceiptNoNotExist"
            const val ACTION_RECEIPT_SCAN_BARCODE : String = "com.magtonic.MagtonicCargoInOut.ReceiptScanBarcode"
            const val ACTION_RECEIPT_UNKNOWN_BARCODE_LENGTH: String = "com.magtonic.MagtonicCargoInOut.ReceiptUnknownBarcodeLength"

            const val ACTION_LICENSE_PLATE_HIDE_KEYBOARD : String = "com.magtonic.MagtonicCargoInOut.LicensePlateHideKeyboardAction"
            //guest
            const val ACTION_GUEST_SCAN_BARCODE : String = "com.magtonic.MagtonicCargoInOut.GuestScanBarcode"
            const val ACTION_GUEST_GET_CURRENT_PLANT_GUEST_LIST : String = "com.magtonic.MagtonicCargoInOut.GuestGetCurrentPlantGuestList"
            const val ACTION_GUEST_GET_CURRENT_PLANT_GUEST_SUCCESS : String = "com.magtonic.MagtonicCargoInOut.GuestGetCurrentPlantGuestSuccess"
            const val ACTION_GUEST_GET_CURRENT_PLANT_GUEST_FAILED : String = "com.magtonic.MagtonicCargoInOut.GuestGetCurrentPlantGuestFailed"
            const val ACTION_GUEST_LIST_CLEAR : String ="com.magtonic.MagtonicCargoInOut.GuestListClear"
            const val ACTION_GUEST_FRAGMENT_REFRESH : String ="com.magtonic.MagtonicCargoInOut.GuestFragmentRefresh"
            const val ACTION_GUEST_IN_OR_LEAVE_ACTION : String ="com.magtonic.MagtonicCargoInOut.GuestInOrLeaveAction"
            const val ACTION_GUEST_IN_OR_LEAVE_FAILED : String ="com.magtonic.MagtonicCargoInOut.GuestInOrLeaveFailed"
            const val ACTION_GUEST_IN_OR_LEAVE_SUCCESS : String ="com.magtonic.MagtonicCargoInOut.GuestInOrLeaveSuccess"
            const val ACTION_GUEST_SHOW_LEAVE_ACTION : String ="com.magtonic.MagtonicCargoInOut.GuestShowLeaveAction"
            const val ACTION_GUEST_SEARCH_GUEST_LIST_ACTION : String = "com.magtonic.MagtonicCargoInOut.GuestSearchGuestList"
            const val ACTION_GUEST_SEARCH_GUEST_NEXT_ACTION : String ="com.magtonic.MagtonicCargoInOut.GuestSearchGuestNext"
            const val ACTION_GUEST_SEARCH_GUEST_COMPLETE : String ="com.magtonic.MagtonicCargoInOut.GuestSearchGuestComplete"
            const val ACTION_GUEST_START_TIMER : String ="com.magtonic.MagtonicCargoInOut.GuestStartTimer"
            const val ACTION_GUEST_STOP_TIMER : String ="com.magtonic.MagtonicCargoInOut.GuestStopTimer"

            //shipment
            const val ACTION_SHIPMENT_SCAN_BARCODE : String = "com.magtonic.MagtonicCargoInOut.ShipmentScanBarcode"
            const val ACTION_SHIPMENT_LIST_CLEAR : String = "com.magtonic.MagtonicCargoInOut.ShipmentListClear"
            const val ACTION_SHIPMENT_FRAGMENT_REFRESH : String ="com.magtonic.MagtonicCargoInOut.ShipmentFragmentRefresh"
            const val ACTION_SHIPMENT_CHECK_ACTION : String ="com.magtonic.MagtonicCargoInOut.ShipmentCheckAction"
            const val ACTION_SHIPMENT_CHECK_FAILED : String ="com.magtonic.MagtonicCargoInOut.ShipmentCheckFailed"
            const val ACTION_SHIPMENT_CHECK_SUCCESS : String ="com.magtonic.MagtonicCargoInOut.ShipmentCheckSuccess"
            const val ACTION_SHIPMENT_CHECK_RETURN_EMPTY : String ="com.magtonic.MagtonicCargoInOut.ShipmentCheckReturnEmpty"
            const val ACTION_SHIPMENT_SEARCH_HISTORY_BY_DATE_ACTION : String ="com.magtonic.MagtonicCargoInOut.ShipmentSearchHistoryByDateAction"
            const val ACTION_SHIPMENT_SEARCH_HISTORY_BY_DATE_FINISH : String ="com.magtonic.MagtonicCargoInOut.ShipmentSearchHistoryByDateFinish"
            const val ACTION_SHIPMENT_FRAGMENT_NOT_COMPLETE_BIKE : String ="com.magtonic.MagtonicCargoInOut.ShipmentFragmentNotCompleteBike"

            //signature
            const val ACTION_SHIPMENT_SIGNATURE_SCAN_BARCODE : String = "com.magtonic.MagtonicCargoInOut.ShipmentSignatureScanBarcode"
            const val ACTION_SHIPMENT_SIGNATURE_LIST_CLEAR : String = "com.magtonic.MagtonicCargoInOut.ShipmentSignatureListClear"
            const val ACTION_SHIPMENT_SIGNATURE_SEARCH_NO_ACTION : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureSearchNoAction"
            const val ACTION_SHIPMENT_SIGNATURE_SEARCH_NO_FAILED : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureSearchNoFailed"
            const val ACTION_SHIPMENT_SIGNATURE_SEARCH_NO_EMPTY : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureSearchNoEmpty"
            const val ACTION_SHIPMENT_SIGNATURE_FRAGMENT_REFRESH : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureFragmentRefresh"
            const val ACTION_SHIPMENT_SIGNATURE_RETURN_EMPTY : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureReturnEmpty"

            //signature detail
            const val ACTION_SHIPMENT_SIGNATURE_SEARCH_DETAIL_ACTION : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureSearchDetailAction"
            const val ACTION_SHIPMENT_SIGNATURE_SEARCH_DETAIL_FAILED : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureSearchDetailFailed"
            const val ACTION_SHIPMENT_SIGNATURE_DETAIL_FRAGMENT_REFRESH : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureDetailFragmentRefresh"
            const val ACTION_SHIPMENT_SIGNATURE_DETAIL_RETURN_EMPTY : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureDetailReturnEmpty"
            const val ACTION_SHIPMENT_SIGNATURE_BACK_TO_SHIPMENT_NO_LIST : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureBackToShipmentNoList"
            const val ACTION_SHIPMENT_SIGNATURE_SHOW_SIGN_DIALOG_ACTION : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureShowSignDialogAction"
            const val ACTION_SHIPMENT_SIGNATURE_HIDE_FAB_BACK : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureHideFabBack"


            const val ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_CONNECT_TIMEOUT : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureDriverFtpConnectTimeout"
            const val ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_CONNECT_UNKNOWN_HOST : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureDriverFTPConnectUnknownHost"
            const val ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_CONNECT_FAILED : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureDriverFtpConnectFailed"
            const val ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_CONNECT_SUCCESS : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureDriverFTPConnectSuccess"
            const val ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_UPLOAD_FAILED : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureDriverFtpUploadFailed"
            const val ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_UPLOAD_SUCCESS : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureDriverFtpUploadSuccess"
            const val ACTION_SHIPMENT_SIGNATURE_DRIVER_FTP_UPLOAD_COMPLETE : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureDriverFTPUploadComplete"

            const val ACTION_SHIPMENT_SIGNATURE_DRIVER_SIGN_CONFIRM_ACTION : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureDriverSignConfirmAction"
            const val ACTION_SHIPMENT_SIGNATURE_DRIVER_SIGN_CONFIRM_FAILED : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureDriverSignConfirmFailed"
            const val ACTION_SHIPMENT_SIGNATURE_DRIVER_SIGN_CONFIRM_SUCCESS : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureDriverSignConfirmSuccess"

            const val ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_CONNECT_TIMEOUT : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureGuardFtpConnectTimeout"
            const val ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_CONNECT_UNKNOWN_HOST : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureGuardFTPConnectUnknownHost"
            const val ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_CONNECT_FAILED : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureGuardFtpConnectFailed"
            const val ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_CONNECT_SUCCESS : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureGuardFTPConnectSuccess"
            const val ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_UPLOAD_FAILED : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureGuardFtpUploadFailed"
            const val ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_UPLOAD_SUCCESS : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureGuardFtpUploadSuccess"
            const val ACTION_SHIPMENT_SIGNATURE_GUARD_FTP_UPLOAD_COMPLETE : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureGuardFTPUploadComplete"

            const val ACTION_SHIPMENT_SIGNATURE_GUARD_SIGN_CONFIRM_ACTION : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureGuardSignConfirmAction"
            const val ACTION_SHIPMENT_SIGNATURE_GUARD_SIGN_CONFIRM_FAILED : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureGuardSignConfirmFailed"
            const val ACTION_SHIPMENT_SIGNATURE_GUARD_SIGN_CONFIRM_SUCCESS : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureGuardSignConfirmSuccess"

            const val ACTION_SHIPMENT_SIGNATURE_ADD_SHIPMENT_NO_TO_MULTI_ACTION : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureAddShipmentNoToMultiAction"
            const val ACTION_SHIPMENT_SIGNATURE_ADD_SHIPMENT_NO_TO_MULTI_SUCCESS : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureAddShipmentNoToMultiSuccess"
            const val ACTION_SHIPMENT_SIGNATURE_ADD_SHIPMENT_NO_TO_MULTI_EXIST : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureAddShipmentNoToMultiExist"
            //multi sign
            const val ACTION_SHIPMENT_SIGNATURE_SHOW_SIGN_MULTI_LIST : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureShowSignMultiList"

            const val ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_UPLOAD_START : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiDriverFtpUploadFailed"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_CONNECT_TIMEOUT : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiDriverFtpConnectTimeout"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_CONNECT_UNKNOWN_HOST : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiDriverFTPConnectUnknownHost"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_CONNECT_FAILED : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiDriverFtpConnectFailed"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_CONNECT_SUCCESS : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiDriverFTPConnectSuccess"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_UPLOAD_FAILED : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiDriverFtpUploadFailed"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_UPLOAD_SUCCESS : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiDriverFtpUploadSuccess"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_UPLOAD_COMPLETE : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiDriverFTPUploadComplete"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_FTP_UPLOAD_ALL_COMPLETE : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiDriverFTPUploadAllComplete"

            const val ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_SIGN_CONFIRM_START : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiDriverSignConfirmStart"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_SIGN_CONFIRM_ACTION : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiDriverSignConfirmAction"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_SIGN_CONFIRM_FAILED : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiDriverSignConfirmFailed"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_DRIVER_SIGN_CONFIRM_SUCCESS : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiDriverSignConfirmSuccess"


            const val ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_UPLOAD_START : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiGuardFtpUploadFailed"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_CONNECT_TIMEOUT : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiGuardFtpConnectTimeout"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_CONNECT_UNKNOWN_HOST : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiGuardFTPConnectUnknownHost"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_CONNECT_FAILED : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiGuardFtpConnectFailed"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_CONNECT_SUCCESS : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiGuardFTPConnectSuccess"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_UPLOAD_FAILED : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiGuardFtpUploadFailed"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_UPLOAD_SUCCESS : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiGuardFtpUploadSuccess"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_UPLOAD_COMPLETE : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiGuardFTPUploadComplete"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_FTP_UPLOAD_ALL_COMPLETE : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiGuardFTPUploadAllComplete"

            const val ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_START : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiGuardSignConfirmStart"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_ACTION : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiGuardSignConfirmAction"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_FAILED : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiGuardSignConfirmFailed"
            const val ACTION_SHIPMENT_SIGNATURE_MULTI_GUARD_SIGN_CONFIRM_SUCCESS : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiGuardSignConfirmSuccess"

            const val ACTION_SHIPMENT_SIGNATURE_MULTI_SIGN_CONFIRM_COMPLETE : String ="com.magtonic.MagtonicCargoInOut.ShipmentSignatureMultiSignConfirmComplete"

            //ip setting
            const val ACTION_WEBSERVICE_FTP_IP_ADDRESS_UPDATE_ACTION : String ="com.magtonic.MagtonicCargoInOut.WebserviceFtpIpAddressUpdateAction"
            const val ACTION_WEBSERVICE_FTP_IP_ADDRESS_SHOW_PASSWORD_DIALOG : String ="com.magtonic.MagtonicCargoInOut.WebserviceFtpIpAddressShowPasswordDialog"
        }

    }
}