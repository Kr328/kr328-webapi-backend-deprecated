package com.github.kr328.webapi.api.subscriptions.model

data class CommonProxy(val remark: String, val proxyType: String) : Proxy {
    override fun getType(): String = proxyType
}