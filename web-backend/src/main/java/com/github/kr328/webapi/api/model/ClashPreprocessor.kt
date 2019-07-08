package com.github.kr328.webapi.api.model

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor

@Serializable
data class ClashPreprocessor(
        @SerialName("clash-general")        val general: Map<String, String>,
        @SerialName("proxy-source")         val sources: List<ProxySource>,
        @SerialName("proxy-group-dispatch") val groups: ProxyGroupDispatch) : Config {

    @Serializable
    data class ProxySource(val type: String, val url: String? = null, val file: String? = null)

    @Serializable
    data class ProxyGroupDispatch(val name: String,
                                  val type: String,
                                  val url: String? = null,
                                  val interval: Long? = null,
                                  @SerialName("proxies-filters") val proxyFilter: ProxyFilter? = null,
                                  @SerialName("flat-proxies") val flatProxy: List<String>? = null) {
        @Serializable
        data class ProxyFilter(
                @SerialName("black-regex") val black: Regex? = null,
                @SerialName("white-regex") val white: Regex? = null)

        @Serializer(forClass = Regex::class)
        companion object : KSerializer<Regex> {
            override val descriptor: SerialDescriptor = StringDescriptor.withName("Regex")
            override fun serialize(encoder: Encoder, obj: Regex) = encoder.encodeString(obj.pattern)
            override fun deserialize(decoder: Decoder): Regex = Regex(decoder.decodeString())
        }
    }

    companion object {
        const val TYPE = "clash_preprocessor"
    }

    override fun getType(): String = TYPE
}