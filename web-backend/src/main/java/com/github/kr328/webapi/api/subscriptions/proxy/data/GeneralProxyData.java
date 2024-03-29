package com.github.kr328.webapi.api.subscriptions.proxy.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GeneralProxyData extends ProxyData {
    public static final int PROXY_TYPE_NONE = 0;
    public static final int PROXY_TYPE_SHADOWSOCKS = 1;
    private String name;
    private int id;
    private int proxyType;
}
