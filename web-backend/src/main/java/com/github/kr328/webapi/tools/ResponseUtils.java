package com.github.kr328.webapi.tools;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Optional;

public class ResponseUtils {
    public static Mono<ServerResponse> yamlError(int httpCode, String code, Throwable throwable) {
        return Mono.defer(() -> ServerResponse.status(httpCode).contentType(MediaType.TEXT_PLAIN).body(
                Mono.just(new Yaml(new Representer() {{
                    addClassTag(ErrorModel.class, Tag.MAP);
                }})
                        .dumpAsMap(Collections.singletonMap("error", new ErrorModel(code,
                                Optional.ofNullable(throwable).map(Throwable::getMessage).or(() ->
                                        Optional.ofNullable(throwable).map(t -> t.getClass().getName())).orElse("error_unknown"))))), String.class));
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
