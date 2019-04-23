package com.github.kr328.webapi.core.subscriptions.subscription;

import com.github.kr328.webapi.core.subscriptions.proxy.Proxy;
import com.github.kr328.webapi.core.subscriptions.proxy.data.*;
import org.springframework.http.HttpHeaders;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SurgeSubscription extends BaseSubscription {
    @Override
    public Proxy[] parseFromRequest(HttpHeaders httpHeaders, String body) throws ParseException {
        String[] lines = body.split("[\\n\\t]");

        if (!detectSurge(lines))
            throw new ParseException("Unsupported");

        List<String> trafficInfoHeader = Optional.ofNullable(httpHeaders.getValuesAsList("subscription-userinfo"))
                .orElse(Collections.emptyList());

        String providerName = Optional.ofNullable(httpHeaders.getContentDisposition().getFilename()).orElse("Unlabeled")
                .replaceAll("\\.(txt|conf)$", "");
        long trafficUsed = parseUsedTraffic(trafficInfoHeader);
        long trafficTotal = parseTotalTraffic(trafficInfoHeader);

        boolean in_proxy = false;
        ArrayList<Proxy> result = new ArrayList<>();

        for ( String line : lines ) {
            line = line.trim();

            if ( line.isEmpty() )
                continue;

            if ( line.startsWith("//") || line.startsWith("#") )
                continue;

            if ( SET_SECTIONS_LINE.contains(line) ) {
                in_proxy = "[Proxy]".equals(line);
                continue;
            }

            if ( !in_proxy )
                continue;

            Matcher matcher = PATTERN_SHADOWSOCKS_LINE.matcher(line);
            if (!matcher.matches())
                continue;

            Proxy proxy = new Proxy();

            parseProxy(matcher, proxy);

            ProviderProxyData providerProxyData =
                    new ProviderProxyData(providerName, trafficUsed, trafficTotal, new Date(0));
            proxy.put(ProxyData.PROVIDER, providerProxyData);

            result.add(proxy);
        }

        return result.toArray(new Proxy[0]);
    }

    private boolean detectSurge(String[] lines) {
        if ( lines.length == 0 )
            return true; //For HEAD and OPTIONS Method

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty())
                continue;

            if ( SET_SECTIONS_LINE.contains(line) )
                return true;
        }

        return false;
    }

    private void parseProxy(Matcher matcher, Proxy proxy) {
        String name = matcher.group(1);
        String host = matcher.group(2);
        String port = matcher.group(3);
        String method = matcher.group(4);
        String password = matcher.group(5);
        String[] extras = matcher.group(6).split(",");

        GeneralProxyData general = new GeneralProxyData(name, name.hashCode(), GeneralProxyData.PROXY_TYPE_SHADOWSOCKS);
        ShadowsocksProxyData shadowsocks = new ShadowsocksProxyData(host, Integer.parseInt(port), method, password);
        ShadowsocksPluginProxyData shadowsocksPlugin = parsePlugin(extras);

        proxy.put(ProxyData.GENERAL, general);
        proxy.put(ProxyData.SHADOWSOCKS, shadowsocks);
        proxy.put(ProxyData.SHADOWSOCKS_PLUGIN, shadowsocksPlugin);
    }

    private ShadowsocksPluginProxyData parsePlugin(String[] extras) {
        TreeMap<String, String> extrasMap = new TreeMap<>();

        for (String s : extras) {
            String[] kv = s.split("=", 2);

            if (kv.length != 2)
                continue;

            extrasMap.put(kv[0], kv[1]);
        }

        if (extrasMap.containsKey("obfs")) {
            return new ShadowsocksPluginProxyData("obfs-local",
                    "obfs=" + extrasMap.get("obfs") + ";obfs-host=" + extrasMap.getOrDefault("obfs-host", ""));
        }

        return null;
    }

    private long parseUsedTraffic(List<String> headerValues) {
        AtomicLong result = new AtomicLong(-1);

        headerValues.stream()
                .flatMap(s -> Stream.of(s.split("[;\\s]")))
                .forEach(s -> {
                    String[] kv = s.split("=");

                    if (kv.length != 2)
                        return;

                    if ("upload".equals(kv[0]))
                        result.updateAndGet(v -> v + Long.parseLong(kv[1]));
                    else if ("download".equals(kv[0]))
                        result.updateAndGet(v -> v + Long.parseLong(kv[1]));
                });

        return result.get() == -1 ? -1 * 1024 * 1024 * 1024 : result.get() + 1;
    }

    private long parseTotalTraffic(List<String> headerValues) {
        AtomicLong result = new AtomicLong(-1);

        headerValues.stream()
                .flatMap(s -> Stream.of(s.split("[;\\s]")))
                .forEach(s -> {
                    String[] kv = s.split("=");

                    if (kv.length != 2)
                        return;

                    if ("total".equals(kv[0]))
                        result.set(Long.parseLong(kv[1]));
                });

        return result.get() == -1 ? -1 * 1024 * 1024 * 1024 : result.get() + 1;
    }

    private static final Pattern PATTERN_SHADOWSOCKS_LINE = Pattern.compile("(.*?)\\s?=\\s?custom,([0-9a-zA-Z.-]+),(\\d+),([0-9a-zA-Z.-]+),(.*?),.*?SSEncrypt\\.module,(.*)$");
    private static final TreeSet<String> SET_SECTIONS_LINE = new TreeSet<>(Arrays.asList("[General]" ,"[Proxy]" ,"[Proxy Group]" ,"[Rule]" ,"[Header Rewrite]" ,"[URL Rewrite]"));
}
