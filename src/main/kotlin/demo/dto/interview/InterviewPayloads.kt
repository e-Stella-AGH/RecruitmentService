package demo.dto.interview

import java.sql.Timestamp
import java.time.Instant

data class InterviewPayloads(val dateTime: Timestamp = Timestamp.from(Instant.now()),
                             val minutesLength: Int = 30)
