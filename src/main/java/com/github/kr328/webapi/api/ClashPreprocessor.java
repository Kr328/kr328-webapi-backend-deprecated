package com.github.kr328.webapi.api;

import com.github.kr328.webapi.api.clash.exceptions.PreprocessorException;
import com.github.kr328.webapi.api.clash.exceptions.ProxySourceException;
import com.github.kr328.webapi.api.clash.exceptions.RuleSetException;
import com.github.kr328.webapi.api.clash.model.*;
import com.github.kr328.webapi.api.clash.utils.ProxyGroupDispatchLoader;
import com.github.kr328.webapi.api.clash.utils.ProxyGroupDispatchLoader.Metadata;
import com.github.kr328.webapi.api.clash.utils.ProxyLoader;
import com.github.kr328.webapi.api.clash.utils.RootUtils;
import com.github.kr328.webapi.api.clash.utils.RuleSetLoader;
import org.yaml.snakeyaml.Yaml;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ClashPreprocessor {
    public static Mono<String> process(String data) {
        ClashPreprocessorRoot root = RootUtils.loadClashPreprocessorRoot(data);

        if ( Optional.ofNullable(root.getPreprocessor()).map(Preprocessor::getVersion).orElse(-1) != 1 )
            return Mono.error(new PreprocessorException("Invalid preprocessor config version"));

        List<Metadata> groupMetadata = ProxyGroupDispatchLoader.load(Optional.ofNullable(root.getProxyGroupDispatch())
                .orElse(Collections.emptyList()));

        Flux<Proxy> proxies = Flux.fromIterable(Optional.ofNullable(root.getProxySources()).orElse(Collections.emptyList()))
                .flatMap(ProxyLoader::load)
                .switchIfEmpty(Mono.error(new ProxySourceException("Empty proxy source")))
                .doOnNext(p -> {
                    for ( Metadata metadata : groupMetadata )
                        if ( metadata.getWhitePattern().matcher(p.getName()).matches() && !metadata.getBlackPattern().matcher(p.getName()).matches() )
                            metadata.getProxies().add(p.getName());
                });

        HashMap<String, Flux<String>> ruleSets = new HashMap<>();
        for ( RuleSet ruleSet :  Optional.ofNullable(root.getRuleSets()).orElse(Collections.emptyList()) )
            ruleSets.put(ruleSet.getName(), RuleSetLoader.load(ruleSet));

        Mono<List<String>> rules = Flux.fromIterable(Optional.ofNullable(root.getRule()).orElse(Collections.emptyList()))
                .flatMap(s -> {
                    if ( s.startsWith("RULE-SET,") )
                        return Optional.ofNullable(ruleSets.get(s.substring(9))).orElse(Flux.error(new RuleSetException("Rule Set " + s + " not found")));
                    return Mono.just(s);
                })
                .collectList();

        return Mono.just(new LinkedHashMap<String, Object>())
                .doOnNext(m -> m.putAll(root.getClashGeneral()))
                .zipWith(proxies.collectList())
                .doOnNext(t -> t.getT1().put("Proxy", t.getT2()))
                .map(Tuple2::getT1)
                .zipWith(ProxyGroupDispatchLoader.buildProxyGroups(groupMetadata).collectList())
                .doOnNext(t -> t.getT1().put("Proxy Group", t.getT2()))
                .map(Tuple2::getT1)
                .zipWith(rules)
                .doOnNext(t -> t.getT1().put("Rule" ,t.getT2()))
                .map(Tuple2::getT1)
                .map(RootUtils::dump);
    }
}
