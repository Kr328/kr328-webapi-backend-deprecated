package com.github.kr328.webapi.api.subscriptions.proxy;

import com.github.kr328.webapi.api.subscriptions.proxy.data.ProxyData;

import java.util.HashMap;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class Proxy {
    private HashMap<String, ProxyData> values = new HashMap<>();

    public Proxy() {
    }

    @SuppressWarnings("unchecked")
    public <T extends ProxyData> T get(ProxyData.ProxyDataKey<T> key) {
        return (T) values.get(key.description);
    }

    public <T extends ProxyData> T require(ProxyData.ProxyDataKey<T> key) {
        T result = this.get(key);
        if (result == null)
            throw new IllegalStateException("Key " + key.description + " not found.");
        return result;
    }

    public <T extends ProxyData> T put(ProxyData.ProxyDataKey<T> key, T value) {
        if (value == null)
            return remove(key);

        values.put(key.description, value);
        return value;
    }

    @SuppressWarnings("unchecked")
    public <T extends ProxyData> T remove(ProxyData.ProxyDataKey<T> key) {
        return (T) values.remove(key.description);
    }
}
