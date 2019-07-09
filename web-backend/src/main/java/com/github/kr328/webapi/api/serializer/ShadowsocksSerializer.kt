package com.github.kr328.webapi.api.serializer

import com.github.kr328.webapi.api.model.ShadowsocksD
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.util.*

class ShadowsocksSerializer {
    private val json = Json(JsonConfiguration(encodeDefaults = false))

    fun stringify(obj: ShadowsocksD): String = json.stringify(ShadowsocksD.serializer(), obj)
            //"ssd://" + Base64.getUrlEncoder().encodeToString(.toByteArray())

    fun parse(string: String): ShadowsocksD =
            json.parse(ShadowsocksD.serializer(), String(Base64.getDecoder().decode(string)))
}