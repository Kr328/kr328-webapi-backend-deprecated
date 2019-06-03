package com.github.kr328.webapi.api.clash.utils;

import com.github.kr328.webapi.api.clash.exceptions.RuleSetException;
import com.github.kr328.webapi.api.clash.model.ClashRoot;
import com.github.kr328.webapi.api.clash.model.RuleSet;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class RuleSetLoader {
    public static Mono<RuleSetData> load(RuleSet set) {
        if (!"url".equals(set.getType()))
            return Mono.error(new RuleSetException("Unsupported type " + set.getType()));

        String url = Optional.ofNullable(set.getUrl()).map(String::trim).orElse("");
        if (!url.startsWith("https://") && !url.startsWith("http://"))
            return Mono.error(new RuleSetException("Unsupported url " + url));

        HashMap<String, String> target = new HashMap<>();

        Optional.ofNullable(set.getTargetMap()).orElse(Collections.emptyList())
                .stream()
                .map(s -> s.split(","))
                .filter(ss -> ss.length == 2)
                .forEach(ss -> target.put(ss[0], ss[1]));

        return WebClient.create()
                .get()
                .uri(url)
                .header("Referer", "https://webapi.kr328.app/preclash")
                .exchange()
                .timeout(Duration.ofSeconds(30), Mono.error(new RuleSetException("Load RuleSet from " + url + " timeout")))
                .filter(clientResponse -> clientResponse.statusCode().is2xxSuccessful())
                .flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
                .map(RootUtils::loadClashRoot)
                .flatMapIterable(ClashRoot::getRule)
                .map(s -> s.split(",+"))
                .filter(ss -> ss.length > 1)
                .doOnNext(ss -> Optional.ofNullable(target.get(ss[ss.length - 1])).ifPresent(m -> ss[ss.length - 1] = m))
                .map(ss -> String.join(",", ss))
                .switchIfEmpty(Flux.error(new RuleSetException("Load RuleSet from " + url + " failure")))
                .collectList()
                .map(l -> new RuleSetData(set.getName(), l));
    }

    @Data
    @AllArgsConstructor
    public static class RuleSetData {
        private String name;
        private List<String> rule;
    }
}
