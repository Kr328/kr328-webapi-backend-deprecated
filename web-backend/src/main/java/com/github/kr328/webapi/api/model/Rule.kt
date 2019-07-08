package com.github.kr328.webapi.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Rule(val matcher: String, val pattern: String, val target: String, val extra: List<String> = mutableListOf()) {
    companion object {
        const val MATCHER_DOMAIN_SUFFIX = "DOMAIN-SUFFIX"
        const val MATCHER_DOMAIN_KEYWORD = "DOMAIN-KEYWORD"
        const val MATCHER_DOMAIN = "DOMAIN"
        const val MATCHER_IP_CIDR = "IP-CIDR"
        const val MATCHER_USER_AGENT = "USER-AGENT"
        const val MATCHER_PROCESS_NAME = "PROCESS-NAME"
        const val MATCHER_URL_REGEX = "URL-REGEX"
        const val MATCHER_GEOIP = "GEOIP"
        const val MATCHER_FINAL = "FINAL"
    }
}