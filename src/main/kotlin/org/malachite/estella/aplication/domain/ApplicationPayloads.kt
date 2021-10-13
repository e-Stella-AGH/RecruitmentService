package org.malachite.estella.aplication.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.malachite.estella.commons.models.offers.ApplicationStatus
import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.JobSeekerFile
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.commons.models.tasks.TaskStage
import org.malachite.estella.people.domain.*
import java.sql.Date
import java.time.LocalDate
import java.util.*
import javax.persistence.*

interface ApplicationPayload {
    fun toApplication(stage: RecruitmentStage, jobSeeker: JobSeeker, files: Set<JobSeekerFile>): Application
    fun getJobSeekerFiles(): Set<JobSeekerFilePayload>
}

//TODO: This solution with separate payloads is good enough for now
data class ApplicationLoggedInPayload(val files: Set<JobSeekerFilePayload>) : ApplicationPayload {
    override fun toApplication(stage: RecruitmentStage, jobSeeker: JobSeeker, files: Set<JobSeekerFile>) = Application(
        applicationDate = Date.valueOf(LocalDate.now()),
        status = ApplicationStatus.IN_PROGRESS,
        id = null,
        jobSeeker = jobSeeker,
        seekerFiles = files.toMutableSet(),
        applicationStages = mutableListOf()
    )

    override fun getJobSeekerFiles(): Set<JobSeekerFilePayload> = files
}

data class ApplicationNoUserPayload(
    val firstName: String,
    val lastName: String,
    val mail: String,
    val files: Set<JobSeekerFilePayload>
) : ApplicationPayload {
    fun toJobSeeker() = JobSeeker(
        id = null,
        user = User(
            id = null,
            firstName = firstName,
            lastName = lastName,
            mail = mail,
            password = null
        ),
        files = files.mapNotNull { it.toJobSeekerFile() }.toMutableSet()
    )

    override fun toApplication(stage: RecruitmentStage, jobSeeker: JobSeeker, files: Set<JobSeekerFile>) = Application(
        applicationDate = Date.valueOf(LocalDate.now()),
        status = ApplicationStatus.IN_PROGRESS,
        id = null,
        jobSeeker = jobSeeker,
        seekerFiles = files.toMutableSet(),
        applicationStages = mutableListOf()
    )

    override fun getJobSeekerFiles(): Set<JobSeekerFilePayload> = files
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
    val seekerFiles: Set<JobSeekerFileDTO>,
    val applicationStages: List<Int>
)

fun Application.toApplicationDTO() =
    ApplicationDTO(
        this.id,
        this.applicationDate,
        this.status,
        this.applicationStages.last().stage,
        this.jobSeeker.toJobSeekerDTO(),
        this.seekerFiles.map { it.toJobSeekerFileDTO() }.toSet(),
        this.applicationStages.map { it.stage.id!! }
    )

data class ApplicationDTOWithStagesListAndOfferName(
    val id: Int?,
    val applicationDate: Date,
    val status: ApplicationStatus,
    val stage: RecruitmentStage,
    val jobSeeker: JobSeekerDTO,
    val seekerFiles: Set<JobSeekerFileDTO>,
    val stages: List<RecruitmentStage>,
    val offerName: String
)

fun Application.toApplicationDTOWithStagesListAndOfferName(stages: List<RecruitmentStage>, offerName: String) =
    ApplicationDTOWithStagesListAndOfferName(
        this.id,
        this.applicationDate,
        this.status,
        this.applicationStages.last().stage,
        this.jobSeeker.toJobSeekerDTO(),
        this.seekerFiles.map { it.toJobSeekerFileDTO() }.toSet(),
        stages,
        offerName
    )

data class ApplicationStageDTO(
    val id: Int,
    val stage: RecruitmentStage,
    val applicationId: Int,
    val tasksStage: TaskStage?,
    val interview: Interview?
)

fun ApplicationStageData.toApplicationStageDTO(): ApplicationStageDTO =
    ApplicationStageDTO(
        id!!,
        stage,
        application.id!!,
        tasksStage,
        interview
    )