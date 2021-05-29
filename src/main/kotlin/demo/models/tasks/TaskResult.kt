package demo.models.tasks

import demo.models.offers.Application
import java.sql.Blob
import java.sql.Clob
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "tasks_results")
data class TaskResult(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        @Lob val results:Blob, @Lob val code:Clob,
        val startTime:Timestamp?, val endTime:Timestamp,
        @ManyToOne val application: Application
)
