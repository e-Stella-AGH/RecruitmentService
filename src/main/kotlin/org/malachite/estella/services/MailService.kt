package org.malachite.estella.services

import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.commons.models.tasks.TaskStage
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

    fun sendInterviewHostInvitationMail(offer: Offer, interview:Interview, application: Application, hostMail: String) =
        sendMail(interview.toInterviewHostInvitationAsMailPayload(offer, application,hostMail))

    fun sendInterviewJobSeekerConfirmationMail(offer: Offer, interview:Interview, application: Application, mail: String) =
        sendMail(interview.toInterviewJobSeekerConfirmationAsMailPayload(offer, application,mail))

    fun sendApplicationConfirmationMail(offer: Offer, application: Application) =
        sendMail(application.toApplicationConfirmationAsMailPayload(offer))

    fun sendTaskAssignmentRequest(devMail: String, taskStage: TaskStage, offer: Offer) {
        sendMail(taskStage.toTaskAssignmentRequestPayload(devMail, offer))
    }

    fun sendTaskAssignedNotification(devMail: String, taskStage: TaskStage, offer: Offer) {
        sendMail(taskStage.toTaskAssignedNotificationPayload(devMail, offer))
    }

    fun sendTaskSubmittedNotification(devMail: String, taskStage: TaskStage, timeToWait: Int, offer: Offer) {
        sendMail(taskStage.toTaskSubmittedNotificationPayload(devMail, timeToWait, offer))
    }

}