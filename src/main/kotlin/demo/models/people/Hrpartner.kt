package demo.models.people

import javax.persistence.*

@Entity
@Table(name = "hrpartners")
data class HrPartner(
        @Id @Column(name = "id") val id: Int?,
        @ManyToOne @JoinColumn(name = "organization_id") val organization: Organization,
        @OneToOne(cascade = [CascadeType.ALL]) @MapsId @JoinColumn(name = "id") val user: User
//        @OneToMany() val offers:
)
