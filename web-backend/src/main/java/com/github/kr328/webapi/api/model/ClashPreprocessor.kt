@file:UseSerializers(ClashPreprocessor.RegexSerializer::class, ClashPreprocessor.URLSerializer::class,
        ClashPreprocessor.RuleSerializer::class, ClashPreprocessor.RuleTargetMapSerializer::class)

package com.github.kr328.webapi.api.model

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import java.net.URL

@Serializable
data class ClashPreprocessor(
        @SerialName("clash-general")        val general: Map<String, String>,
        @SerialName("proxy-source")         val sources: List<ProxySource>,
        @SerialName("proxy-group-dispatch") val groups: ProxyGroupDispatch,
        @SerialName("rule-sets")            val ruleSets: List<RuleSet>,
        @SerialName("rule")                 val rules: List<Rule>) : Config {

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
    }

    @Serializable
    data class RuleSet(val name: String, val type: String, val url: URL,
                       @SerialName("target-map") val targetMap: List<String>)

    @Serializable
    data class RuleTargetMap(val source: String, val target: String)

    @Serializer(forClass = Regex::class)
    object RegexSerializer : KSerializer<Regex> {
        override val descriptor: SerialDescriptor = StringDescriptor
        override fun serialize(encoder: Encoder, obj: Regex) = encoder.encodeString(obj.pattern)
        override fun deserialize(decoder: Decoder): Regex = Regex(decoder.decodeString())
    }

    @Serializer(forClass = URL::class)
    object URLSerializer: KSerializer<URL> {
        override val descriptor: SerialDescriptor = StringDescriptor.withName(URL::javaClass.name)
        override fun deserialize(decoder: Decoder): URL = URL(decoder.decodeString())
        override fun serialize(encoder: Encoder, obj: URL) = encoder.encodeString(obj.toString())
    }

    @Serializer(forClass = Rule::class)
    object RuleSerializer : KSerializer<Rule> {
        override val descriptor: SerialDescriptor = StringDescriptor
        override fun serialize(encoder: Encoder, obj: Rule) = encoder.encodeString("${obj.matcher},${obj.pattern},${obj.target}")
        override fun deserialize(decoder: Decoder): Rule = decoder.decodeString().split(",")
                    .also { if ( it.size < 3 ) throw IllegalArgumentException("Rule: $it invalid") }
                    .let { Rule(it[0], it[1], it[2]) }
    }

    @Serializer(forClass = Regex::class)
    object RuleTargetMapSerializer : KSerializer<RuleTargetMap> {
        override val descriptor: SerialDescriptor = StringDescriptor
        override fun serialize(encoder: Encoder, obj: RuleTargetMap) = encoder.encodeString("${obj.source},${obj.target}")
        override fun deserialize(decoder: Decoder): RuleTargetMap = decoder.decodeString().split(",")
                .also { if ( it.size != 2 ) throw IllegalArgumentException("Rule set target map $it invalid")}
                .let { RuleTargetMap(it[0], it[1]) }
    }
}

