package org.malachite.estella.util

import com.github.tomakehurst.wiremock.client.WireMock.*

object EmailServiceStub: Wiremock() {

    fun stubForSendEmail() {
        wiremock.stubFor(post(urlEqualTo("/email"))
            .willReturn(aResponse().withStatus(200))
        )
    }

}