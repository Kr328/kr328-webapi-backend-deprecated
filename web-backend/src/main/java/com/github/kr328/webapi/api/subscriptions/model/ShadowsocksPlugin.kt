package com.github.kr328.webapi.api.subscriptions.model

data class ShadowsocksPlugin(val plugin: String, val pluginOptions: String) {
    constructor(pair: Pair<String, String>) : this(pair.first, pair.second)
}