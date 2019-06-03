package com.github.kr328.webapi.api;

import com.github.kr328.webapi.api.clash.exceptions.DispatcherException;
import com.github.kr328.webapi.api.clash.exceptions.PreprocessorException;
import com.github.kr328.webapi.api.clash.exceptions.ProxySourceException;
import com.github.kr328.webapi.api.clash.exceptions.RuleSetException;
import com.github.kr328.webapi.api.clash.model.ClashPreprocessorRoot;
import com.github.kr328.webapi.api.clash.model.Preprocessor;
import com.github.kr328.webapi.api.clash.model.Proxy;
import com.github.kr328.webapi.api.clash.utils.ProxyGroupDispatchLoader;
import com.github.kr328.webapi.api.clash.utils.ProxyGroupDispatchLoader.ProxyGroupData;
import com.github.kr328.webapi.api.clash.utils.ProxyLoader;
import com.github.kr328.webapi.api.clash.utils.RootUtils;
import com.github.kr328.webapi.api.clash.utils.RuleSetLoader;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClashPreprocessor {
    public static Mono<String> process(String data) {
        ClashPreprocessorRoot root = RootUtils.loadClashPreprocessorRoot(data);

        return Mono.just(new LinkedHashMap<String, Object>())
                // check preprocessor version
                .filter(l -> Optional.ofNullable(root.getPreprocessor()).map(Preprocessor::getVersion).orElse(-1) == 1)
                .switchIfEmpty(Mono.error(new PreprocessorException("Invalid preprocessor config version")))
                // load and put clash general
                .zipWith(Mono.justOrEmpty(root.getClashGeneral()))
                .switchIfEmpty(Mono.error(new PreprocessorException("Empty clash-general")))
                .doOnNext(t -> t.getT1().putAll(t.getT2()))
                .map(Tuple2::getT1)
                // load proxy from proxy source
                .zipWith(Mono.justOrEmpty(root.getProxySources())
                        .flatMapIterable(l -> l)
                        .flatMap(ProxyLoader::load)
                        .switchIfEmpty(Mono.error(new ProxySourceException("Empty proxy-sources")))
                        .collectList())
                .doOnNext(t -> t.getT1().put("Proxy", t.getT2()))
                //load and dispatch proxy
                .zipWhen(t -> Mono.justOrEmpty(root.getProxyGroupDispatch())
                        .map(ProxyGroupDispatchLoader::load)
                        .flatMapIterable(l -> l)
                        .switchIfEmpty(Mono.error(new DispatcherException("Empty proxy-group-dispatch")))
                        .collectList())
                .doOnNext(tt -> tt.getT1().getT2().stream()
                        .map(Proxy::getName)
                        .forEach(name -> tt.getT2().stream()
                                .filter(pd -> pd.getWhitePattern().matcher(name).matches())
                                .filter(pd -> !pd.getBlackPattern().matcher(name).matches())
                                .forEach(pd -> pd.getProxies().add(name))))
                .doOnNext(tt -> tt.getT1().getT1().put("Proxy Group", tt.getT2().stream()
                        .map(ProxyGroupData::toMap)
                        .collect(Collectors.toList())))
                .map(tt -> tt.getT1().getT1())
                // load rule set and expend rule
                .zipWith(Mono.justOrEmpty(root.getRuleSets())
                        .flatMapIterable(l -> l)
                        .flatMap(RuleSetLoader::load)
                        .collectMap(RuleSetLoader.RuleSetData::getName)
                        .flatMap(ruleSets -> Mono.justOrEmpty(root.getRule())
                                .flatMapIterable(l -> l)
                                .flatMap(s -> s.startsWith("RULE-SET,") ?
                                        Optional.ofNullable(ruleSets.get(s.substring(9)))
                                                .map(d -> Flux.fromIterable(d.getRule()))
                                                .orElse(Flux.error(new RuleSetException("RuleSet " + s.substring(9) + " not found")))
                                        : Flux.just(s))
                                .switchIfEmpty(Mono.error(new PreprocessorException("Empty rule")))
                                .collectList()))
                .doOnNext(t -> t.getT1().put("Rule", t.getT2()))
                .map(Tuple2::getT1)
                .map(RootUtils::dump);
    }
}
