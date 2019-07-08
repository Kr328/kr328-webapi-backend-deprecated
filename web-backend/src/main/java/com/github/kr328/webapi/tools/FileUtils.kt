package com.github.kr328.webapi.tools

import reactor.core.publisher.Mono

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.CompletionHandler
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.CompletableFuture

fun fileLines(path: Path): Mono<String> {
    return try {
        Mono.fromFuture(FileReaderHandler(path))
    } catch (e: IOException) {
        Mono.error(e)
    }
}

private class FileReaderHandler(path: Path) : CompletableFuture<String>(), CompletionHandler<Int, AsynchronousFileChannel> {
    private var position = 0
    private val buffer = ByteBuffer.allocate(256)
    private val data = StringBuilder()

    init {
        completed(0, AsynchronousFileChannel.open(path, StandardOpenOption.READ))
    }

    override fun completed(result: Int, asynchronousFileChannel: AsynchronousFileChannel) {
        if (result >= 0) {
            data.append(String(buffer.array(), 0, result))
            buffer.clear()
            position += result
            asynchronousFileChannel.read(buffer, position.toLong(), asynchronousFileChannel, this)
        } else {
            try {
                asynchronousFileChannel.close()
            } catch (ignored: IOException) {
            }

            complete(data.toString())
        }
    }

    override fun failed(throwable: Throwable, asynchronousFileChannel: AsynchronousFileChannel) {
        completeExceptionally(throwable)

        try { asynchronousFileChannel.close() } catch (ignored: IOException) {}
    }
}

