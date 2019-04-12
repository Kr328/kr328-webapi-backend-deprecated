package com.github.kr328.webapi.api;

import com.github.kr328.webapi.core.subscriptions.proxy.Proxy;
import com.github.kr328.webapi.core.subscriptions.subscription.BaseSubscription;
import com.github.kr328.webapi.tools.Subscriptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.Base64;
import java.util.Optional;

@Controller
@SuppressWarnings("unused")
public class Surge2ShadowSocks {
    @Autowired
    private Subscriptions subscriptions;

    public Mono<ServerResponse> process(ServerRequest request) {
        Optional<String> url = request.queryParam("url");
        Optional<String> name = request.queryParam("name");

        if (!url.isPresent())
            return ServerResponse.badRequest().body(Mono.just("Query param 'url' required"), String.class);

        String decodedUrl = new String(Base64.getUrlDecoder().decode(url.get()));
        if (!decodedUrl.startsWith("http"))
            return ServerResponse.badRequest().body(Mono.just("Query param 'url' require Base64URL encoded string"), String.class);

        return WebClient.create()
                .method(request.method())
                .uri(decodedUrl)
                .exchange()
                .onErrorResume(throwable -> Mono.empty())
                .filter(clientResponse -> clientResponse.statusCode().is2xxSuccessful())
                .flatMap(clientResponse -> Mono.zip(Mono.just(HttpHeaders.writableHttpHeaders(clientResponse.headers().asHttpHeaders())),
                        clientResponse.bodyToMono(String.class).switchIfEmpty(Mono.just(""))))
                .map(t -> Tuples.of(t.getT1(), parseFromRequest(t.getT1(), t.getT2(), name.orElse(""))))
                .map(t -> Tuples.of(t.getT1(), buildForResponse(t.getT1(), t.getT2())))
                .flatMap(p -> ServerResponse.ok().headers((headers) -> headers.setAll(p.getT1().toSingleValueMap())).body(Mono.just(p.getT2()), String.class))
                .switchIfEmpty(ServerResponse.status(502).body(Mono.just("Upstream subscription service unreachable"), String.class))
                .onErrorResume(throwable -> ServerResponse.badRequest().body(Mono.just("Conversion failure " + throwable.getCause().getClass().getSimpleName() + " " + throwable.getCause().getMessage()), String.class));
    }

    private Proxy[] parseFromRequest(HttpHeaders httpHeaders, String body, String nameOverride) {
        if (!nameOverride.isEmpty())
            httpHeaders.setContentDisposition(ContentDisposition.builder("attachment").filename(nameOverride + ".conf").build());

        try {
            return subscriptions.getSurgeSubscription().parseFromRequest(httpHeaders, body);
        } catch (BaseSubscription.ParseException e) {
            throw new WebServerException("parse config failure", e);
        }
    }

    private String buildForResponse(HttpHeaders httpHeaders, Proxy[] proxies) {
        return subscriptions.getShadowsocksDSubscription().buildForResponse(httpHeaders, proxies);
    }
}

