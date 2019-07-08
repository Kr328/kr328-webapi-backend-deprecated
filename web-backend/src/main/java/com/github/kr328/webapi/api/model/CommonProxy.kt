package com.github.kr328.webapi.api.model

import kotlinx.serialization.Serializable

@Serializable
data class CommonProxy(val remark: String, val proxyType: String) : Proxy {
    override fun getType(): String = proxyType
}
