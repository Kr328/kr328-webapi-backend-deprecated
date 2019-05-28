package com.github.kr328.webapi.core.subscriptions.proxy.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ShadowsocksProxyData extends ProxyData {
    private String host;
    private int port;
    private String method;
    private String password;
}
