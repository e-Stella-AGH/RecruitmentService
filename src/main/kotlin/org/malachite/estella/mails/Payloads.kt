package org.malachite.estella.mails

import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.mails.MailTexts.getApplicationConfirmation
import org.malachite.estella.mails.MailTexts.getHrPartnerRegistrationText
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

fun Application.toApplicationConfirmationAsMailPayload(offer: Offer): MailPayload {
    val hrPartnerFullName = "${offer.creator.user.firstName} ${offer.creator.user.lastName}"
    return MailPayload(
        subject = "Your application has been received!",
        receiver = this.jobSeeker.user.mail,
        content = getApplicationConfirmation(this,offer,hrPartnerFullName),
        sender_name = hrPartnerFullName,
        sender_email = offer.creator.user.mail
    )
}

fun Interview.toInterviewInvitationAsMailPayload(offer: Offer): MailPayload {
    val hrPartnerFullName = "${offer.creator.user.firstName} ${offer.creator.user.lastName}"
    val url = "${MAIN_URL}interview/${this.id}"
    return offer.creator.organization.name.let {
        MailPayload(
            subject = "Your are invited for interview with ${it}!",
            receiver = this.applicationStage.application.jobSeeker.user.mail,
            content = MailTexts.getInterviewInvitation(this,it,url,hrPartnerFullName),
            sender_name = hrPartnerFullName,
            sender_email = offer.creator.user.mail
        )

    }
}

fun Organization.toVerificationMailPayload( verified: Boolean) =
    MailPayload(
        subject = "Your company has been ${if (verified) "verified" else "unverified"}!",
        sender_name = "e-Stella Team",
        receiver = this.user.mail,
        content = if (verified) getVerificationText() else getUnVerificationText(),
        sender_email = MAIN_MAIL
    )

fun User.toRegistrationMailPayload() =
    MailPayload(
        subject = "Thank you for register",
        sender_name = "e-Stella Team",
        receiver = this.mail,
        content = getRegistrationText(),
        sender_email = MAIN_MAIL
    )

fun HrPartner.toRegistrationMailPayload(password: String) =
    MailPayload(
        subject = "Your account as Recruiter was created",
        sender_name = "e-Stella Team",
        receiver = this.user.mail,
        content = getHrPartnerRegistrationText(this.organization.name,
            this.user.mail, password, MAIN_URL),
        sender_email = MAIN_MAIL
    )
