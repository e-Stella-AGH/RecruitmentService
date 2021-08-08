package org.malachite.estella.services

import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.offers.RecruitmentProcess
import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.process.domain.InvalidEndDateException
import org.malachite.estella.process.domain.InvalidStagesListException
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
            listOf(RecruitmentStage(null, StageType.APPLIED), RecruitmentStage(null, StageType.ENDED)),
            setOf(), setOf()
        )
        recruitmentProcessRepository.save(recruitmentProcess)
    }

    fun getProcessFromStage(recruitmentProcessStage: RecruitmentStage): RecruitmentProcess {
        return recruitmentProcessRepository.findAll().find { it.stages.contains(recruitmentProcessStage) }!!
    }

    fun updateStagesList(processId: Int, stagesList: List<String>) {
        val user = securityService.getHrPartnerFromContext()
        val process = getProcess(processId)
        if (process.offer.creator.id != user?.id) throw UnauthenticatedException()
        val stages = compareAndGetStagesList(process.stages, stagesList.toListOfRecruitmentStage())
        recruitmentProcessRepository.save(process.copy(stages = stages))
    }

    private fun compareAndGetStagesList(oldStages: List<RecruitmentStage>, newStages: List<RecruitmentStage>): List<RecruitmentStage> {
        if(newStages[0].type != StageType.APPLIED || newStages.last().type != StageType.ENDED) throw InvalidStagesListException()
        val stages: MutableList<RecruitmentStage> = mutableListOf()
        stages += oldStages.zip(newStages).map {
            if(it.first.type != it.second.type)
                recruitmentStageService.save(it.first.copy(type = it.second.type))
            else it.first
        }
        oldStages.drop(newStages.size).map { recruitmentStageService.delete(it) }
        stages += newStages.drop(oldStages.size).map { recruitmentStageService.save(it) }
        if(stages.count { it.type == StageType.APPLIED } > 1 || stages.count { it.type == StageType.ENDED } > 1)
            throw InvalidStagesListException("There must be only one APPLIED and ENDED stage")
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

    fun updateEndDate(processId: Int, endDate: Date) {
        val userFromJWT = securityService.getHrPartnerFromContext()
        val process = getProcess(processId)
        if (process.offer.creator.id != userFromJWT?.id) throw UnauthenticatedException()
        if(process.startDate.after(endDate)) throw InvalidEndDateException()
        recruitmentProcessRepository.save(process.copy(endDate = endDate))
    }

}