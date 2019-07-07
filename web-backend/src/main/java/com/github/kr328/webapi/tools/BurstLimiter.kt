package com.github.kr328.webapi.tools

import kotlin.concurrent.thread
import kotlin.concurrent.timer

class BurstLimiter {
    private val table: MutableMap<String, Record> = mutableMapOf()

    init {
        timer(period = 60 * 1000) { cleanup() }
    }

    @Synchronized
    fun note(id: String): Boolean {
        val record = table.computeIfAbsent(id) { Record(0, System.currentTimeMillis()) }

        if (System.currentTimeMillis() - record.last > 1000 * 60)
            record.count = 0

        record.last = System.currentTimeMillis()

        return record.count++ > 20
    }

    @Synchronized
    fun cleanup() {
        table.entries.removeIf { System.currentTimeMillis() - it.value.last > 60 * 1000 }
    }

    private data class Record(var count: Long, var last: Long)
}
