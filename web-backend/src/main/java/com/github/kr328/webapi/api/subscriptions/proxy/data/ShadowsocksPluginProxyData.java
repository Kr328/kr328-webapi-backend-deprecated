package com.github.kr328.webapi.api.subscriptions.proxy.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ShadowsocksPluginProxyData extends ProxyData {
    private String plugin;
    private String pluginOptions;
}
