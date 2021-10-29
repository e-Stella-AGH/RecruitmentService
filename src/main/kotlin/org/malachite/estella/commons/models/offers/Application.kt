package org.malachite.estella.commons.models.offers

import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.JobSeekerFile
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
    @OneToMany(mappedBy = "application") val applicationStages: MutableList<ApplicationStageData>
)
