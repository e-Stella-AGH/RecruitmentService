package demo.models.people

import javax.persistence.*

@Entity
@Table(name = "hrpartners")
data class HrPartner(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        @ManyToOne @JoinColumn(name = "organization_id") val organization: Organization,
        @OneToOne val user: User
//        @OneToMany() val offers:
)
