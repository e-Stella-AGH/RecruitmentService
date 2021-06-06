package demo.dto.application

import demo.models.offers.Application
import demo.models.offers.ApplicationStatus
import demo.models.offers.RecruitmentStage
import demo.models.people.JobSeeker
import demo.models.people.User
import java.sql.Date
import java.time.LocalDate
import java.util.*

//TODO: This solution with separate payloads is temporary
data class ApplicationLoggedInPayload(val userId: Int, val files: Set<JobSeekerFilePayload>) {
    fun toApplication(stage: RecruitmentStage, jobSeeker: JobSeeker) = Application(
            applicationDate = Date.valueOf(LocalDate.now()),
            status = ApplicationStatus.IN_PROGRESS,
            id = null,
            stage = stage,
            jobSeeker = jobSeeker,
            seekerFiles = files.mapNotNull{it.toJobSeekerFile()}.toSet(),
            tasksResults = Collections.emptySet(),
            quizzesResults = Collections.emptySet(),
            interviews = Collections.emptySet(),
    )
}

data class ApplicationNoUserPayload(
        val firstName: String,
        val lastName: String,
        val mail: String,
        val files: Set<JobSeekerFilePayload>
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
            files = files.mapNotNull{it.toJobSeekerFile()}.toSet()
    )

    fun toApplication(stage: RecruitmentStage, jobSeeker: JobSeeker) = Application(
            applicationDate = Date.valueOf(LocalDate.now()),
            status = ApplicationStatus.IN_PROGRESS,
            id = null,
            stage = stage,
            jobSeeker = jobSeeker,
            seekerFiles = jobSeeker.files.toSet(),
            tasksResults = Collections.emptySet(),
            quizzesResults = Collections.emptySet(),
            interviews = Collections.emptySet(),
    )
}