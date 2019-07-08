package com.github.kr328.webapi.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Surge(val general: Map<String, String>,
                 val proxy: List<Proxy>, val proxyGroup: Map<String, ProxyGroup>,
                 val rule: List<Rule>)

