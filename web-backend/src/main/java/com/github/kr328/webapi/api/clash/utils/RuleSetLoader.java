package com.github.kr328.webapi.api.clash.utils;

import com.github.kr328.webapi.api.clash.exceptions.RuleSetException;
import com.github.kr328.webapi.api.clash.model.ClashRoot;
import com.github.kr328.webapi.api.clash.model.RuleSet;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

public class RuleSetLoader {
    public static Flux<String> load(RuleSet set) {
        if ( !"url".equals(set.getType()) )
            return Flux.error(new RuleSetException("Unsupported type " + set.getType()));

        String url = Optional.ofNullable(set.getUrl()).map(String::trim).orElse("");
        if ( !url.startsWith("https://") && !url.startsWith("http://") )
            return Flux.error(new RuleSetException("Unsupported url " + url));

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
                .header("Kr328WebAPIName", "preclash")
                .exchange()
                .timeout(Duration.ofSeconds(30))
                .flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
                .map(RootUtils::loadClashRoot)
                .flatMapIterable(ClashRoot::getRule)
                .map(s -> s.split(",+"))
                .filter(ss -> ss.length > 1 )
                .doOnNext(ss -> Optional.ofNullable(target.get(ss[ss.length-1])).ifPresent(m -> ss[ss.length-1] = m))
                .map(ss -> String.join(",", ss));
    }
}
