package com.github.kr328.webapi.web

import com.github.kr328.webapi.api.EXTRA_SHADOWSOCKS_D_PROVIDER_NAME
import com.github.kr328.webapi.api.EXTRA_SHADOWSOCKS_D_TRAFFIC_TOTAL
import com.github.kr328.webapi.api.EXTRA_SHADOWSOCKS_D_TRAFFIC_USED
import com.github.kr328.webapi.api.surge2Shadowsocks
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

class Surge2ShadowSocks {
    fun process(request: ServerRequest): Mono<ServerResponse> {
        val url = request.queryParam("url")
        val name = request.queryParam("name")

        if (url.isEmpty)
            return ServerResponse.badRequest().body(Mono.just("Query param 'url' required"), String::class.java)

        val decodedUrl = String(Base64.getUrlDecoder().decode(url.get()))
        return if (!decodedUrl.startsWith("http")) ServerResponse.badRequest().body(Mono.just("Query param 'url' require Base64URL encoded string"), String::class.java) else WebClient.create()
                .get()
                .uri(decodedUrl)
                .exchange()
                .timeout(Duration.ofMinutes(1))
                .filter { response -> response.statusCode().is2xxSuccessful }
                .zipWhen { clientResponse -> clientResponse.bodyToMono(String::class.java) }
                .map { t -> surge2Shadowsocks(t.t2, parseExtras(t.t1.headers().asHttpHeaders(), name.orElse(null))) }
                .flatMap { s -> ServerResponse.ok().body(Mono.just(s), String::class.java) }
                .switchIfEmpty(Mono.error(Exception("Empty surge config")))
                .onErrorResume { throwable -> ServerResponse.badRequest().body(Mono.just(throwable.toString()), String::class.java) }
    }

    private fun parseExtras(headers: HttpHeaders, name: String?): Map<String, String?> {
        var trafficUsed: Long? = null
        var trafficTotal: Long? = null

        for (line in headers["Subscription-UserInfo"]?.flatMap { it.split(REGEX_USER_INFO_SPLIT) } ?: emptyList()) {
            when {
                line.startsWith("upload=", ignoreCase = true) ->
                    trafficUsed = line.removePrefix("upload=").toLong() + (trafficUsed ?: 0L)
                line.startsWith("download=", ignoreCase = true) ->
                    trafficUsed = line.removePrefix("download=").toLong() + (trafficUsed ?: 0L)
                line.startsWith("total=", ignoreCase = true) ->
                    trafficTotal = line.removePrefix("total=").toLong() + (trafficTotal ?: 0L)
            }
        }

        return mapOf(
                EXTRA_SHADOWSOCKS_D_PROVIDER_NAME to (name ?: headers.contentDisposition.filename?.replace(REGEX_SURGE_CONFIG_SUFFIX, "") ?: "Unlabeled"),
                EXTRA_SHADOWSOCKS_D_TRAFFIC_USED to trafficUsed?.toString(),
                EXTRA_SHADOWSOCKS_D_TRAFFIC_TOTAL to trafficTotal?.toString()
        )
    }

    companion object {
        private val REGEX_USER_INFO_SPLIT = Regex("[;\\s]")
        private val REGEX_SURGE_CONFIG_SUFFIX = Regex("\\.(txt|conf)$")
    }
}

