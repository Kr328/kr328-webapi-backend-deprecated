package com.github.kr328.webapi.api.clash.model;

import lombok.Data;

import java.util.List;

@Data
public class ClashRoot {
    private List<Proxy> proxy;
    private List<String> rule;
}
