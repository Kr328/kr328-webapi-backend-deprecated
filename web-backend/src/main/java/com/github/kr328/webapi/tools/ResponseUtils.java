package com.github.kr328.webapi.tools;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Optional;

public class ResponseUtils {
    public static Mono<ServerResponse> yamlError(int httpCode, String code, Throwable throwable) {
        return Mono.defer(() -> ServerResponse.status(httpCode).body(
                Mono.just(new Yaml(new SafeConstructor())
                        .dumpAsMap(Collections.singletonMap("error", new ErrorModel(code,
                                Optional.ofNullable(throwable).map(Throwable::getMessage).orElse("unknown"))))), String.class));
    }

    public static Mono<ServerResponse> yamlError(int httpCode, String code) {
        return yamlError(httpCode, code, null);
    }

    @Data
    @AllArgsConstructor
    private static class ErrorModel {
        private String code;
        private String message;
    }
}
