package com.github.kr328.webapi.tools;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Collections;
import java.util.Optional;

public class ResponseUtils {
    public static Mono<ServerResponse> error(int httpCode, String code, Throwable throwable) {
        return Mono.defer(() -> ServerResponse.status(httpCode).contentType(MediaType.TEXT_PLAIN).body(
                Mono.just(new Yaml(new Representer() {{
                    addClassTag(ErrorModel.class, Tag.MAP);
                }}).dumpAsMap(Collections.singletonMap("error", new ErrorModel(code,
                                Optional.ofNullable(throwable).map(ResponseUtils::castString).orElse("error_unknown"))))), String.class));
    }

    public static Mono<ServerResponse> error(int httpCode, String code) {
        return error(httpCode, code, null);
    }

    public static Throwable log(Class<?> clazz, Throwable throwable) {
        Logger logger = Loggers.getLogger(clazz);

        Optional.ofNullable(throwable)
                .ifPresentOrElse(t -> logger.error("Unexpected", t),
                        () -> logger.error("Unexpected empty throwable"));

        return throwable;
    }

    private static String castString(@NonNull Throwable throwable) {
        return throwable.getClass().getSimpleName() + ": " + Optional.ofNullable(throwable.getMessage()).orElse("");
    }

    @Data
    @AllArgsConstructor
    private static class ErrorModel {
        private String code;
        private String message;
    }
}
