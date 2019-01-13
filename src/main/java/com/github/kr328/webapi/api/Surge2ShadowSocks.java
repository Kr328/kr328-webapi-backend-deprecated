package com.github.kr328.webapi.api;

import com.alibaba.fastjson.JSON;
import com.github.kr328.webapi.utils.Proxy;
import com.github.kr328.webapi.utils.ShadowSocksD;
import com.github.kr328.webapi.utils.Surge;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Base64;

@RestController
@SuppressWarnings("unused")
public class Surge2ShadowSocks {
    @GetMapping("/surge2ssd")
    @PostMapping("/surge2ssd")
    public Mono<String> process(@RequestParam(value = "url") String url , @RequestParam(required = false) String name) {
        Proxy.Provider provider = new Proxy.Provider();
        provider.name = name == null ? "Unlabeled" : name;

        return WebClient.create()
                .get()
                .uri(new String(Base64.getUrlDecoder().decode(url)))
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful() ,
                        response -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST ,"Config download failure.")))
                .bodyToMono(String.class)
                .map(s -> s.split("\n"))
                .filter(Surge::detectSurge)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST ,"Unsupported config type.")))
                .flatMapMany(Surge::splitProxyLines)
                .flatMap(s -> Surge.parseProxy(s ,provider))
                .collect(ShadowSocksD.toJsonCollector())
                .map(JSON::toString)
                .map(s -> "ssd://" + Base64.getUrlEncoder().encodeToString(s.getBytes()));
    }
}

