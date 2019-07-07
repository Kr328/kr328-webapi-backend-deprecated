package com.github.kr328.webapi.web;

import com.github.kr328.webapi.api.subscriptions.Surge2ShadowsocksKt;
import com.github.kr328.webapi.tools.ResponseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

@Controller
@SuppressWarnings("unused")
public class Surge2ShadowSocks {
    public Mono<ServerResponse> process(ServerRequest request) {
        Optional<String> url = request.queryParam("url");
        Optional<String> name = request.queryParam("name");

        if (url.isEmpty())
            return ServerResponse.badRequest().body(Mono.just("Query param 'url' required"), String.class);

        String decodedUrl = new String(Base64.getUrlDecoder().decode(url.get()));
        if (!decodedUrl.startsWith("http"))
            return ServerResponse.badRequest().body(Mono.just("Query param 'url' require Base64URL encoded string"), String.class);

        return WebClient.create()
                .get()
                .uri(decodedUrl)
                //.headers(httpHeaders -> httpHeaders.addAll(request.headers().asHttpHeaders()))
                .exchange()
                .timeout(Duration.ofMinutes(1))
                .filter(response -> response.statusCode().is2xxSuccessful())
                .zipWhen(clientResponse -> clientResponse.bodyToMono(String.class))
                .map(t -> Surge2ShadowsocksKt.surge2Shadowsocks(t.getT2(), name.orElse(null), t.getT1().headers().asHttpHeaders()))
                .flatMap(s -> ServerResponse.ok().body(Mono.just(s), String.class))
                .switchIfEmpty(Mono.error(new Exception("Empty surge config")))
                .onErrorResume(throwable -> ServerResponse.badRequest().body(Mono.just(throwable.toString()), String.class));
    }
}

