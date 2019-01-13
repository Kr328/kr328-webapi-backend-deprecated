package com.github.kr328.webapi.utils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

public class Surge {
    public static Flux<String> splitProxyLines(String[] lines) {
        boolean inProxy = false;
        ArrayList<String> proxyLines = new ArrayList<>();

        for ( String line : lines ) {
            line = line.trim();

            if ( line.isEmpty() )
                continue;

            if ( line.startsWith("//") || line.startsWith("#") )
                continue;

            if ( line.startsWith("[") ) {
                inProxy = line.equals("[Proxy]");
                continue;
            }

            if ( !inProxy )
                continue;

            proxyLines.add(line);
        }

        return Flux.fromIterable(proxyLines);
    }

    public static boolean detectSurge(String[] lines) {
        for ( String line : lines ) {
            line = line.trim();

            if ( line.isEmpty() )
                continue;

            if ( line.startsWith("//") )
                continue;

            if ( line.startsWith("#") ) {
                if ( line.startsWith("#!MANAGED-CONFIG") )
                    return true;
                continue;
            }

            return false;
        }

        return false;
    }

    public static Mono<Proxy> parseProxy(String data ,Proxy.Provider provider) {
        String[] nameAndProxyData = data.split("=" ,2);
        if ( nameAndProxyData.length != 2 ) return Mono.empty();
        String[] proxyData = nameAndProxyData[1].trim().split("(\\s|,)+");
        if ( proxyData.length < 6 ) return Mono.empty();
        if ( !proxyData[0].equals("custom") ) return Mono.empty();

        Proxy proxy = new Proxy();
        proxy.provider = provider;
        proxy.id = Integer.toString(nameAndProxyData[0].trim().hashCode());
        proxy.remark = nameAndProxyData[0].trim();
        proxy.host = proxyData[1].trim();
        proxy.port = proxyData[2].trim();
        proxy.method = proxyData[3].trim();
        proxy.password = proxyData[4].trim();

        Proxy.Plugin plugin = null;
        StringBuilder argumentsBuilder = new StringBuilder();

        for ( int i = 6 ; i < proxyData.length ; i++ ) {
            if ( proxyData[i].startsWith("obfs") ) {
                if (plugin == null)
                    plugin = new Proxy.SimpleObfsPlugin();
                argumentsBuilder.append(proxyData[i].trim()).append(";");
            }
        }

        if ( plugin != null ) {
            proxy.plugin = plugin;
            ((Proxy.SimpleObfsPlugin)proxy.plugin).arguments = argumentsBuilder
                    .toString().replaceAll(";$" ,"");
        }

        return Mono.just(proxy);
    }
}
