package com.github.kr328.webapi.web

import com.charleskorn.kaml.Yaml
import com.github.kr328.webapi.Global
import com.github.kr328.webapi.api.clash.ClashPreprocessor
import com.github.kr328.webapi.api.clash.exceptions.DispatcherException
import com.github.kr328.webapi.api.clash.exceptions.PreprocessorException
import com.github.kr328.webapi.api.clash.exceptions.ProxySourceException
import com.github.kr328.webapi.api.clash.exceptions.RuleSetException
import com.github.kr328.webapi.tools.BurstLimiter
import com.github.kr328.webapi.tools.fileLines
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClientException
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.yaml.snakeyaml.error.YAMLException
import reactor.core.publisher.Mono
import reactor.util.Logger
import reactor.util.Loggers
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Paths
import java.util.regex.Pattern

class ClashPreprocessor {
    companion object {
        private val PATTERN_ID = Pattern.compile("\\d+")
        private val PATTERN_SECRET = Pattern.compile("[a-f0-9]{32}")
        private val LOGGER: Logger = Loggers.getLogger(ClashPreprocessor::class.simpleName!!)
    }

    private val limiter = BurstLimiter()

    fun process(request: ServerRequest): Mono<ServerResponse> {
        val userId = request.pathVariable("userId")
        val secret = request.pathVariable("secret")

        if (!PATTERN_ID.matcher(userId).matches() || !PATTERN_SECRET.matcher(secret).matches())
            return error(HttpStatus.BAD_REQUEST, "error_invalid_id_or_secret")

        if (limiter.note(userId))
            return error(HttpStatus.FORBIDDEN, "error_burst")

        return fileLines(Paths.get(Global.WEBAPI_DATA_PATH, userId, "metadata.json"))
                    .map { s -> Json(JsonConfiguration.Stable).parse(MetadataModel.serializer(), s) }
                    .filter { metadataModel -> metadataModel.secret == secret }
                    .switchIfEmpty(Mono.error(FileNotFoundException()))
                    .zipWhen {
                        fileLines(Paths.get(Global.WEBAPI_DATA_PATH, userId, "data.yml"))
                                .flatMap { ClashPreprocessor.process(it) }
                    }
                    .flatMap { t ->
                        ServerResponse.ok()
                                .contentType(MediaType.TEXT_PLAIN)
                                .headers { h -> h.setContentDispositionFormData("attachment", t.t1.username + ".yaml") }
                                .body(Mono.just(t.t2), String::class.java)
                    }
                    .onErrorResume { throwable ->
                        when ( throwable ) {
                            is IOException -> error(HttpStatus.NOT_FOUND, "error_config_not_found", throwable)
                            is YAMLException -> error(HttpStatus.FORBIDDEN, "error_invalid_config", throwable)
                            is DispatcherException -> error(HttpStatus.FORBIDDEN, "error_invalid_dispatcher", throwable)
                            is PreprocessorException -> error(HttpStatus.FORBIDDEN, "error_invalid_preprocessor", throwable)
                            is ProxySourceException -> error(HttpStatus.FORBIDDEN, "error_invalid_proxy_source", throwable)
                            is RuleSetException -> error(HttpStatus.FORBIDDEN, "error_invalid_rule_set", throwable)
                            is WebClientException -> error(HttpStatus.FORBIDDEN, "error_download_from_upstream_failure", throwable)
                            else -> {
                                LOGGER.error("Unknown", throwable)
                                error(HttpStatus.GONE, "unknown", throwable)
                            }
                        }
                    }
    }

    private fun error(status: HttpStatus, code: String, throwable: Throwable? = null): Mono<ServerResponse> {
        return ServerResponse.status(status).contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just(Yaml.default.stringify(Error.serializer(), Error(Error.ErrorInfo(code, throwable?.toString() ?: "unknown")))),
                        String::class.java)
    }

    @Serializable
    private data class MetadataModel(var username: String = "", var userId: Long = 0, var messageId: Long = 0, var secret: String = "")

    @Serializable
    private data class Error(val error: ErrorInfo) {
        @Serializable
        data class ErrorInfo(val code: String, val message: String)
    }
}
