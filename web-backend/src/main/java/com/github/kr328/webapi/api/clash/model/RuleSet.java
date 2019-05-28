package com.github.kr328.webapi.api.clash.model;

import lombok.Data;

import java.util.List;

@Data
public class RuleSet {
    private String name;
    private String type;
    private String url;
    private List<String> targetMap;
}
