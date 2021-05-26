package demo.models.people

import javax.persistence.*

@Entity
@Table(name = "JobSeeker")
class JobSeeker(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        @OneToOne val user:User,
        @OneToMany @JoinTable(name = "JobSeekerToFiles") val files: Set<JobSeekerFile>
)