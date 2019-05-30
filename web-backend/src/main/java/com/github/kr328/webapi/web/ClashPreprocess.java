package com.github.kr328.webapi.web;

import com.alibaba.fastjson.JSONObject;
import com.github.kr328.webapi.WebApiApplication;
import com.github.kr328.webapi.api.ClashPreprocessor;
import com.github.kr328.webapi.api.clash.exceptions.DispatcherException;
import com.github.kr328.webapi.api.clash.exceptions.PreprocessorException;
import com.github.kr328.webapi.api.clash.exceptions.ProxySourceException;
import com.github.kr328.webapi.api.clash.exceptions.RuleSetException;
import com.github.kr328.webapi.api.clash.model.Preprocessor;
import com.github.kr328.webapi.api.clash.model.ProxySource;
import com.github.kr328.webapi.api.clash.model.RuleSet;
import com.github.kr328.webapi.tools.FileUtils;
import com.github.kr328.webapi.tools.ResponseUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.resource.ResourceUrlProvider;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.regex.Pattern;

@Controller
public class ClashPreprocess {
    @Autowired
    private WebApiApplication.ExternalConfigure externalConfigure;

    public Mono<ServerResponse> handle(ServerRequest request) {
        String userId = request.pathVariable("userId");
        String secret = request.pathVariable("secret");

        if ( !PATTERN_ID.matcher(userId).matches() || !PATTERN_SECRET.matcher(secret).matches() )
            return ResponseUtils.yamlError(400, 1, "invalid_id_or_secret");

        return FileUtils.readLines(Paths.get(externalConfigure.getDataPath(), userId, "metadata.json"))
                .map(s -> JSONObject.parseObject(s, MetadataModel.class))
                .filter(metadataModel -> metadataModel.getSecret().equals(secret))
                .switchIfEmpty(Mono.error(new FileNotFoundException()))
                .flatMap((metadata) -> FileUtils.readLines(Paths.get(externalConfigure.getDataPath(), userId, "data.yml")))
                .flatMap(ClashPreprocessor::process)
                .flatMap(s -> ServerResponse.ok().body(Mono.just(s), String.class))
                .onErrorResume(IOException.class, th -> ResponseUtils.yamlError(404 ,2, "config_not_found"))
                .onErrorResume(YAMLException.class, th -> ResponseUtils.yamlError(403, 3, "invalid_config"))
                .onErrorResume(DispatcherException.class ,th -> ResponseUtils.yamlError(403, 4, "invalid_dispatcher"))
                .onErrorResume(PreprocessorException.class, th -> ResponseUtils.yamlError(403, 5, "invalid_preprocessor"))
                .onErrorResume(ProxySourceException.class, th -> ResponseUtils.yamlError(403, 6, "invalid_proxy_source"))
                .onErrorResume(RuleSetException.class ,th -> ResponseUtils.yamlError(403, 7, "invalid_rule_set"))
                .onErrorResume(WebClientException.class, th -> ResponseUtils.yamlError(403, 8, "download_from_upstream_failure"))
                .onErrorResume(throwable -> ResponseUtils.yamlError(403, -1, "unknown_error"));
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

    private static final Pattern PATTERN_ID = Pattern.compile("\\d+");
    private static final Pattern PATTERN_SECRET = Pattern.compile("[a-f0-9]{32}");
}
