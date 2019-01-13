package com.github.kr328.webapi.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class ShadowSocksD {
    public static JsonCollector toJsonCollector() {
        return new JsonCollector();
    }

    private static String pluginString(Proxy proxy) {
        if ( proxy.plugin instanceof Proxy.SimpleObfsPlugin )
            return "\"plugin\":\"simple-obfs\",\"plugin_options\":\"" + proxy.plugin.getArguments() + "\",";
        return "";
    }

    private static class JsonCollector implements Collector<Proxy ,JSONObject ,JSONObject> {
        private static Proxy lastObject = null;

        @Override
        public Supplier<JSONObject> supplier() {
            return JSONObject::new;
        }

        @Override
        public BiConsumer<JSONObject, Proxy> accumulator() {
            return ((jsonObject, proxy) -> {
                JSONArray array = jsonObject.getJSONArray("servers");
                if ( array == null ) {
                    array = new JSONArray();
                    jsonObject.put("servers" ,array);
                }

                lastObject = proxy;
                array.add(proxyToJson(proxy));
            });
        }

        @Override
        public BinaryOperator<JSONObject> combiner() {
            return (j1 ,j2) -> {
                JSONArray a1 = j1.getJSONArray("servers");
                JSONArray a2 = j2.getJSONArray("servers");

                if ( a1 == null )
                    return j2;
                if ( a2 == null )
                    return j1;

                a1.addAll(a2);
                return j1;
            };
        }

        @Override
        public Function<JSONObject, JSONObject> finisher() {
            return (jsonObject -> {
                if ( lastObject == null )
                    return jsonObject;

                jsonObject.put("airport" ,lastObject.provider.name);
                jsonObject.put("port" ,lastObject.port);
                jsonObject.put("encryption" ,lastObject.method);
                jsonObject.put("password" ,lastObject.password);

                if ( lastObject.plugin != null ) {
                    jsonObject.put("plugin" ,lastObject.plugin.getType());
                    jsonObject.put("plugin_options" ,lastObject.plugin.getArguments());
                }

                return jsonObject;
            });
        }

        @Override
        public Set<Characteristics> characteristics() {
            return null;
        }

        private static JSONObject proxyToJson(Proxy proxy) {
            JSONObject result = new JSONObject();

            result.put("remarks" , proxy.remark);
            result.put("server" ,proxy.host);
            result.put("id" ,proxy.id);
            result.put("port" ,proxy.port);
            result.put("encryption" ,proxy.method);
            result.put("password" ,proxy.password);

            if ( proxy.plugin != null ) {
                result.put("plugin" ,proxy.plugin.getType());
                result.put("plugin_options" ,proxy.plugin.getArguments());
            }

            return result;
        }
    }
}
