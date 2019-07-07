package com.github.kr328.webapi.api.subscriptions.model

data class Surge(val general: Map<String, Any>,
                 val proxy: List<Proxy>, val proxyGroup: Map<String, List<String>>,
                 val rule: List<Rule>)

