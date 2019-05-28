package com.github.kr328.webapi;

import com.github.kr328.webapi.web.ApiControllers;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.all;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class WebApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebApiApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> routes(ApiControllers controllers) {
        return route()
                .add(route(path("/surge2ssd"), controllers.getSurge2ShadowSocks()::process))
                .add(route(all(), request -> ServerResponse.notFound().build()))
                .build();
    }
}

