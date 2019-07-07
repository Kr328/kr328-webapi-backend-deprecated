package com.github.kr328.webapi.web

import com.alibaba.fastjson.JSONObject
import com.github.kr328.webapi.Global
import com.github.kr328.webapi.WebApiApplication
import com.github.kr328.webapi.api.clash.ClashPreprocessor
import com.github.kr328.webapi.api.clash.exceptions.DispatcherException
import com.github.kr328.webapi.api.clash.exceptions.PreprocessorException
import com.github.kr328.webapi.api.clash.exceptions.ProxySourceException
import com.github.kr328.webapi.api.clash.exceptions.RuleSetException
import com.github.kr328.webapi.tools.BurstLimiter
import com.github.kr328.webapi.tools.FileUtils
import com.github.kr328.webapi.tools.ResponseUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientException
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.yaml.snakeyaml.error.YAMLException
import reactor.core.publisher.Mono
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Paths
import java.util.regex.Pattern

class ClashPreprocess {
    private val limiter = BurstLimiter()

    fun process(request: ServerRequest): Mono<ServerResponse> {
        val userId = request.pathVariable("userId")
        val secret = request.pathVariable("secret")

        if (!PATTERN_ID.matcher(userId).matches() || !PATTERN_SECRET.matcher(secret).matches())
            return ResponseUtils.error(400, "error_invalid_id_or_secret")

        return if (limiter.note(userId)) ResponseUtils.error(400, "error_burst") else
            FileUtils.readLines(Paths.get(Global.WEBAPI_DATA_PATH, userId, "metadata.json"))
                .map { s -> JSONObject.parseObject(s, MetadataModel::class.java) }
                .filter { metadataModel -> metadataModel.secret == secret }
                .switchIfEmpty(Mono.error(FileNotFoundException()))
                .zipWhen {
                    FileUtils.readLines(Paths.get(Global.WEBAPI_DATA_PATH, userId, "data.yml"))
                            .flatMap { ClashPreprocessor.process(it) }
                }
                .flatMap { t ->
                    ServerResponse.ok()
                            .contentType(MediaType.TEXT_PLAIN)
                            .headers { h -> h.setContentDispositionFormData("attachment", t.t1.username + ".yml") }
                            .body(Mono.just(t.t2), String::class.java)
                }
                .onErrorResume(IOException::class.java) { th -> ResponseUtils.error(404, "error_config_not_found", th) }
                .onErrorResume(YAMLException::class.java) { th -> ResponseUtils.error(403, "error_invalid_config", th) }
                .onErrorResume(DispatcherException::class.java) { th -> ResponseUtils.error(403, "error_invalid_dispatcher", th) }
                .onErrorResume(PreprocessorException::class.java) { th -> ResponseUtils.error(403, "error_invalid_preprocessor", th) }
                .onErrorResume(ProxySourceException::class.java) { th -> ResponseUtils.error(403, "error_invalid_proxy_source", th) }
                .onErrorResume(RuleSetException::class.java) { th -> ResponseUtils.error(403, "error_invalid_rule_set", th) }
                .onErrorResume(WebClientException::class.java) { th -> ResponseUtils.error(403, "error_download_from_upstream_failure", th) }
                .onErrorResume { throwable -> ResponseUtils.error(403, "error_unknown", ResponseUtils.log(ClashPreprocess::class.java, throwable)) }

    }

    private data class MetadataModel(var username: String = "", var userId: Long = 0, var messageId: Long = 0, var secret: String = "")

    companion object {
        private val PATTERN_ID = Pattern.compile("\\d+")
        private val PATTERN_SECRET = Pattern.compile("[a-f0-9]{32}")
    }
}
