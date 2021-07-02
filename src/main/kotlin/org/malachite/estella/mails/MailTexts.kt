package org.malachite.estella.mails

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

}