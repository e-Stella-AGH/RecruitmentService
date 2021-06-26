package org.malachite.estella.process.domain

import java.sql.Timestamp
import java.time.Instant

data class InterviewPayloads(val dateTime: Timestamp = Timestamp.from(Instant.now()),
                             val minutesLength: Int = 30)
