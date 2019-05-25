package com.github.kr328.webapi.api.clash.model;

import com.github.kr328.webapi.api.clash.utils.ExtractLinkedHashMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class Proxy extends ExtractLinkedHashMap {
    private String name;
}
