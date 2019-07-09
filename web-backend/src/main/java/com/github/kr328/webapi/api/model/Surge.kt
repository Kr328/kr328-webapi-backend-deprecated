package com.github.kr328.webapi.api.model

data class Surge(val general: Map<String, String>,
                 val proxy: List<Proxy>, val proxyGroup: Map<String, ProxyGroup>,
                 val rule: List<Rule>)

