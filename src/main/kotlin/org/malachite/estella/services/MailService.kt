package org.malachite.estella.services

import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.mails.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class MailService(
    @Value("\${mail_service_url}") val MAIL_SERVICE_URL: String
) {

    private fun sendMail(mailPayload: MailPayload) {
        val restTemplate = RestTemplate()
        restTemplate
            .postForLocation("$MAIL_SERVICE_URL/email", mailPayload.toHttpEntity())
    }


    fun sendRegisterMail(user:User) =
        sendMail(userRegistrationMailPayload(user))

    fun sendOrganizationVerificationMail(organization: Organization,verified:Boolean) =
        sendMail(organizationVerificationMailPayload(organization, verified))

    fun sendInterviewInvitationMail(offer: Offer, interview:Interview) =
        sendMail(getInterviewInvitationAsMailPayload(offer, interview))

    fun sendApplicationConfirmationMail(offer: Offer,application: Application) =
        sendMail(getApplicationConfirmationAsMailPayload(offer, application))

}