package com.github.kr328.webapi.api.clash.utils;

import java.lang.reflect.Field;
import java.util.*;

public class ExtractLinkedHashMap implements Map<String, Object> {
    private LinkedHashMap<String, Field> cachedField;
    private LinkedHashMap<String, Object> parent = new LinkedHashMap<>();

    public ExtractLinkedHashMap() {
        cachedField = sCache.get(getClass());
        if ( cachedField != null )
            return;

        Class<?> child = getClass();
        LinkedHashMap<String, Field> fields = new LinkedHashMap<>();
        while ( child != ExtractLinkedHashMap.class && child != null ) {
            for ( Field field : child.getDeclaredFields() ) {
                KeyName keyName = field.getAnnotation(KeyName.class);
                field.setAccessible(true);
                fields.putIfAbsent(keyName == null ? field.getName() : keyName.value(), field);
            }
            child = child.getSuperclass();
        }
        sCache.put(getClass(), fields);

        cachedField = fields;
    }

    @Override
    public int size() {
        return parent.size() + cachedField.size();
    }

    @Override
    public boolean isEmpty() {
        return parent.isEmpty() && cachedField.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return parent.containsKey(o) && cachedField.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return parent.containsValue(o);
    }

    @Override
    public Object get(Object key) {
        try {
            if (key instanceof String) {
                Field field = cachedField.get(key);
                if ( field != null )
                    return field.get(this);
            }
        }
        catch (IllegalAccessException ignored) {}

        return parent.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        try {
            if (key != null) {
                Field field = cachedField.get(key);
                Object result;
                if ( field != null ) {
                    result = field.get(this);
                    field.set(this, value);
                    return result;
                }
            }
        }
        catch (IllegalAccessException ignored) {}

        return parent.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        try {
            if (key instanceof String) {
                Field field = cachedField.get(key);
                Object result = null;
                if ( field != null ) {
                    result = field.get(this);
                    field.set(this, null);
                    return result;
                }
            }
        }
        catch (IllegalAccessException ignored) {}

        return parent.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> map) {
        for ( Entry<? extends String, ?> entry : map.entrySet() )
            put(entry.getKey() ,entry.getValue());
    }

    @Override
    public void clear() {
        for ( Field field : cachedField.values() ) {
            try {
                field.set(this, null);
            } catch (IllegalAccessException ignored) {}
        }

        parent.clear();
    }

    @Override
    public Set<String> keySet() {
        LinkedHashSet<String> result = new LinkedHashSet<>();

        result.addAll(cachedField.keySet());
        result.addAll(parent.keySet());

        return result;
    }

    @Override
    public Collection<Object> values() {
        ArrayList<Object> result = new ArrayList<>();

        for ( Field f : cachedField.values() ) {
            try {
                result.add(f.get(this));
            } catch (IllegalAccessException e) {
                result.add(null);
            }
        }

        result.addAll(parent.values());

        return result;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        LinkedHashSet<Entry<String, Object>> result = new LinkedHashSet<>();

        for ( Entry<String, Field> entry : cachedField.entrySet() ) {
            try {
                result.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().get(this)));
            } catch (IllegalAccessException ignored) {}
        }

        result.addAll(parent.entrySet());

        return result;
    }

    @Override
    public String toString() {
        return parent.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!( obj instanceof ExtractLinkedHashMap))
            return false;

        return parent.equals(obj);
    }

    @Override
    public int hashCode() {
        return parent.hashCode();
    }

    private static Hashtable<Class<?>, LinkedHashMap<String, Field>> sCache = new Hashtable<>();
}
