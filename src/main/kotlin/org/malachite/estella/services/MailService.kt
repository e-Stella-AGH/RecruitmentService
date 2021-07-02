package org.malachite.estella.services

import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.mails.MailPayload
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class MailService(
    @Value("\${mail_service_url}") val SERVICE_URL: String
) {

    fun sendMail(mailPayload: MailPayload) {
        val restTemplate = RestTemplate()
        restTemplate
            .postForLocation("$SERVICE_URL/email", mailPayload.toHttpEntity())
    }

}