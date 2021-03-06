package org.malachite.estella.commons.models.people

import com.fasterxml.jackson.annotation.JsonIgnore
import org.malachite.estella.commons.models.tasks.Task
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "organizations")
data class Organization(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: UUID?,
        val name: String,
        @OneToOne(cascade = [CascadeType.ALL]) val user: User,
        val verified: Boolean = false,
        @JsonIgnore @OneToMany(fetch = FetchType.EAGER) @JoinColumn(name="organization_id")
        val tasks: Set<Task> = HashSet<Task>(),
)
