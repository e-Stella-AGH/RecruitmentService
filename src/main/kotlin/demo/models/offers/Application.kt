package demo.models.offers

import demo.models.people.JobSeeker
import demo.models.people.JobSeekerFile
import java.sql.Date
import javax.persistence.*

@Entity
@Table(name = "Applications")
data class Application(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val applicationDate: Date, @Enumerated(EnumType.STRING) val status: ApplicationStatus,
        @ManyToOne val stage: RecruitmentStage, @ManyToOne val jobSeeker: JobSeeker,
        @ManyToMany @JoinTable(
                name = "ApplicationFiles",
                joinColumns = [JoinColumn(name = "job_seekers_id")],
                inverseJoinColumns = [JoinColumn(name = "applications_id")]
        ) val seekerFiles: Set<JobSeekerFile>

)
