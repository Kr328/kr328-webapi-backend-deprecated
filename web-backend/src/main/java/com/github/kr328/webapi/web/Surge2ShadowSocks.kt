package com.github.kr328.webapi.web

import com.github.kr328.webapi.api.subscriptions.surge2Shadowsocks
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
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
                .map { t -> surge2Shadowsocks(t.t2, name.orElse(null), t.t1.headers().asHttpHeaders()) }
                .flatMap { s -> ServerResponse.ok().body(Mono.just(s), String::class.java) }
                .switchIfEmpty(Mono.error(Exception("Empty surge config")))
                .onErrorResume { throwable -> ServerResponse.badRequest().body(Mono.just(throwable.toString()), String::class.java) }
    }
}

