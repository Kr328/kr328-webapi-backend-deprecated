package com.github.kr328.webapi.api.dumper

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.github.kr328.webapi.api.model.Shadowsocks
import com.github.kr328.webapi.api.model.ShadowsocksD
import java.util.*

fun dumpShadowsocksD(shadowsocksD: ShadowsocksD): String {
    val root = JSONObject()

    root["airport"] = shadowsocksD.provider
    root["port"] = shadowsocksD.defaultShadowsocks.port
    root["encryption"] = shadowsocksD.defaultShadowsocks.method
    root["password"] = shadowsocksD.defaultShadowsocks.password
    shadowsocksD.defaultShadowsocks.plugin?.let {
        root["plugin"] = it.plugin
        root["plugin_opts"] = it.pluginOptions
    }

    shadowsocksD.expires?.let { root["expiry"] = it }
    shadowsocksD.trafficTotal?.let { root["traffic_total"] = castByteToGiByte(it) }
    shadowsocksD.trafficUsed?.let { root["traffic_used"] = castByteToGiByte(it) }
    shadowsocksD.url?.let { root["url"] = it }

    root["servers"] = JSONArray(shadowsocksD.servers.map(::dumpSingleServer))

    return "ssd://" + Base64.getUrlEncoder().encodeToString(root.toString().toByteArray())
}

private fun castByteToGiByte(data: Long): Float = (data / 1024 / 1024 / 1024.0f)

private fun dumpSingleServer(shadowsocks: Shadowsocks): JSONObject {
    val result = JSONObject()

    result["remarks"] = shadowsocks.remarks
    result["server"] = shadowsocks.host
    result["port"] = shadowsocks.port
    result["encryption"] = shadowsocks.method
    result["password"] = shadowsocks.password

    shadowsocks.plugin?.let {
        result["plugin"] = it.plugin
        result["plugin_options"] = it.pluginOptions
    }

    shadowsocks.extras["id"]?.let { result["id"] = it }
    shadowsocks.extras["ratio"]?.let { result["ratio"] = it }

    return result
}