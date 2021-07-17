package org.malachite.estella.process.recruitmentStage

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.services.RecruitmentStageService
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class RecruitmentStageServiceTest {

    private val repository = DummyRecruitmentStageRepository()
    private val service = RecruitmentStageService(repository)

    @AfterEach
    fun setup() {
        repository.clear()
    }

    @Test
    fun `should be able to add and find stage`() {
        val stage = service.findOrCreate(StageType.APPLIED)
        expectThat(stage.id).isNotNull()
        expectThat(stage.type).isEqualTo(StageType.APPLIED)
    }

    @Test
    fun `should find stage if there's one and not adding another`() {
        //given - dummy value in repository
        repository.add(RecruitmentStage(id = 1000, type = StageType.HR_INTERVIEW))

        val stage = service.findOrCreate(StageType.HR_INTERVIEW)
        expectThat(stage.id).isEqualTo(1000)
        expectThat(stage.type).isEqualTo(StageType.HR_INTERVIEW)
        expectThat(repository.size()).isEqualTo(1)
    }

}