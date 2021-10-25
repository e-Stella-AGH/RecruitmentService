package org.malachite.estella.aplication.domain

import org.malachite.estella.commons.models.offers.ApplicationStageData
import java.util.*

interface ApplicationStageRepository {
    fun save(application: ApplicationStageData): ApplicationStageData
    fun findById(applicationId: Int): Optional<ApplicationStageData>
    fun findAll(): List<ApplicationStageData>
    fun deleteById(applicationId: Int)
}