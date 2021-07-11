package org.malachite.estella.people

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.services.JobSeekerService
import org.malachite.estella.services.MailService
import org.malachite.estella.services.UserService
import org.malachite.estella.util.jobSeekers
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ClientHttpRequest
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.context.Context
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.nio.charset.Charset
import java.time.ZonedDateTime
import java.util.function.Consumer
import java.util.function.Function

class JobSeekerServiceTest {

    private val jobSeekerRepository = DummyJobSeekerRepository()
    private val userRepository = DummyUserRepository()
    private val mailServiceMock = mockk<MailService>()
    private val userService = UserService(userRepository, mailServiceMock)
    private val service = JobSeekerService(jobSeekerRepository, userService)

    @BeforeEach
    fun setup() {
        every { mailServiceMock.sendRegisterMail(any()) } returns mailServiceResponse
    }

    @AfterEach
    fun cleanup() {
        jobSeekerRepository.clear()
        userRepository.clear()
    }

    @Test
    fun `should be able to add job seekers`() {
        testUsers.forEach {
            service.addJobSeeker(it)
        }
        expectThat(service.getJobSeekers()).isEqualTo(testUsers as MutableIterable<JobSeeker>)
    }

    @Test
    fun `should be able to register jobSeekers and send mails to them`() {
        testUsers.forEach {
            service.registerJobSeeker(it)
        }
        expectThat(service.getJobSeekers()).isEqualTo(testUsers as MutableIterable<JobSeeker>)
        verify(exactly = testUsers.size) { mailServiceMock.sendRegisterMail(any()) }
    }

    private val testUsers = jobSeekers

}

private val mailServiceResponse = object: WebClient.RequestHeadersSpec<Nothing> {
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