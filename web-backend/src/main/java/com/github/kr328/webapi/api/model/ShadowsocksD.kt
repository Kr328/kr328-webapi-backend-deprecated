@file:UseSerializers(ShadowsocksD.ShadowsocksSerializer::class,
        ShadowsocksD.DateSerializer::class, ShadowsocksD.URLSerializer::class)

package com.github.kr328.webapi.api.model

import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl
import kotlinx.serialization.internal.StringDescriptor
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class ShadowsocksD(@SerialName("airport")        val provider: String,
                        @SerialName("servers")        val servers: List<Shadowsocks>,
                        @SerialName("traffic_used")   val trafficUsed: Long? = null,
                        @SerialName("traffic_total")  val trafficTotal: Long? = null,
                        @SerialName("expiry")         val expires: Date? = null,
                        @SerialName("url")            val url: URL? = null,
                        @SerialName("encryption")     val method: String,
                        @SerialName("port")           val port: Int,
                        @SerialName("password")       val password: String,
                        @SerialName("plugin")         val plugin: String? = null,
                        @SerialName("plugin_options") val pluginOptions: String? = null) : Config {
    constructor(provider: String,
                servers: List<Shadowsocks>,
                trafficUsed: Long?,
                trafficTotal: Long?,
                expires: Date?,
                url: URL?,
                defaultShadowsocks: Shadowsocks) :
            this(provider, servers, trafficUsed, trafficTotal, expires, url,
                    defaultShadowsocks.method, defaultShadowsocks.port, defaultShadowsocks.password,
                    defaultShadowsocks.plugin?.plugin, defaultShadowsocks.plugin?.pluginOptions)

    companion object {
        const val EXTRA_PROXY_ID: String = "EXTRA_PROXY_ID"
        const val EXTRA_PROXY_RATIO: String = "EXTRA_PROXY_RATIO"
    }

    @Serializer(forClass = URL::class)
    object URLSerializer: KSerializer<URL> {
        override val descriptor: SerialDescriptor = StringDescriptor.withName(URL::javaClass.name)
        override fun deserialize(decoder: Decoder): URL = URL(decoder.decodeString())
        override fun serialize(encoder: Encoder, obj: URL) = encoder.encodeString(obj.toString())
    }

    @Serializer(forClass = Date::class)
    object DateSerializer: KSerializer<Date> {
        private val FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        override val descriptor: SerialDescriptor = StringDescriptor.withName(Date::javaClass.name)
        override fun deserialize(decoder: Decoder): Date = FORMAT.parse(decoder.decodeString())
        override fun serialize(encoder: Encoder, obj: Date) = encoder.encodeString(FORMAT.format(obj))
    }

    @Serializer(forClass = Shadowsocks::class)
    object ShadowsocksSerializer: KSerializer<Shadowsocks> {
        override val descriptor: SerialDescriptor = SerialClassDescImpl(Shadowsocks::javaClass.name).apply {
            addElement("remarks")
            addElement("server")
            addElement("port")
            addElement("encryption")
            addElement("password")
            addElement("plugin", true)
            addElement("plugin_options", true)
            addElement("id", true)
            addElement("ratio", true)
        }

        override fun deserialize(decoder: Decoder): Shadowsocks {
            TODO()
        }

        override fun serialize(encoder: Encoder, obj: Shadowsocks) {
            with(encoder.beginStructure(descriptor)) {
                encodeStringElement(descriptor, 0, obj.remarks)
                encodeStringElement(descriptor, 1, obj.host)
                encodeIntElement(descriptor, 2, obj.port)
                encodeStringElement(descriptor, 3, obj.method)
                encodeStringElement(descriptor, 4, obj.password)

                obj.plugin?.let {
                    encodeStringElement(descriptor, 5, it.plugin)
                    encodeStringElement(descriptor, 6, it.pluginOptions)
                }
                obj.extras[EXTRA_PROXY_RATIO]?.let {
                    encodeFloatElement(descriptor, 7, it.toFloat())
                }
                obj.extras[EXTRA_PROXY_ID]?.let {
                    encodeIntElement(descriptor, 8, it.toInt())
                }

                endStructure(descriptor)
            }
        }
    }
}