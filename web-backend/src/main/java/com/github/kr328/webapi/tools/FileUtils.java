package com.github.kr328.webapi.tools;

import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;

public class FileUtils {
    public static Mono<String> readLines(Path path) {
        try {
            return Mono.fromFuture(new FileReaderHandler(path));
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    private static class FileReaderHandler extends CompletableFuture<String> implements CompletionHandler<Integer, AsynchronousFileChannel> {
        private int position = 0;
        private ByteBuffer buffer = ByteBuffer.allocate(256);
        private StringBuilder data = new StringBuilder();

        private FileReaderHandler(Path path) throws IOException {
            completed(0, AsynchronousFileChannel.open(path, StandardOpenOption.READ));
        }

        @Override
        public void completed(Integer result, AsynchronousFileChannel asynchronousFileChannel) {
            if ( result >= 0 ) {
                data.append(new String(buffer.array(), 0, result));
                buffer.clear();
                position += result;
                asynchronousFileChannel.read(buffer, position ,asynchronousFileChannel, this);
            }
            else {
                try {asynchronousFileChannel.close();} catch (IOException ignored) {}

                complete(data.toString());
            }
        }

        @Override
        public void failed(Throwable throwable, AsynchronousFileChannel asynchronousFileChannel) {
            completeExceptionally(throwable);

            try {asynchronousFileChannel.close();} catch (IOException ignored) {}
        }
    }
}
