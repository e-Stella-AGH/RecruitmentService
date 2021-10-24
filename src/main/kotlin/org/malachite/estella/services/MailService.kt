package org.malachite.estella.services

import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.mails.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class MailService(
    @Value("\${mail_service_url}") final val SERVICE_URL: String
) {
    val restTemplate = RestTemplate()

    private fun sendMail(mailPayload: MailPayload) =
        restTemplate.postForObject("${SERVICE_URL}/email", mailPayload, Any::class.java)

    fun sendRegisterMail(user: User) =
        sendMail(user.toRegistrationMailPayload())

    fun sendHrPartnerRegisterMail(hrPartner: HrPartner, password: String) =
        sendMail(hrPartner.toRegistrationMailPayload(password))

    fun sendOrganizationVerificationMail(organization: Organization, verified: Boolean) =
        sendMail(organization.toVerificationMailPayload(verified))

    fun sendInterviewInvitationMail(offer: Offer, interview: Interview) =
        sendMail(interview.toInterviewInvitationAsMailPayload(offer))

    fun sendInterviewDevInvitationMail(offer: Offer, interview:Interview, application: Application, hostMail: String) =
        sendMail(interview.toInterviewDevInvitationAsMailPayload(offer, application,hostMail))

    fun sendInterviewDateConfirmationMail(offer: Offer, interview:Interview, application: Application, mail: String) =
        sendMail(interview.toInterviewDateConfirmationAsMailPayload(offer, application,mail))

    fun sendApplicationConfirmationMail(offer: Offer, application: Application) =
        sendMail(application.toApplicationConfirmationAsMailPayload(offer))

}