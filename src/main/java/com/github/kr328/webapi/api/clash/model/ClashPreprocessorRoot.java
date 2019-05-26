package com.github.kr328.webapi.api.clash.model;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class ClashPreprocessorRoot {
    private Preprocessor preprocessor;
    private LinkedHashMap<String, Object> clashGeneral;
    private List<ProxySource> proxySources;
    private List<ProxyGroupDispatch> proxyGroupDispatch;
    private List<RuleSet> ruleSets;
    private List<String> rule;
}
