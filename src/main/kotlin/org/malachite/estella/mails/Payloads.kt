package org.malachite.estella.mails

import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.commons.models.tasks.TaskStage
import org.malachite.estella.mails.MailTexts.getApplicationConfirmation
import org.malachite.estella.mails.MailTexts.getHrPartnerRegistrationText
import org.malachite.estella.mails.MailTexts.getRegistrationText
import org.malachite.estella.mails.MailTexts.getUnVerificationText
import org.malachite.estella.mails.MailTexts.getVerificationText
import org.springframework.http.HttpEntity
import java.util.*

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

private val MAIN_URL = "https://e-stella-agh.github.io/MainFrontApp/#/"
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

fun Interview.toInterviewHostInvitationAsMailPayload(offer: Offer, application: Application, hostMail: String): MailPayload {
    val hrPartnerFullName = "${offer.creator.user.firstName} ${offer.creator.user.lastName}"
    val jobSeekerFullName = "${application.jobSeeker.user.firstName} ${application.jobSeeker.user.lastName}"
    val role = when (this.applicationStage.stage.type) {
        StageType.HR_INTERVIEW -> "hr/"
        StageType.TECHNICAL_INTERVIEW -> "technical/"
        else -> ""
    }
    val url = "${MAIN_URL}interview/$role${this.id}/${offer.creator.organization.id}"

    val passwordNote = if (role == "technical/")
        """
        Password for adding notes is 
        ${this.applicationStage.tasksStage!!.id.toString()}
        but remember that you can use any of passwords we've sent you that are still valid."""
    else ""

    return MailPayload(
            subject = "You are invited for interview with ${jobSeekerFullName}!",
            receiver = hostMail,
            content = MailTexts.getInterviewHostInvitation(jobSeekerFullName, url, this.dateTime.toString(), hrPartnerFullName, offer.position, passwordNote),
            sender_name = hrPartnerFullName,
            sender_email = offer.creator.user.mail
    )
}

fun Interview.toInterviewJobSeekerConfirmationAsMailPayload(offer: Offer, application: Application, mail: String): MailPayload {
    val hrPartnerFullName = "${offer.creator.user.firstName} ${offer.creator.user.lastName}"
    val jobSeekerFullName = "${application.jobSeeker.user.firstName} ${application.jobSeeker.user.lastName}"
    val position = offer.position
    val role = when (this.applicationStage.stage.type) {
            StageType.HR_INTERVIEW -> "hr/"
            StageType.TECHNICAL_INTERVIEW -> "technical/"
            else -> ""
        }

    val url = "${MAIN_URL}interview/$role${this.id}"
    val date = this.dateTime.toString()
    return MailPayload(
            subject = "Your interview's date for $position has been set!",
            receiver = mail,
            content = MailTexts.getInterviewJobSeekerConfirmation(jobSeekerFullName, url, date, hrPartnerFullName, offer.position),
            sender_name = hrPartnerFullName,
            sender_email = offer.creator.user.mail
    )
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

fun TaskStage.toTaskAssignmentRequestPayload(mail: String, offer: Offer): MailPayload {
    val hrPartnerFullName = "${offer.creator.user.firstName} ${offer.creator.user.lastName}"
    val encodedMail = String(Base64.getEncoder().encode(mail.toByteArray()))
    val url = "${MAIN_URL}tasks/assign/${offer.creator.organization.id}/$encodedMail"
    return MailPayload(
            subject = "You have been requested to assign task",
            sender_name = "e-Stella Team",
            receiver = mail,
            content = MailTexts.getTaskAssignmentRequestText(this.applicationStage.stage.type, url, hrPartnerFullName, offer.position, this.id!!),
            sender_email = MAIN_MAIL
    )
}
fun TaskStage.toTaskAssignedNotificationPayload(mail: String, offer: Offer): MailPayload {
    val hrPartnerFullName = "${offer.creator.user.firstName} ${offer.creator.user.lastName}"
    val url = "${MAIN_URL}task/${this.id}"
    return MailPayload(
            subject = "You have been requested to solve a task",
            sender_name = "e-Stella Team",
            receiver = mail,
            content = MailTexts.getTaskAssignedNotificationText(url, hrPartnerFullName, offer.position),
            sender_email = MAIN_MAIL
    )
}

fun TaskStage.toTaskSubmittedNotificationPayload(mail: String, timeToWait: Int, offer: Offer): MailPayload {
    val hrPartnerFullName = "${offer.creator.user.firstName} ${offer.creator.user.lastName}"
    val url = "${MAIN_URL}tasks/review/${this.id}"
    val password = offer.creator.organization.id!!
    return MailPayload(
            subject = "You have been requested to review a task",
            sender_name = "e-Stella Team",
            receiver = mail,
            content = MailTexts.getTaskSubmittedNotificationText(url, timeToWait, hrPartnerFullName, password),
            sender_email = MAIN_MAIL
    )
}

