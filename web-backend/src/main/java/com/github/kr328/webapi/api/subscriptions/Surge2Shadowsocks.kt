package com.github.kr328.webapi.api.subscriptions

import com.github.kr328.webapi.api.subscriptions.dumper.dumpShadowsocksD
import com.github.kr328.webapi.api.subscriptions.model.Shadowsocks
import com.github.kr328.webapi.api.subscriptions.model.ShadowsocksD
import com.github.kr328.webapi.api.subscriptions.parser.parseSurge
import org.springframework.http.HttpHeaders

private val REGEX_USER_INFO_SPLIT = Regex("[;\\s]")
private val REGEX_SURGE_CONFIG_SUFFIX = Regex("\\.(txt|conf)$")

data class ResponseInfo(var remark: String, var trafficUsed: Long, var trafficTotal: Long)

fun surge2Shadowsocks(data: String, name: String?, headers: HttpHeaders): String {
    val info = parseHeaders(headers, name)
    val proxy = parseSurge(data).proxy.filterIsInstance<Shadowsocks>()
    val shadowsocksD = ShadowsocksD(provider = info.remark,
            defaultShadowsocks = proxy.getOrNull(0) ?: Shadowsocks.EMPTY,
            servers = proxy,
            trafficUsed = info.trafficUsed,
            trafficTotal = info.trafficTotal)

    return dumpShadowsocksD(shadowsocksD)
}

private fun parseHeaders(headers: HttpHeaders, name: String?): ResponseInfo {
    val result = ResponseInfo("Unlabeled", 0, 0)

    for (line in headers["Subscription-UserInfo"]?.flatMap { it.split(REGEX_USER_INFO_SPLIT) } ?: emptyList()) {
        when {
            line.startsWith("upload=", ignoreCase = true) ->
                result.trafficUsed += line.removePrefix("upload=").toLong()
            line.startsWith("download=", ignoreCase = true) ->
                result.trafficUsed += line.removePrefix("download=").toLong()
            line.startsWith("total=", ignoreCase = true) ->
                result.trafficTotal += line.removePrefix("total=").toLong()
        }
    }

    result.remark = name ?: headers.contentDisposition.filename?.replace(REGEX_SURGE_CONFIG_SUFFIX, "") ?: "Unlabeled"

    return result
}
