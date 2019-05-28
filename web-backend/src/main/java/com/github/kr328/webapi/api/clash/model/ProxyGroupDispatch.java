package com.github.kr328.webapi.api.clash.model;

import lombok.Data;

import java.util.List;

@Data
public class ProxyGroupDispatch {
    private String name;
    private String type;
    private String url;
    private int interval = -1;

    private ProxiesFilters proxiesFilters;
    private List<String> flatProxies;

    @Data
    public static class ProxiesFilters {
        private String whiteRegex;
        private String blackRegex;
    }
}
