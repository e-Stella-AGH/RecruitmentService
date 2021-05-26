package demo.models.people

import javax.persistence.*

@Entity
@Table(name = "Organizations")
data class Organization(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: String?,
        val name: String,
        val verified: Boolean?
)
