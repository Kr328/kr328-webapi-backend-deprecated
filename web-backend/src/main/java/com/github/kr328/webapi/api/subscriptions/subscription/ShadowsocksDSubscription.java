package com.github.kr328.webapi.api.subscriptions.subscription;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.kr328.webapi.api.subscriptions.proxy.Proxy;
import com.github.kr328.webapi.api.subscriptions.proxy.data.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Optional;

public class ShadowsocksDSubscription extends BaseSubscription {
    private static final SimpleDateFormat DATE_FORMAT_SSD = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static float castByteToGibibyte(long b) {
        if (b == -1) return -1.0f;
        return (float) b / 1024f / 1024f / 1024f;
    }

    @Override
    public String buildForResponse(HttpHeaders httpHeaders, Proxy[] proxies) {
        HashMap<Integer, ArrayList<Proxy>> sorted = sortProxies(proxies);
        StringBuilder result = new StringBuilder();

        for (ArrayList<Proxy> ps : sorted.values())
            result.append(buildSingleProvider(ps)).append("\n");

        httpHeaders.remove(HttpHeaders.CONTENT_DISPOSITION);
        httpHeaders.setContentType(MediaType.TEXT_PLAIN);

        return result.toString();
    }

    private HashMap<Integer, ArrayList<Proxy>> sortProxies(Proxy[] proxies) {
        HashMap<Integer, ArrayList<Proxy>> result = new HashMap<>();

        for (Proxy p : proxies)
            result.computeIfAbsent(p.get(ProxyData.PROVIDER).hashCode(), k -> new ArrayList<>()).add(p);

        return result;
    }

    private String buildSingleProvider(ArrayList<Proxy> proxies) {
        JSONObject root = new JSONObject();
        ProviderProxyData provider = null;
        ShadowsocksProxyData shadowsocks = null;
        ShadowsocksPluginProxyData rootPlugin = null; //Workaround for 0.0.3

        JSONArray servers = new JSONArray();

        for (Proxy p : proxies) {
            GeneralProxyData general = p.require(ProxyData.GENERAL);

            if (general.getProxyType() != GeneralProxyData.PROXY_TYPE_SHADOWSOCKS)
                continue;

            provider = p.require(ProxyData.PROVIDER);
            shadowsocks = p.require(ProxyData.SHADOWSOCKS);

            JSONObject server = new JSONObject();

            server.put("server", shadowsocks.getHost());
            server.put("port", shadowsocks.getPort());
            server.put("encryption", shadowsocks.getMethod());
            server.put("password", shadowsocks.getPassword());
            server.put("id", general.getId());
            server.put("remarks", general.getName());

            ShadowsocksPluginProxyData shadowsocksPlugin = p.get(ProxyData.SHADOWSOCKS_PLUGIN);
            if (shadowsocksPlugin != null) {
                server.put("plugin", shadowsocksPlugin.getPlugin());
                server.put("plugin_options", shadowsocksPlugin.getPluginOptions());

                rootPlugin = shadowsocksPlugin;
            }

            servers.add(server);
        }

        if (provider == null)
            return "";

        root.put("airport", provider.getName());
        root.put("traffic_used", castByteToGibibyte(provider.getTrafficUsed()));
        root.put("traffic_total", castByteToGibibyte(provider.getTrafficTotal()));
        Optional.ofNullable(provider.getExpires()).ifPresent(date -> root.put("expiry", DATE_FORMAT_SSD.format(date)));

        root.put("port", shadowsocks.getPort());
        root.put("encryption", shadowsocks.getMethod());
        root.put("password", shadowsocks.getPassword());

        if (rootPlugin != null) {
            root.put("plugin", rootPlugin.getPlugin());
            root.put("plugin_options", rootPlugin.getPluginOptions());
        }

        root.put("servers", servers);

        return "ssd://" + Base64.getUrlEncoder().encodeToString(root.toJSONString().getBytes());
    }
}
