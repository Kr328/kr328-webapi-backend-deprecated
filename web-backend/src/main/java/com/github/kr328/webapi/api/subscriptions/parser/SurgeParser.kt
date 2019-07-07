package com.github.kr328.webapi.api.subscriptions.parser

import com.github.kr328.webapi.api.subscriptions.model.*
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.streams.toList

private enum class Status {
    EMPTY, STATUS_GENERAL, STATUS_PROXY, STATUS_PROXY_GROUP, STATUS_RULE
}

private val REGEX_GENERAL_SPLIT = Regex("(\\s*=\\s*)")
private val REGEX_PROXY_SPLIT = Regex("(\\s*[=,]\\s*)")
private val REGEX_PROXY_GROUP_SPLIT = Regex("(\\s*[=,]\\s*)")

fun parseSurge(body: String): Surge {
    var status: Status = Status.EMPTY

    val generalLines: MutableList<String> = mutableListOf()
    val proxyLines: MutableList<String> = mutableListOf()
    val proxyGroupLines: MutableList<String> = mutableListOf()
    val ruleLines: MutableList<String> = mutableListOf()

    loop@ for (line in body.split("[\\r\\n]+").map(String::trim)) {
        when {
            line.isEmpty() -> continue@loop
            line.startsWith("//") -> continue@loop
        }

        when (line) {
            "[General]" -> status = Status.STATUS_GENERAL
            "[Proxy]" -> status = Status.STATUS_PROXY
            "[Proxy Group]" -> status = Status.STATUS_PROXY_GROUP
            "[Rule]" -> status = Status.STATUS_RULE
            "[URL Rewrite]", "[Header Rewrite]" -> {
            }
            else -> {
                when (status) {
                    Status.STATUS_GENERAL -> generalLines.add(line)
                    Status.STATUS_PROXY -> proxyLines.add(line)
                    Status.STATUS_PROXY_GROUP -> proxyGroupLines.add(line)
                    Status.STATUS_RULE -> ruleLines.add(line)
                    else -> {
                    }
                }
            }
        }
    }

    val general: Map<String, String> = generalLines.stream()
            .map {
                it.split(REGEX_GENERAL_SPLIT, 2)
            }
            .filter {
                it.size == 2
            }
            .collect(Collectors.toMap({ it[0] }, { it[1] }))

    val proxy: List<Proxy> = proxyLines.stream()
            .map {
                it.split(REGEX_PROXY_SPLIT)
            }
            .filter {
                it.size >= 2
            }
            .flatMap {
                when {
                    it[1] == "direct" -> Stream.of(CommonProxy(it[0], "direct"))
                    it[1] == "custom" -> {
                        when {
                            it.size < 6 -> Stream.empty()

                            it[5].endsWith("SSEncrypt.module") -> Stream.of(parseShadowsocks(it))

                            else -> Stream.empty()
                        }
                    }
                    else -> Stream.empty()
                }
            }
            .toList()

    val proxyGroup: Map<String, List<String>> = proxyGroupLines.stream()
            .map {
                it.split(REGEX_PROXY_GROUP_SPLIT)
            }
            .collect(Collectors.toMap({ it[0] }, { it.subList(1, it.size) }))

    val rule: List<Rule> = ruleLines.stream()
            .map { it.split(",") }
            .filter { it.size >= 3 }
            .map { Rule(matcher = it[0], pattern = it[1], target = it[2], extra = it.subList(2, it.size)) }
            .toList()

    return Surge(general = general, proxy = proxy, proxyGroup = proxyGroup, rule = rule)
}

private fun parseShadowsocks(data: List<String>): Shadowsocks {
    var plugin: ShadowsocksPlugin? = null

    for (element in data) {
        if (element.startsWith("obfs=") or element.startsWith("obfs-host="))
            plugin = plugin?.apply { this.pluginOptions += ";$element" } ?: ShadowsocksPlugin("obfs-local", element)
    }

    return Shadowsocks(remark = data[0], host = data[2], port = data[3].toInt(), method = data[4], password = data[5],
            plugin = plugin)
}