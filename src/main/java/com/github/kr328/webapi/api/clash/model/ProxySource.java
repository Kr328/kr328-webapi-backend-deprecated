package com.github.kr328.webapi.api.clash.model;

import lombok.Data;

@Data
public class ProxySource {
    private String type;
    private String url;
    private Proxy data;
}
