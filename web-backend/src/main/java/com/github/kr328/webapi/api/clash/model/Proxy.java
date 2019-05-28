package com.github.kr328.webapi.api.clash.model;

import java.util.LinkedHashMap;

public class Proxy extends LinkedHashMap<String, Object> {
    public String getName() {
        return String.valueOf(get("name"));
    }
}
