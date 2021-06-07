package demo.services

import demo.models.offers.RecruitmentProcess
import demo.repositories.RecruitmentProcessRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RecruitmentProcessService(@Autowired private val recruitmentProcessRepository: RecruitmentProcessRepository) {

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

}