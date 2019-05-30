package com.github.kr328.webapi.api.clash.utils;

import com.github.kr328.webapi.api.clash.exceptions.ProxySourceException;
import com.github.kr328.webapi.api.clash.model.ClashRoot;
import com.github.kr328.webapi.api.clash.model.Proxy;
import com.github.kr328.webapi.api.clash.model.ProxySource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Optional;

public class ProxyLoader {
    public static Flux<Proxy> load(ProxySource source) {
        switch (source.getType()) {
            case "url":
                String url = Optional.ofNullable(source.getUrl()).map(String::trim).orElse("");

                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    return Flux.error(new ProxySourceException("Invalid url " + url));

                return WebClient.create()
                        .get()
                        .uri(url)
                        .header("Referer", "https://webapi.kr328.app/preclash")
                        .header("Kr328WebAPIName", "preclash")
                        .exchange()
                        .timeout(Duration.ofSeconds(30))
                        .flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
                        .map(RootUtils::loadClashRoot)
                        .flatMapIterable(ClashRoot::getProxy);
            case "plain":
                if (source.getData() == null)
                    return Flux.error(new ProxySourceException("Empty data"));
                return Flux.just(source.getData());
            default:
                return Flux.error(new ProxySourceException("Unsupported type " + source.getType()));
        }
    }
}
