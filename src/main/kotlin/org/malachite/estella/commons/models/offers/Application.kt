package org.malachite.estella.commons.models.offers

import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.JobSeekerFile
import java.sql.Date
import javax.persistence.*

@Entity
@Table(name = "applications")
data class Application(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int?,
    val applicationDate: Date,

    @Enumerated(EnumType.STRING)
    val status: ApplicationStatus,
    @ManyToOne val jobSeeker: JobSeeker,

    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinTable(
        name = "application_files",
        joinColumns = [JoinColumn(name = "application_id")],
        inverseJoinColumns = [JoinColumn(name = "job_seeker_file_id")]
    )
    val seekerFiles: MutableSet<JobSeekerFile>,

    @OneToMany(mappedBy = "application", fetch = FetchType.EAGER)
    var applicationStages: MutableList<ApplicationStageData> = mutableListOf()
) {

    override fun equals(other: Any?) = other is Application && EssentialData(this) == EssentialData(other)
    override fun hashCode() = EssentialData(this).hashCode()
    override fun toString() = EssentialData(this).toString().replaceFirst("EssentialData", "Application")

    companion object {
        private data class EssentialData(
            val id: Int?,
            val applicationDate: Date,
            val status: ApplicationStatus,
            val jobSeeker: JobSeeker,
            val seekerFiles: MutableSet<JobSeekerFile>,
        ) {
            constructor(data: Application) : this(
                data.id, data.applicationDate, data.status, data.jobSeeker, data.seekerFiles
            )
        }
    }

    fun getCurrentApplicationStage(): ApplicationStageData = this.applicationStages.sortedBy { it.id }.last()
}
