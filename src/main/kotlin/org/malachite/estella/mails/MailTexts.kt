package org.malachite.estella.mails

import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.Offer

object MailTexts {

    fun getVerificationText() =
        """Your company was successfully verified! You can log in now to your account!"""

    fun getUnVerificationText() =
        """We're sorry to inform you that your company was unverified and so your account was disabled. Please, contact us
            at estellaagh@gmail.com to resolve this issue.
        """.trimMargin()

    fun getRegistrationText() =
        """
            Thank you for registration in our service. We hope we will help you find employees or employer.
            Please, contact us at estellaagh@gmail.com with any questions you have..
        """.trimIndent()

    fun getApplicationConfirmation(application: Application, offer: Offer, hrPartnerFullName: String): String =
        """
            Hi ${application.jobSeeker.user.firstName}
            Thank you for submitting your application to be a ${offer.position}. 
            I with our team are reviewing your application and will be in touch if we think you’re a potential match for the position.
            All the best,
            $hrPartnerFullName
            """.trimIndent()

    fun getInterviewInvitation(interview: Interview, organizationName: String, url: String, hrPartnerFullName: String) =
        """
            Hi ${interview.application.jobSeeker.user.firstName}
            Thanks so much for your interest in joining the ${organizationName}! 
            We are excited to move you forward in our engineering recruiting process.
            Next step will be interview with our recruiters. It will take place at $url
            All the best,
            $hrPartnerFullName
            """.trimIndent()
}