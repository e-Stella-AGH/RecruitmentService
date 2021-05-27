package demo.models.interviews

import demo.models.offers.Application
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "Interviews")
data class Interview(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: String?,
        val dateTime:Timestamp, val minutesLength:Int,
        @ManyToOne val application:Application,
        @OneToMany val notes:Set<InterviewNote>

)