package org.malachite.estella.aplication.domain

import org.malachite.estella.commons.models.offers.Application
import java.util.*

interface ApplicationRepository {
    fun getAllByJobSeekerId(jobSeekerId: Int): List<Application>
    fun save(application: Application): Application
    fun findById(applicationId: Int): Optional<Application>
    fun findAll(): List<Application>
    fun deleteById(applicationId: Int)
}