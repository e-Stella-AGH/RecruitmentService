package demo.services

import com.fasterxml.jackson.databind.ObjectMapper
import demo.models.offers.Application
import demo.models.offers.Offer
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object MailService {

    val SERVICE_URL = "https://email-service-estella.herokuapp.com/email"

    fun send_mail(mailPayload: MailPayload) {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(SERVICE_URL))
            .POST(HttpRequest.BodyPublishers.ofString(mailPayload.toRequestBody()))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        println(response.body())
    }

    fun getMailPayloadFromApplication(offer: Offer, application: Application): MailPayload {
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

}

data class MailPayload(
    val subject: String,
    val sender_name: String,
    val receiver: String,
    val content: String,
    val sender_email: String
) {
    fun toRequestBody(): String {
        val values = mapOf(
            "subject" to subject,
            "sender_name" to sender_name,
            "receiver" to receiver,
            "content" to content,
            "sender_email" to sender_email
        )
        return ObjectMapper().writeValueAsString(values)
    }
}