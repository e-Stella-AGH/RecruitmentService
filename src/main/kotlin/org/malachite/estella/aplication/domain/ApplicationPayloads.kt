package org.malachite.estella.aplication.domain

import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.ApplicationStatus
import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.people.domain.*
import java.sql.Date
import java.time.LocalDate
import java.util.*

interface ApplicationPayload{
    fun toApplication(stage: RecruitmentStage, jobSeeker: JobSeeker):Application
}

//TODO: This solution with separate payloads is good enough for now
data class ApplicationLoggedInPayload(val files: Set<JobSeekerFilePayload>) : ApplicationPayload {
    override fun toApplication(stage: RecruitmentStage, jobSeeker: JobSeeker) = Application(
        applicationDate = Date.valueOf(LocalDate.now()),
        status = ApplicationStatus.IN_PROGRESS,
        id = null,
        stage = stage,
        jobSeeker = jobSeeker,
        seekerFiles = files.mapNotNull { it.toJobSeekerFile() }.toSet(),
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
): ApplicationPayload {
    fun toJobSeeker() = JobSeeker(
        id = null,
        user = User(
            id = null,
            firstName = firstName,
            lastName = lastName,
            mail = mail,
            password = null
        ),
        files = files.mapNotNull { it.toJobSeekerFile() }.toSet()
    )

    override fun toApplication(stage: RecruitmentStage, jobSeeker: JobSeeker) = Application(
        applicationDate = Date.valueOf(LocalDate.now()),
        status = ApplicationStatus.IN_PROGRESS,
        id = null,
        stage = stage,
        jobSeeker = jobSeeker,
        seekerFiles = files.mapNotNull { it.toJobSeekerFile() }.toSet(),
        tasksResults = Collections.emptySet(),
        quizzesResults = Collections.emptySet(),
        interviews = Collections.emptySet(),
    )
}

////////
//// DTO TO READ APPLICATIONS
////////

data class ApplicationDTO(
    val id: Int?,
    val applicationDate: Date,
    val status: ApplicationStatus,
    val stage: RecruitmentStage,
    val jobSeeker: JobSeekerDTO,
    val seekerFiles: Set<JobSeekerFileDTO>
)

fun Application.toApplicationDTO() =
    ApplicationDTO(
        this.id,
        this.applicationDate,
        this.status,
        this.stage,
        this.jobSeeker.toJobSeekerDTO(),
        this.seekerFiles.map { it.toJobSeekerFileDTO() }.toSet()
    )