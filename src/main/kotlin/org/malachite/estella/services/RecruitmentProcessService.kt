package org.malachite.estella.services

import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.offers.RecruitmentProcess
import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.process.domain.RecruitmentProcessRepository
import org.malachite.estella.process.infrastructure.HibernateRecruitmentProcessRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Date
import java.time.LocalDate

@Service
class RecruitmentProcessService(
    @Autowired private val recruitmentProcessRepository: RecruitmentProcessRepository,
    @Autowired private val securityService: SecurityService,
    @Autowired private val recruitmentStageService: RecruitmentStageService
) {

    fun getProcesses(): MutableIterable<RecruitmentProcess> =
        recruitmentProcessRepository.findAll()

    fun getProcess(id: Int): RecruitmentProcess = recruitmentProcessRepository.findById(id).get()

    fun addProcess(process: RecruitmentProcess): RecruitmentProcess = recruitmentProcessRepository.save(process)

    //TODO - nie ma opcji, że działa
//    fun updateProcess(id: Int, process: RecruitmentProcess) {
//        val currProcess: RecruitmentProcess = getProcess(id)
//        val updated: RecruitmentProcess = currProcess.copy(
//            id = process.id,
//            startDate = process.startDate,
//            endDate = process.endDate,
//            offer = process.offer,
//            stages = process.stages,
//            quizzes = process.quizzes,
//            tasks = process.tasks
//        )
//    }

    fun deleteProcess(id: Int) = recruitmentProcessRepository.deleteById(id)

    fun addBasicProcess(offer: Offer) {
        val recruitmentProcess = RecruitmentProcess(
            null,
            Date.valueOf(LocalDate.now()),
            null,
            offer,
            listOf(RecruitmentStage(null, StageType.APPLIED)),
            setOf(), setOf()
        )
        recruitmentProcessRepository.save(recruitmentProcess)
    }

    fun getProcessFromStage(recruitmentProcessStage: RecruitmentStage): RecruitmentProcess {
        return recruitmentProcessRepository.findAll().find { it.stages.contains(recruitmentProcessStage) }!!
    }

    fun updateStagesList(jwt: String?, processId: Int, stagesList: List<String>) {
        val userFromJWT = securityService.getUserFromJWT(jwt)
        val process = getProcess(processId)
        if(process.offer.creator.user.id != userFromJWT?.id) throw UnauthenticatedException()
        val updated = process.copy(stages = stagesList.toListOfStageType().map { it.toRecruitmentStage() })
        recruitmentProcessRepository.save(updated)
    }

    private fun List<String>.toListOfStageType() =
        this.toMutableList()
            .map { StageType.valueOf(it) }

    private fun StageType.toRecruitmentStage() =
        recruitmentStageService.findOrCreate(this)

}