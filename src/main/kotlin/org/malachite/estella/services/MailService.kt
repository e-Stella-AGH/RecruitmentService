package org.malachite.estella.services

import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class MailService(
    @Value("\${mail_service_url}") val SERVICE_URL: String
) {

    private val MAIN_URL = "https://e-stella-site.herokuapp.com/"
    private val MAIN_MAIL = "estellaagh@gmail.com"

    fun sendMail(mailPayload: MailPayload) {
        val restTemplate = RestTemplate()
        restTemplate
            .postForLocation("$SERVICE_URL/email", mailPayload.toHttpEntity())
    }

    fun getApplicationConfirmationAsMailPayload(offer: Offer, application: Application): MailPayload {
        val hrPartnerFullName = "${offer.creator.user.firstName} ${offer.creator.user.lastName}"
        return MailPayload(
            subject = "Your application has been received!",
            receiver = application.jobSeeker.user.mail,
            content = """
                Hi ${application.jobSeeker.user.firstName}
                Thank you for submitting your application to be a ${offer.position}. 
                I with our team are reviewing your application and will be in touch if we think you’re a potential match for the position.
                All the best,
                $hrPartnerFullName
                """.trimIndent(),
            sender_name = hrPartnerFullName,
            sender_email = offer.creator.user.mail
        )
    }

    fun getInterviewInvitationAsMailPayload(offer: Offer, interview: Interview): MailPayload {
        val hrPartnerFullName = "${offer.creator.user.firstName} ${offer.creator.user.lastName}"
        val url = "${MAIN_URL}interview/${interview.id}"
        return offer.creator.organization.name.let {
            MailPayload(
                subject = "Your are invited for interview with ${it}!",
                receiver = interview.application.jobSeeker.user.mail,
                content = """
                Hi ${interview.application.jobSeeker.user.firstName}
                Thanks so much for your interest in joining the ${it}! 
                We are excited to move you forward in our engineering recruiting process.
                Next step will be interview with our recruiters. It will take place at $url
                All the best,
                $hrPartnerFullName
                """.trimIndent(),
                sender_name = hrPartnerFullName,
                sender_email = offer.creator.user.mail
            )

        }
    }

    fun organizationVerificationMailPayload(organization: Organization, verified: Boolean) =
        MailPayload(
            subject = "Your company has been ${if(verified) "verified" else "unverified"}!",
            sender_name = "e-Stella Team",
            receiver = organization.user.mail,
            content = if(verified) getVerificationText() else getUnVerificationText(),
            sender_email = MAIN_MAIL
        )

    fun getVerificationText() =
        """Your company was successfully verified! You can log in now to your account!"""

    fun getUnVerificationText() =
        """We're sorry to inform you that your company was unverified and so your account was disabled. Please, contact us
            at estellaagh@gmail.com to resolve this issue.
        """.trimMargin()

    fun userRegistrationMailPayload(user:User) =
        MailPayload(
            subject = "Thank you for register",
            sender_name = "e-Stella Team",
            receiver = user.mail,
            content = getRegistrationText(),
            sender_email = MAIN_MAIL
        )

    fun getRegistrationText() =
        """
            Thank you for registration in our service. We hope we will help you find employees or employer.
            Please, contact us at estellaagh@gmail.com with any questions you have..
        """.trimIndent()
}

data class MailPayload(
    val subject: String,
    val sender_name: String,
    val receiver: String,
    val content: String,
    val sender_email: String
) {

    fun toHttpEntity(): HttpEntity<MailPayload> {
        return HttpEntity(this)
    }
}