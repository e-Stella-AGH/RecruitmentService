package demo.models.people

import javax.persistence.*

@Entity
@Table(name = "job_seekers")
class JobSeeker(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        @OneToOne val user:User,
        @OneToMany @JoinColumn(name="job_seeker_id") val files: Set<JobSeekerFile>
)