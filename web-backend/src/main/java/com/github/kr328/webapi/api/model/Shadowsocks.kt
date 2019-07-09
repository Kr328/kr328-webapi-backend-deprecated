package com.github.kr328.webapi.api.model

data class Shadowsocks(val remarks: String, val host: String, val port: Int,
                       val password: String, val method: String,
                       val plugin: ShadowsocksPlugin? = null,
                       val extras: Map<String, String> = mutableMapOf()) : Proxy {
    companion object {
        val EMPTY: Shadowsocks = Shadowsocks("Localhost", "localhost", 1080, "password", "aes-128-cfb")
    }
}

