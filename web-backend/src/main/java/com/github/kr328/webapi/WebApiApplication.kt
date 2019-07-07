package com.github.kr328.webapi

import com.github.kr328.webapi.web.Components
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

fun main(args: Array<String>) {
    Global.WEBAPI_DATA_PATH

    SpringApplication.run(WebApiApplication::class.java, *args)
}

@SpringBootApplication
class WebApiApplication {
    @Bean
    fun routes(components: Components): RouterFunction<ServerResponse> {
        return router {
            GET("/surge2ssd") { components.surge2ShadowSocks.process(it) }
            GET("/preclash/{userId}/{secret}") { components.clashPreprocess.process(it) }
        }.andRoute({ true }, { ServerResponse.notFound().build() })
    }
}

