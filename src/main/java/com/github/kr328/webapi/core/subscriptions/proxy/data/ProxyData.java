package com.github.kr328.webapi.core.subscriptions.proxy.data;

public abstract class ProxyData {
    public static final ProxyDataKey<GeneralProxyData> GENERAL = new ProxyDataKey<>("general");
    public static final ProxyDataKey<ProviderProxyData> PROVIDER = new ProxyDataKey<>("provider");
    public static final ProxyDataKey<ShadowsocksProxyData> SHADOWSOCKS = new ProxyDataKey<>("shadowsocks");
    public static final ProxyDataKey<ShadowsocksPluginProxyData> SHADOWSOCKS_PLUGIN = new ProxyDataKey<>("shadowsocks_plugin");

    public static class ProxyDataKey<T extends ProxyData> {
        public ProxyDataKey(String description) {this.description = description;}
        public String description;
    }
}
