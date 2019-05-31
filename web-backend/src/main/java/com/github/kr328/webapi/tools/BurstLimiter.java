package com.github.kr328.webapi.tools;

import lombok.AllArgsConstructor;

import java.util.HashMap;

public class BurstLimiter {
    private HashMap<String, Record> table;

    public BurstLimiter() {
        table = new HashMap<>();
    }

    public synchronized boolean note(String id) {
        Record record = table.computeIfAbsent(id, s -> new Record(0, System.currentTimeMillis()));

        if (System.currentTimeMillis() - record.last > 1000 * 60)
            record.count = 0;

        record.last = System.currentTimeMillis();

        return record.count++ > 20;
    }

    @AllArgsConstructor
    private static class Record {
        private int count;
        private long last;
    }
}
