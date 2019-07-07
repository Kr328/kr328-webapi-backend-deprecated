package com.github.kr328.webapi.api.subscriptions.model

data class Shadowsocks(val remark: String, val host: String, val port: Int,
                       val password: String, val method: String,
                       val plugin: ShadowsocksPlugin? = null,
                       val extra: Map<String, Any> = mutableMapOf()) : Proxy {
    companion object {
        const val TYPE: String = "TYPE_SHADOWSOCKS"

        val EMPTY: Shadowsocks = Shadowsocks("Localhost", "localhost", 1080, "password", "aes-128-cfb")
    }

    override fun getType(): String = TYPE
}

