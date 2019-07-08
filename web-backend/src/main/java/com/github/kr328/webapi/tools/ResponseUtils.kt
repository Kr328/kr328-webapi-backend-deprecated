package com.github.kr328.webapi.tools

import lombok.AllArgsConstructor
import lombok.Data
import lombok.NonNull
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerResponse
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.representer.Representer
import reactor.core.publisher.Mono
import reactor.util.Logger
import reactor.util.Loggers

import java.util.Collections
import java.util.Optional

object ResponseUtils {
    fun log(clazz: Class<*>, throwable: Throwable): Throwable? {
        val logger = Loggers.getLogger(clazz)

        Optional.ofNullable(throwable)
                .ifPresentOrElse({ t -> logger.error("Unexpected", t) },
                        { logger.error("Unexpected empty throwable") })

        return throwable
    }

    private fun castString(@NonNull throwable: Throwable): String {
        return throwable.javaClass.simpleName + ": " + Optional.ofNullable(throwable.message).orElse("")
    }
}
