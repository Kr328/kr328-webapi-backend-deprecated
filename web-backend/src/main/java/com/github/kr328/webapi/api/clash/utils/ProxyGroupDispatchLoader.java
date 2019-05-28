package com.github.kr328.webapi.api.clash.utils;

import com.github.kr328.webapi.api.clash.model.ProxyGroupDispatch;
import lombok.Data;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.regex.Pattern;

public class ProxyGroupDispatchLoader {
    @Data
    public static class Metadata {
        private ProxyGroupDispatch dispatch;
        private Pattern whitePattern = PATTERN_MATCHES_ANY;
        private Pattern blackPattern = PATTERN_MATCHES_NONE;
        private ArrayList<String> proxies = new ArrayList<>();
    }

    public static List<Metadata> load(Collection<ProxyGroupDispatch> dispatches) {
        ArrayList<Metadata> result = new ArrayList<>();

        for ( ProxyGroupDispatch dispatch : dispatches ) {
            Metadata metadata = new Metadata();

            metadata.dispatch = dispatch;
            metadata.whitePattern = Optional.ofNullable(dispatch.getProxiesFilters())
                    .map(ProxyGroupDispatch.ProxiesFilters::getWhiteRegex)
                    .map(Pattern::compile).orElse(PATTERN_MATCHES_ANY);
            metadata.blackPattern = Optional.ofNullable(dispatch.getProxiesFilters())
                    .map(ProxyGroupDispatch.ProxiesFilters::getBlackRegex)
                    .map(Pattern::compile).orElse(PATTERN_MATCHES_NONE);
            metadata.proxies.addAll(Optional.ofNullable(dispatch.getFlatProxies()).orElse(Collections.emptyList()));

            result.add(metadata);
        }

        return result;
    }

    public static Flux<LinkedHashMap<String, Object>> buildProxyGroups(List<Metadata> metadata) {
        return Flux.fromIterable(metadata)
                .map(m -> {
                    LinkedHashMap<String, Object> r = new LinkedHashMap<>();

                    Optional.ofNullable(m.dispatch.getName()).ifPresent(n -> r.put("name", n));
                    Optional.ofNullable(m.dispatch.getType()).ifPresent(t -> r.put("type", t));
                    Optional.ofNullable(m.dispatch.getUrl()).ifPresent(u -> r.put("url", u));
                    if ( m.dispatch.getInterval() != -1 ) r.put("interval", m.dispatch.getInterval());
                    r.put("proxies", m.proxies);

                    return r;
                });
    }

    private static final Pattern PATTERN_MATCHES_ANY = Pattern.compile(".*");
    private static final Pattern PATTERN_MATCHES_NONE = Pattern.compile("");
}
