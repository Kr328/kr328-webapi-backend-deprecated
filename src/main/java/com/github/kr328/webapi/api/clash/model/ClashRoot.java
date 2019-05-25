package com.github.kr328.webapi.api.clash.model;

import com.github.kr328.webapi.api.clash.utils.ExtractLinkedHashMap;
import com.github.kr328.webapi.api.clash.utils.KeyName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class ClashRoot extends ExtractLinkedHashMap {
    @KeyName("Proxy")
    private List<Proxy> proxy;
}
