package org.malachite.estella.util

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.AfterAll

abstract class Wiremock {

    companion object {
        val wiremock = WireMockServer(9797)

        @AfterAll
        @JvmStatic
        fun cleanupAll(){
            wiremock.resetAll()
        }
    }

    init {
        wiremock.start()
    }
}