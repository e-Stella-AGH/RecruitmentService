package demo.models.people

import javax.persistence.*

@Entity
@Table(name = "Hrpartners")
data class Hrpartner(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        @ManyToOne @JoinColumn(name = "organization_id") val organization: Organization,
        @OneToOne val user: User
//        @OneToMany() val offers:
)
