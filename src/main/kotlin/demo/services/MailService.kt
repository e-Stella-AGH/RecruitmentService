package demo.services

import com.fasterxml.jackson.databind.ObjectMapper
import demo.models.interviews.Interview
import demo.models.offers.Application
import demo.models.offers.Offer
import org.springframework.http.HttpEntity
import org.springframework.web.client.RestTemplate
import java.net.http.HttpRequest

object MailService {

    const val SERVICE_URL = "https://email-service-estella.herokuapp.com/email"
    const val MAIN_URL = "https://e-stella-site.herokuapp.com/"

    fun sendMail(mailPayload: MailPayload) {
        val restTemplate = RestTemplate();
        restTemplate
            .postForLocation(SERVICE_URL, mailPayload.toHttpEntity())
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
        val organizationName = offer.creator.organization
        return MailPayload(
            subject = "Your are invited for interview with ${organizationName}!",
            receiver = interview.application.jobSeeker.user.mail,
            content = """
                Hi ${interview.application.jobSeeker.user.firstName}
                Thanks so much for your interest in joining the ${organizationName}! 
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