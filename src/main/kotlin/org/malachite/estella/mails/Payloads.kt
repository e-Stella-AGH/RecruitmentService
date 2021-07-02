package org.malachite.estella.mails

import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.mails.MailTexts.getApplicationConfirmation
import org.malachite.estella.mails.MailTexts.getRegistrationText
import org.malachite.estella.mails.MailTexts.getUnVerificationText
import org.malachite.estella.mails.MailTexts.getVerificationText
import org.springframework.http.HttpEntity

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

private val MAIN_URL = "https://e-stella-site.herokuapp.com/"
private val MAIN_MAIL = "estellaagh@gmail.com"

fun getApplicationConfirmationAsMailPayload(offer: Offer, application: Application): MailPayload {
    val hrPartnerFullName = "${offer.creator.user.firstName} ${offer.creator.user.lastName}"
    return MailPayload(
        subject = "Your application has been received!",
        receiver = application.jobSeeker.user.mail,
        content = getApplicationConfirmation(application,offer,hrPartnerFullName),
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
            content = MailTexts.getInterviewInvitation(interview,it,url,hrPartnerFullName),
            sender_name = hrPartnerFullName,
            sender_email = offer.creator.user.mail
        )

    }
}

fun organizationVerificationMailPayload(organization: Organization, verified: Boolean) =
    MailPayload(
        subject = "Your company has been ${if (verified) "verified" else "unverified"}!",
        sender_name = "e-Stella Team",
        receiver = organization.user.mail,
        content = if (verified) getVerificationText() else getUnVerificationText(),
        sender_email = MAIN_MAIL
    )

fun userRegistrationMailPayload(user: User) =
    MailPayload(
        subject = "Thank you for register",
        sender_name = "e-Stella Team",
        receiver = user.mail,
        content = getRegistrationText(),
        sender_email = MAIN_MAIL
    )


