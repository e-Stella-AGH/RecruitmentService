package org.malachite.estella.util.dev.`null`

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ClientHttpRequest
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.context.Context
import java.nio.charset.Charset
import java.time.ZonedDateTime
import java.util.function.Consumer
import java.util.function.Function

val mailServiceResponse = object: WebClient.RequestHeadersSpec<Nothing> {
    override fun accept(vararg acceptableMediaTypes: MediaType?): Nothing {
        TODO("Not yet implemented")
    }

    override fun acceptCharset(vararg acceptableCharsets: Charset?): Nothing {
        TODO("Not yet implemented")
    }

    override fun cookie(name: String, value: String): Nothing {
        TODO("Not yet implemented")
    }

    override fun cookies(cookiesConsumer: Consumer<MultiValueMap<String, String>>): Nothing {
        TODO("Not yet implemented")
    }

    override fun ifModifiedSince(ifModifiedSince: ZonedDateTime): Nothing {
        TODO("Not yet implemented")
    }

    override fun ifNoneMatch(vararg ifNoneMatches: String?): Nothing {
        TODO("Not yet implemented")
    }

    override fun header(headerName: String, vararg headerValues: String?): Nothing {
        TODO("Not yet implemented")
    }

    override fun headers(headersConsumer: Consumer<HttpHeaders>): Nothing {
        TODO("Not yet implemented")
    }

    override fun attribute(name: String, value: Any): Nothing {
        TODO("Not yet implemented")
    }

    override fun attributes(attributesConsumer: Consumer<MutableMap<String, Any>>): Nothing {
        TODO("Not yet implemented")
    }

    override fun context(contextModifier: Function<Context, Context>): Nothing {
        TODO("Not yet implemented")
    }

    override fun httpRequest(requestConsumer: Consumer<ClientHttpRequest>): Nothing {
        TODO("Not yet implemented")
    }

    override fun retrieve(): WebClient.ResponseSpec {
        TODO("Not yet implemented")
    }

    override fun <V : Any?> exchangeToMono(responseHandler: Function<ClientResponse, out Mono<V>>): Mono<V> {
        TODO("Not yet implemented")
    }

    override fun <V : Any?> exchangeToFlux(responseHandler: Function<ClientResponse, out Flux<V>>): Flux<V> {
        TODO("Not yet implemented")
    }

    override fun exchange(): Mono<ClientResponse> {
        TODO("Not yet implemented")
    }
}