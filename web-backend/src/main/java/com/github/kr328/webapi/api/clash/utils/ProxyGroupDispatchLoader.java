package com.github.kr328.webapi.api.clash.utils;

import com.github.kr328.webapi.api.clash.model.ProxyGroupDispatch;
import lombok.Data;

import java.util.*;
import java.util.regex.Pattern;

public class ProxyGroupDispatchLoader {
    private static final Pattern PATTERN_MATCHES_ANY = Pattern.compile(".*");
    private static final Pattern PATTERN_MATCHES_NONE = Pattern.compile("");

    public static List<ProxyGroupData> load(Collection<ProxyGroupDispatch> dispatches) {
        ArrayList<ProxyGroupData> result = new ArrayList<>();

        for (ProxyGroupDispatch dispatch : dispatches) {
            ProxyGroupData proxyGroupData = new ProxyGroupData();

            proxyGroupData.dispatch = dispatch;
            proxyGroupData.whitePattern = Optional.ofNullable(dispatch.getProxiesFilters())
                    .map(ProxyGroupDispatch.ProxiesFilters::getWhiteRegex)
                    .map(Pattern::compile).orElse(PATTERN_MATCHES_NONE);
            proxyGroupData.blackPattern = Optional.ofNullable(dispatch.getProxiesFilters())
                    .map(ProxyGroupDispatch.ProxiesFilters::getBlackRegex)
                    .map(Pattern::compile).orElse(PATTERN_MATCHES_ANY);
            proxyGroupData.proxies.addAll(Optional.ofNullable(dispatch.getFlatProxies()).orElse(Collections.emptyList()));

            result.add(proxyGroupData);
        }

        return result;
    }

    @Data
    public static class ProxyGroupData {
        private ProxyGroupDispatch dispatch;
        private Pattern whitePattern = PATTERN_MATCHES_NONE;
        private Pattern blackPattern = PATTERN_MATCHES_ANY;
        private ArrayList<String> proxies = new ArrayList<>();

        public LinkedHashMap<String, Object> toMap() {
            LinkedHashMap<String, Object> r = new LinkedHashMap<>();

            Optional.ofNullable(dispatch.getName()).ifPresent(n -> r.put("name", n));
            Optional.ofNullable(dispatch.getType()).ifPresent(t -> r.put("type", t));
            Optional.ofNullable(dispatch.getUrl()).ifPresent(u -> r.put("url", u));
            if (dispatch.getInterval() != -1) r.put("interval", dispatch.getInterval());
            r.put("proxies", proxies);

            return r;
        }
    }
}
