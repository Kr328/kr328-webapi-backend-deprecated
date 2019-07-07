package com.github.kr328.webapi.api.subscriptions.parser

import com.github.kr328.webapi.api.subscriptions.model.*
import java.util.stream.Stream

private enum class Status {
    EMPTY, STATUS_GENERAL, STATUS_PROXY, STATUS_PROXY_GROUP, STATUS_RULE
}

private val REGEX_GENERAL_SPLIT = Regex("(\\s*=\\s*)")
private val REGEX_NAME_SPLIT = Regex("(\\s*=\\s*)")
private val REGEX_ARGS_SPLIT = Regex("(\\s*,\\s*)")

@Suppress("UNCHECKED_CAST")
fun parseSurge(body: String): Surge {
    var status: Status = Status.EMPTY

    val general: MutableMap<String, String> = mutableMapOf()
    val proxy: MutableList<Proxy> = mutableListOf()
    val proxyGroup: MutableMap<String, ProxyGroup> = mutableMapOf()
    val rule: MutableList<Rule> = mutableListOf()

    loop@ for (line in body.split('\r', '\n').map(String::trim)) {
        when {
            line.isEmpty() -> continue@loop
            line.startsWith("//") -> continue@loop
            line.startsWith("#") -> continue@loop
            line.startsWith("[") and line.endsWith("]") ->
                status = when (line) {
                    "[General]" -> Status.STATUS_GENERAL
                    "[Proxy]"   -> Status.STATUS_PROXY
                    "[Proxy Group]" -> Status.STATUS_PROXY_GROUP
                    "[Rule]"    -> Status.STATUS_RULE
                    else -> Status.EMPTY
                }
            else -> {
                when (status) {
                    Status.STATUS_GENERAL -> {
                        Stream.of(line)
                                .map { it.split(REGEX_GENERAL_SPLIT, 2) }
                                .filter { it.size == 2 }
                                .forEach { general[it[0]] = it[1] }
                    }
                    Status.STATUS_PROXY -> {
                        Stream.of(line)
                                .map { it.split(REGEX_NAME_SPLIT, 2) }
                                .filter { it.size >= 2 }
                                .map { listOf(it[0]) + it[1].split(REGEX_ARGS_SPLIT) }
                                .flatMap {
                                    when {
                                        it[1] == "direct" -> Stream.of(CommonProxy(it[0], "direct"))
                                        it[1] == "custom" -> {
                                            when {
                                                (it.size > 6) and it[6].endsWith("SSEncrypt.module") -> Stream.of(parseShadowsocks(it))

                                                else -> Stream.empty()
                                            }
                                        }
                                        else -> Stream.empty()
                                    }
                                }
                                .forEach { proxy.add(it) }
                    }
                    Status.STATUS_PROXY_GROUP -> {
                        Stream.of(line)
                                .map { it.split(REGEX_NAME_SPLIT, 2) }
                                .filter { it.size == 2 }
                                .map { listOf(it[0]) + it[1].split(REGEX_ARGS_SPLIT) }
                                .forEach {
                                    proxyGroup[it[0]] = when ( it[1] ) {
                                        "select" -> ProxyGroup(ProxyGroup.TypeSelect(), it.subList(2, it.size))
                                        "url-test" ->
                                            parseProxyGroupArgs(it.subList(2, it.size)).let {
                                                m -> ProxyGroup(ProxyGroup.TypeUrlTest(m["url"].toString(),
                                                    m["interval"]?.toString()?.toLong(),
                                                    m["tolerance"]?.toString()?.toLong(),
                                                    m["timeout"]?.toString()?.toLong()),
                                                    m["proxies"] as List<String>)
                                            }
                                        "fallback" ->
                                            parseProxyGroupArgs(it.subList(2, it.size)).let {
                                                m -> ProxyGroup(ProxyGroup.TypeFallback(m["url"].toString(),
                                                    m["interval"]?.toString()?.toLong()),
                                                    m["proxies"] as List<String>)
                                            }
                                        else -> ProxyGroup(ProxyGroup.TypeUnknown(it[1]), it.subList(2, it.size))
                                    }
                                }
                    }
                    Status.STATUS_RULE -> {
                        Stream.of(line)
                                .map { it.split(",") }
                                .filter { it.size >= 3 }
                                .map { Rule(matcher = it[0], pattern = it[1], target = it[2], extra = it.subList(3, it.size)) }
                                .forEach { rule.add(it) }
                    }
                    else -> {}
                }
            }
        }
    }

    return Surge(general = general, proxy = proxy, proxyGroup = proxyGroup, rule = rule)
}

private fun parseShadowsocks(data: List<String>): Shadowsocks {
    var plugin: Pair<String, String>? = null

    val extras: MutableMap<String, String> = mutableMapOf()

    for (element in data.subList(7, data.size).distinct()) {
        when {
            element.startsWith("obfs=") or element.startsWith("obfs-host=") ->
                plugin = plugin?.apply { this.copy(second = "$second;$element") } ?: Pair("obfs-local", "$element;")
            element.contains('=') ->
                element.split(REGEX_NAME_SPLIT, limit = 2).let { extras[it[0]] = it[1] }
            else ->
                extras[element] = "true"
        }
    }

    return Shadowsocks(remarks = data[0], host = data[2], port = data[3].toInt(), method = data[4], password = data[5],
            plugin = plugin?.let(::ShadowsocksPlugin) , extras = extras)
}

private fun parseProxyGroupArgs(data: List<String>): Map<String, Any> {
    val result: MutableMap<String, Any> = mutableMapOf()
    val proxies: MutableList<String> = mutableListOf()

    for ( e in data ) {
        if ( e.contains('=') )
            e.split(REGEX_NAME_SPLIT, limit = 2).let { result[it[0]] = it[1] }
        else
            proxies.add(e)
    }

    result["proxies"] = proxies

    return result
}