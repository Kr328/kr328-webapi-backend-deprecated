package com.github.kr328.webapi.web;

import com.alibaba.fastjson.JSONObject;
import com.github.kr328.webapi.WebApiApplication;
import com.github.kr328.webapi.api.ClashPreprocessor;
import com.github.kr328.webapi.api.clash.exceptions.DispatcherException;
import com.github.kr328.webapi.api.clash.exceptions.PreprocessorException;
import com.github.kr328.webapi.api.clash.exceptions.ProxySourceException;
import com.github.kr328.webapi.api.clash.exceptions.RuleSetException;
import com.github.kr328.webapi.tools.BurstLimiter;
import com.github.kr328.webapi.tools.FileUtils;
import com.github.kr328.webapi.tools.ResponseUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.yaml.snakeyaml.error.YAMLException;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Pattern;

@Controller
public class ClashPreprocess {
    private static final Pattern PATTERN_ID = Pattern.compile("\\d+");
    private static final Pattern PATTERN_SECRET = Pattern.compile("[a-f0-9]{32}");

    @Autowired
    private WebApiApplication.ExternalConfigure externalConfigure;
    private BurstLimiter limiter = new BurstLimiter();

    public Mono<ServerResponse> handle(ServerRequest request) {
        String userId = request.pathVariable("userId");
        String secret = request.pathVariable("secret");

        if (!PATTERN_ID.matcher(userId).matches() || !PATTERN_SECRET.matcher(secret).matches())
            return ResponseUtils.yamlError(400, "error_invalid_id_or_secret");

        if (limiter.note(userId))
            return ResponseUtils.yamlError(400, "error_burst");

        return FileUtils.readLines(Paths.get(externalConfigure.getDataPath(), userId, "metadata.json"))
                .map(s -> JSONObject.parseObject(s, MetadataModel.class))
                .filter(metadataModel -> metadataModel.getSecret().equals(secret))
                .switchIfEmpty(Mono.error(new FileNotFoundException()))
                .zipWhen(s -> FileUtils.readLines(Paths.get(externalConfigure.getDataPath(), userId, "data.yml")))
                .map(t -> t.mapT2(ClashPreprocessor::process))
                .flatMap(t -> ServerResponse.ok().contentType(MediaType.TEXT_PLAIN).headers(h -> h.setContentDispositionFormData("attachment", t.getT1().getUsername() + ".yml")).body(t.getT2(), String.class))
                .onErrorResume(IOException.class, th -> ResponseUtils.yamlError(404, "error_config_not_found", th))
                .onErrorResume(YAMLException.class, th -> ResponseUtils.yamlError(403, "error_invalid_config", th))
                .onErrorResume(DispatcherException.class, th -> ResponseUtils.yamlError(403, "error_invalid_dispatcher", th))
                .onErrorResume(PreprocessorException.class, th -> ResponseUtils.yamlError(403, "error_invalid_preprocessor", th))
                .onErrorResume(ProxySourceException.class, th -> ResponseUtils.yamlError(403, "error_invalid_proxy_source", th))
                .onErrorResume(RuleSetException.class, th -> ResponseUtils.yamlError(403, "error_invalid_rule_set", th))
                .onErrorResume(WebClientException.class, th -> ResponseUtils.yamlError(403, "error_download_from_upstream_failure", th))
                .onErrorResume(throwable -> ResponseUtils.yamlError(403,"error_unknown", throwable));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class MetadataModel {
        private String username;
        private long userId;
        private long messageId;
        private String secret;
    }
}