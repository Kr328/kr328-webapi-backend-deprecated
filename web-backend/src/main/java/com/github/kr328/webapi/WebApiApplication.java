package com.github.kr328.webapi;

import com.github.kr328.webapi.web.ApiControllers;
import lombok.Data;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
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
                .add(route(GET("/preclash/{userId}/{secret}"), controllers.getClashPreprocess()::handle))
                .add(route(all(), request -> ServerResponse.notFound().build()))
                .build();
    }

    @Data
    public static class ExternalConfigure {
        private String dataPath;

        @Bean
        @Primary
        public static ExternalConfigure create() {
            ExternalConfigure externalConfigure = new ExternalConfigure();

            externalConfigure.setDataPath(System.getenv("WEBAPI_DATA_PATH"));

            if ( externalConfigure.getDataPath() == null )
                throw new IllegalArgumentException("WEBAPI_DATA_PATH must be set in env");

            return externalConfigure;
        }
    }
}

