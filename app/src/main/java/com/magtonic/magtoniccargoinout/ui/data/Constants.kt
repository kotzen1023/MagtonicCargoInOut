package com.magtonic.magtoniccargoinout.ui.data

class Constants {
    class ACTION {
        companion object {
            
            const val ACTION_HIDE_KEYBOARD : String = "com.magtonic.MagtonicCargoInOut.HideKeyboardAction"
            const val ACTION_CONNECTION_TIMEOUT : String = "com.magtonic.MagtonicCargoInOut.ConnectionTimeOut"
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
        }

    }
}