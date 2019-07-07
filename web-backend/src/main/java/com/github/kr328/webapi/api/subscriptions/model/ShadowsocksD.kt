package com.github.kr328.webapi.api.subscriptions.model

import java.net.URL
import java.util.*

data class ShadowsocksD(val provider: String, val defaultShadowsocks: Shadowsocks,
                        val servers: List<Shadowsocks>,
                        val trafficUsed: Long? = null, val trafficTotal: Long? = null,
                        val expires: Date? = null, val url: URL? = null) : Config {
    companion object {
        const val TYPE: String = "SHADOWSOCKSD"

        const val PROXY_EXTRA_ID: String = "PROXY_EXTRA_ID"
        const val PROXY_EXTRA_RATIO: String = "PROXY_EXTRA_RATIO"
    }

    override fun getType(): String = TYPE
}