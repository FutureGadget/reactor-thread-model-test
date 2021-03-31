package com.block.threads

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import reactor.util.Loggers

@SpringBootApplication
class ThreadsApplication

fun main(args: Array<String>) {
    runApplication<ThreadsApplication>(*args)
}

data class Message(val message: String)

@EnableWebFlux
@Configuration
class WebConfig : WebFluxConfigurer {

    val logger = Loggers.getLogger(WebConfig::class.java)

    @Bean
    fun routerFunction(): RouterFunction<ServerResponse> {
        return router {
            GET("/test") { req ->
                logger.debug("very outside - ${Thread.currentThread().name}")
                asyncSubscribe().flatMap {
                    ServerResponse.ok().bodyValue(Message(it))
                }
            }
        }
    }

    fun asyncSubscribe(): Mono<String> {
        logger.debug("outside - ${Thread.currentThread().name}")
        return Mono.just("Test")
            .map {
                Mono.defer {
                    logger.debug("inside - ${Thread.currentThread().name}")
                    Thread.sleep(5000L)
                    Mono.empty<String>()
                }.subscribe() // .subscribeOn(Schedulers.boundedElastic()).subscribe()
                it
            }
    }
}