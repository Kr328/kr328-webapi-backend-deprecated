package com.github.kr328.webapi.api.clash.model;

import lombok.Data;

import java.util.Map;

@Data
public class ProxySource {
    private String type;
    private String url;
    private Map<String, Object> data;
}
