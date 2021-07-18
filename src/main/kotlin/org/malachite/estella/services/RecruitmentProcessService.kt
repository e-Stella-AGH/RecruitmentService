package org.malachite.estella.services

import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.offers.RecruitmentProcess
import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.process.domain.NoSuchStageTypeException
import org.malachite.estella.process.domain.RecruitmentProcessRepository
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

    fun updateProcess(id: Int, process: RecruitmentProcess) {
        val currProcess: RecruitmentProcess = getProcess(id)
        val updated: RecruitmentProcess = currProcess.copy(
            id = process.id,
            startDate = process.startDate,
            endDate = process.endDate,
            offer = process.offer,
            stages = process.stages,
            quizzes = process.quizzes,
            tasks = process.tasks
        )
        recruitmentProcessRepository.save(updated)
    }

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
        val userFromJWT = securityService.getHrPartnerFromJWT(jwt)
        val process = getProcess(processId)
        if (process.offer.creator.id != userFromJWT?.id) throw UnauthenticatedException()
        val stages = updateStagesList(process.stages, stagesList.toListOfRecruitmentStage())
        recruitmentProcessRepository.save(process.copy(stages = stages))
    }

    private fun updateStagesList(oldStages: List<RecruitmentStage>, newStages: List<RecruitmentStage>): List<RecruitmentStage> {
        val stages: MutableList<RecruitmentStage> = mutableListOf()
        stages += oldStages.zip(newStages).map {
            if(it.first.type != it.second.type)
                recruitmentStageService.save(it.first.copy(type = it.second.type))
            else it.first
        }
        oldStages.drop(newStages.size).map { recruitmentStageService.delete(it) }
        stages += newStages.drop(oldStages.size).map { recruitmentStageService.save(it) }
        return stages
    }

    private fun List<String>.toListOfRecruitmentStage() =
        this.toMutableList()
            .map {
                try {
                    StageType.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    throw NoSuchStageTypeException(it)
                }
            }
            .map { RecruitmentStage(null, it) }

}