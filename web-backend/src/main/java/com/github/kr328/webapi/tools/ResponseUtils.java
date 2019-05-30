package com.github.kr328.webapi.tools;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import reactor.core.publisher.Mono;

public class ResponseUtils {
    public static Mono<ServerResponse> yamlError(int httpCode, int code, String message) {
        return ServerResponse.status(httpCode)
                .body(Mono.just(new Yaml(new SafeConstructor()).dumpAsMap(new ErrorModel(code, message))), String.class);
    }

    @Data
    @AllArgsConstructor
    private static class ErrorModel {
        private int code;
        private String message;
    }
}
