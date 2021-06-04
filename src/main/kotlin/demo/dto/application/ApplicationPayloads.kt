package demo.dto.application

import demo.models.offers.Application
import demo.models.offers.ApplicationStatus
import demo.models.offers.RecruitmentStage
import demo.models.people.JobSeeker
import demo.models.people.User
import java.sql.Date
import java.time.LocalDate
import java.util.*

data class ApplicationLoggedInPayload(val userId: Int) {
    fun toApplication(stage: RecruitmentStage, jobSeeker: JobSeeker) = Application(
            applicationDate = Date.valueOf(LocalDate.now()),
            status = ApplicationStatus.IN_PROGRESS,
            id = null,
            stage = stage,
            jobSeeker = jobSeeker,
            seekerFiles = Collections.emptySet(),
            tasksResults = Collections.emptySet(),
            quizzesResults = Collections.emptySet(),
            interviews = Collections.emptySet(),
    )
}

data class ApplicationNoUserPayload(
        val firstName: String,
        val lastName: String,
        val mail: String
) {
    fun toJobSeeker() = JobSeeker(
            id = null,
            user = User(
                    id = null,
                    firstName = firstName,
                    lastName = lastName,
                    mail = mail,
                    password = null
            ),
            files = Collections.emptySet()
    )

    fun toApplication(stage: RecruitmentStage, jobSeeker: JobSeeker) = Application(
            applicationDate = Date.valueOf(LocalDate.now()),
            status = ApplicationStatus.IN_PROGRESS,
            id = null,
            stage = stage,
            jobSeeker = jobSeeker,
            seekerFiles = Collections.emptySet(),
            tasksResults = Collections.emptySet(),
            quizzesResults = Collections.emptySet(),
            interviews = Collections.emptySet(),
    )
}