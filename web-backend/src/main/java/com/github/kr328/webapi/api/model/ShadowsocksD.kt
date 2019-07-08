package com.github.kr328.webapi.api.model

import kotlinx.serialization.Serializable
import java.net.URL
import java.util.*

@Serializable
data class ShadowsocksD(val provider: String, val defaultShadowsocks: Shadowsocks,
                        val servers: List<Shadowsocks>,
                        val trafficUsed: Long? = null, val trafficTotal: Long? = null,
                        val expires: String? = null, val url: String? = null) : Config {
    companion object {
        const val TYPE: String = "SHADOWSOCKSD"

        const val EXTRA_PROXY_ID: String = "EXTRA_PROXY_ID"
        const val EXTRA_PROXY_RATIO: String = "EXTRA_PROXY_RATIO"
    }

    override fun getType(): String = TYPE
}