package org.malachite.estella.aplication.domain

import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.interviews.Note
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.malachite.estella.commons.models.offers.ApplicationStatus
import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.JobSeekerFile
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.commons.models.tasks.TaskResult
import org.malachite.estella.commons.models.tasks.TaskStage
import org.malachite.estella.people.domain.*
import org.malachite.estella.services.TasksNotes
import java.sql.Blob
import java.sql.Clob
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.util.*
import javax.sql.rowset.serial.SerialBlob
import javax.sql.rowset.serial.SerialClob

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

fun Clob?.toBase64String(): String =
    this?.let {
        Base64.getEncoder()
            .encodeToString(this.getSubString(1, this.length().toInt()).toByteArray())
    } ?: ""

fun Blob.toBase64String(): String =
    Base64.getEncoder().encodeToString(this.getBytes(1, this.length().toInt()))

fun String.toClob(): Clob =
    SerialClob(String(Base64.getDecoder().decode(this)).toCharArray())

fun String.toBlob(): Blob =
    SerialBlob(String(Base64.getDecoder().decode(this)).toByteArray())

fun String.decodeBase64(): String =
    String(Base64.getDecoder().decode(this))

fun String.encodeToBase64(): String =
    String(Base64.getEncoder().encode(this.toByteArray()))


data class ApplicationNoteDTO(val author: String, val tags: List<String>, val text: String)

fun Note.toApplicationNoteDTO(): ApplicationNoteDTO =
    ApplicationNoteDTO(author, tags.map { it.text }, text.toBase64String())

data class ApplicationNotes(val notes: List<ApplicationNoteDTO>)

fun List<Note>.toApplicationNotesDTO(): ApplicationNotes =
    ApplicationNotes(this.map { it.toApplicationNoteDTO() })

data class TasksNotesDTO(val tasks: List<TaskResultWithTestDTO>, val notes: List<ApplicationNoteDTO>)

data class TaskResultWithTestDTO(
    val code: String,
    val results: String,
    val tests: String,
    val description: String,
)

fun TaskResult.toTaskResultWithTestDTO(): TaskResultWithTestDTO =
    TaskResultWithTestDTO(
        code.toBase64String(),
        this.results.toBase64String(),
        task.tests.toBase64String(),
        task.description.toBase64String()
    )

fun TasksNotes.toTasksNotesDTO(): TasksNotesDTO =
    TasksNotesDTO(
        this.first.map { it.toTaskResultWithTestDTO() },
        this.second.map { it.toApplicationNoteDTO() }
    )


