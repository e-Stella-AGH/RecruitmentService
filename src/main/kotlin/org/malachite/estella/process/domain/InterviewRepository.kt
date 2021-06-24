package org.malachite.estella.process.domain

import org.malachite.estella.commons.models.interviews.Interview

interface InterviewRepository {
    fun save(interview: Interview): Interview
}