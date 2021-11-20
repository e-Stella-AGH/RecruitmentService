package org.malachite.estella.commons.models.offers

import com.fasterxml.jackson.annotation.JsonIgnore
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.JobSeekerFile
import org.malachite.estella.task.domain.TaskStageNotFoundException
import java.sql.Date
import javax.persistence.*

@Entity
@Table(name = "applications")
data class Application(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
    val applicationDate: Date, @Enumerated(EnumType.STRING) val status: ApplicationStatus,
    @ManyToOne val jobSeeker: JobSeeker,
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER) @JoinTable(
        name = "application_files",
        joinColumns = [JoinColumn(name = "application_id")],
        inverseJoinColumns = [JoinColumn(name = "job_seeker_file_id")]
    ) val seekerFiles: MutableSet<JobSeekerFile>,
) {

    @OneToMany(mappedBy = "application", fetch = FetchType.EAGER)
    var applicationStages: MutableList<ApplicationStageData> = mutableListOf()


    fun getCurrentApplicationStage(): ApplicationStageData = this.applicationStages.sortedBy { it.id }.last()


    constructor(
        id: Int?,
        applicationDate: Date,
        status: ApplicationStatus,
        jobSeeker: JobSeeker,
        seekerFiles: MutableSet<JobSeekerFile>,
        applicationStages: MutableList<ApplicationStageData>
    ) : this(id,applicationDate,status,jobSeeker, seekerFiles){
        this.applicationStages = applicationStages
    }

    fun copy(id: Int? = this.id,
             applicationDate: Date = this.applicationDate,
             status: ApplicationStatus = this.status,
             jobSeeker: JobSeeker = this.jobSeeker,
             seekerFiles: MutableSet<JobSeekerFile> = this.seekerFiles,
             applicationStages: MutableList<ApplicationStageData> = this.applicationStages) =
        Application(id,applicationDate,status,jobSeeker, seekerFiles, applicationStages)
}
