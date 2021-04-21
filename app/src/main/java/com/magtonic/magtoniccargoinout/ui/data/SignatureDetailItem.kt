package com.magtonic.magtoniccargoinout.ui.data

class SignatureDetailItem(header: String, content: String) {
    private var header: String? = header
    private var content: String? = content

    fun getHeader(): String? {
        return header
    }

    fun getContent(): String? {
        return content
    }
}