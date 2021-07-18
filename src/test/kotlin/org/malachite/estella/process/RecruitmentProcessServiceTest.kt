package org.malachite.estella.process

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.malachite.estella.commons.loader.FakeRecruitmentProcess.getProcesses
import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.services.RecruitmentProcessService
import org.malachite.estella.services.RecruitmentStageService
import org.malachite.estella.services.SecurityService
import org.malachite.estella.util.offersWithNullProcess
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class RecruitmentProcessServiceTest {

    private val stagesRepository = DummyStageRepository()
    private val processRepository = DummyProcessRepository(stagesRepository)
    private val stageService = RecruitmentStageService(stagesRepository)
    private val securityMock = mockk<SecurityService>()
    private val service = RecruitmentProcessService(processRepository, securityMock, stageService)

    @BeforeEach
    fun setup() {
        processRepository.clear()
        stagesRepository.clear()
        every { securityMock.getHrPartnerFromJWT(any()) } returns HrPartner(
            null,
            Organization(null, "xd", User(1, "xd", "xd", "xd")),
            User(1, "xd", "xd", "xd")
        )
        processRepository.save(process)
        process.stages.forEach { stagesRepository.save(it) }
    }

    @Test
    fun `should be able to update stages when they are the same length`() {
        service.updateStagesList(
            null,
            process.id!!,
            listOf("APPLIED", "HR_INTERVIEW", "HR_INTERVIEW"))
        val updatedProcess = service.getProcess(process.id!!)
        expectThat(updatedProcess.stages).isEqualTo(listOf(
            RecruitmentStage(0, StageType.APPLIED),
            RecruitmentStage(1, StageType.HR_INTERVIEW),
            RecruitmentStage(2, StageType.HR_INTERVIEW)
        ))
        expectThat(updatedProcess.stages.size).isEqualTo(3)
    }

    @Test
    fun `should be able to add new stage while updating`() {
        service.updateStagesList(
            null,
            process.id!!,
            listOf("APPLIED", "HR_INTERVIEW", "TECHNICAL_INTERVIEW", "TECHNICAL_INTERVIEW")
        )
        val updatedProcess = service.getProcess(process.id!!)
        expectThat(updatedProcess.stages).isEqualTo(listOf(
            RecruitmentStage(0, StageType.APPLIED),
            RecruitmentStage(1, StageType.HR_INTERVIEW),
            RecruitmentStage(2, StageType.TECHNICAL_INTERVIEW),
            RecruitmentStage(3, StageType.TECHNICAL_INTERVIEW)
        ))
        expectThat(updatedProcess.stages.size).isEqualTo(4)
    }

    @Test
    fun `should be able to delete stage while updating`() {
        service.updateStagesList(
            null,
            process.id!!,
            listOf("APPLIED", "HR_INTERVIEW")
        )
        val updatedProcess = service.getProcess(process.id!!)
        expectThat(updatedProcess.stages).isEqualTo(listOf(
            RecruitmentStage(0, StageType.APPLIED),
            RecruitmentStage(1, StageType.HR_INTERVIEW)
        ))
        expectThat(updatedProcess.stages.size).isEqualTo(2)
    }

    private val process = getProcesses(offersWithNullProcess)[0].let {
        it.copy(stages = it.stages.mapIndexed { index, recruitmentStage -> recruitmentStage.copy(id = index) })
    }
}