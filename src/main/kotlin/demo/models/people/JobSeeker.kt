package demo.models.people

import javax.persistence.*

@Entity
@Table(name = "job_seekers")
data class JobSeeker(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        @OneToOne(cascade = [CascadeType.ALL]) val user:User,
        @OneToMany(cascade = [CascadeType.ALL]) @JoinColumn(name="job_seeker_id") val files: Set<JobSeekerFile>
)