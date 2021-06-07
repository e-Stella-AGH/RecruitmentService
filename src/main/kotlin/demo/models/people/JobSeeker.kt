package demo.models.people

import javax.persistence.*

@Entity
@Table(name = "job_seekers")
data class JobSeeker(
        @Id @Column(name = "id") val id: Int?,
        @OneToOne(cascade = [CascadeType.ALL]) @MapsId @JoinColumn(name = "id") val user: User,
        @OneToMany(cascade = [CascadeType.ALL]) @JoinColumn(name="job_seeker_id") val files: Set<JobSeekerFile>
)