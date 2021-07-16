package org.malachite.estella.application

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.malachite.estella.BaseIntegration
import org.malachite.estella.commons.loader.FakeLoader
import org.malachite.estella.services.ApplicationService
import org.malachite.estella.util.offersWithProcess
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ApplicationIntegration: BaseIntegration() {

    @Autowired
    private lateinit var applicationService: ApplicationService

    @Test
    @Order(1)
    fun `should be able to apply for an offer`() {
//        val response = httpRequest(
//                path = "/api/application/${offer.id}/no-user",
//                method = HttpMethod.POST,
//
//        )
    }


    val user = FakeLoader.getFakeJobSeekers()[0]
    val password = "a"

    val offer = offersWithProcess[0]



}