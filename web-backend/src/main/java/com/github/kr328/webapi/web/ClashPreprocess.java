package com.github.kr328.webapi.web;

import com.github.kr328.webapi.WebApiApplication;
import com.github.kr328.webapi.api.ClashPreprocessor;
import com.github.kr328.webapi.tools.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.file.Paths;
import java.util.regex.Pattern;

@Controller
public class ClashPreprocess {
    @Autowired
    private WebApiApplication.ExternalConfigure externalConfigure;

    public Mono<ServerResponse> handle(ServerRequest request) {
        String id = request.pathVariable("id");
        if ( !PATTERN_ID.matcher(id).matches() )
            return ServerResponse.badRequest().body(Mono.just("Invalid id"), String.class);

        return FileUtils.readLines(Paths.get(externalConfigure.getDataPath(), id, "data.yml"))
                .flatMap(ClashPreprocessor::process)
                .flatMap(s -> ServerResponse.ok().body(Mono.just(s), String.class));
    }

    private static final Pattern PATTERN_ID = Pattern.compile("\\d{32}");
}
