package org.malachite.estella.commons.models.people

import com.fasterxml.jackson.annotation.JsonIgnore
import org.malachite.estella.commons.models.quizes.Quiz
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
        @JsonIgnore @ManyToMany @JoinTable(
                name = "organization_quizes",
                joinColumns = [JoinColumn(name = "organization_id")],
                inverseJoinColumns = [JoinColumn(name = "quizes_id")]
        ) val quizzes: Set<Quiz> = HashSet<Quiz>(),
        @JsonIgnore @ManyToMany @JoinTable(
                name = "organization_tasks",
                joinColumns = [JoinColumn(name = "organization_id")],
                inverseJoinColumns = [JoinColumn(name = "tasks_id")]
        ) val tasks: Set<Task> = HashSet<Task>(),
)
