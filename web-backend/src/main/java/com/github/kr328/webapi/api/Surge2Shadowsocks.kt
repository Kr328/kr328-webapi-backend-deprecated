package com.github.kr328.webapi.api

import com.github.kr328.webapi.api.dumper.dumpShadowsocksD
import com.github.kr328.webapi.api.model.Shadowsocks
import com.github.kr328.webapi.api.model.ShadowsocksD
import com.github.kr328.webapi.api.parser.parseSurge

const val EXTRA_SHADOWSOCKS_D_PROVIDER_NAME: String = "extra_shadowsocks_d_provider_name"
const val EXTRA_SHADOWSOCKS_D_TRAFFIC_USED: String = "extra_shadowsocks_d_traffic_used"
const val EXTRA_SHADOWSOCKS_D_TRAFFIC_TOTAL: String = "extra_shadowsocks_d_traffic_total"

fun surge2Shadowsocks(data: String, extras: Map<String, String>): String {
    val proxy = parseSurge(data).proxy.filterIsInstance<Shadowsocks>()
    val shadowsocksD = ShadowsocksD(provider = extras[EXTRA_SHADOWSOCKS_D_PROVIDER_NAME] ?: "Unlabeled",
            defaultShadowsocks = proxy.getOrNull(0) ?: Shadowsocks.EMPTY,
            servers = proxy,
            trafficUsed = extras[EXTRA_SHADOWSOCKS_D_TRAFFIC_USED]?.toLong() ?: 0,
            trafficTotal = extras[EXTRA_SHADOWSOCKS_D_TRAFFIC_TOTAL]?.toLong() ?: 0)

    return dumpShadowsocksD(shadowsocksD)
}
