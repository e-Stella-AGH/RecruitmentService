package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.JobSeekerFile
import org.malachite.estella.people.domain.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class JobSeekerFileService(
    @Autowired private val jobSeekerFileRepository: JobSeekerFileRepository,
    @Autowired private val jobSeekerService: JobSeekerService
) : EStellaService<JobSeekerFile>() {

    override val throwable: Exception = JobSeekerFileNotFoundException()

    fun getFile(fileId: Int) = withExceptionThrower { jobSeekerFileRepository.findById(fileId) }

    fun saveFile(file: JobSeekerFile): JobSeekerFile = jobSeekerFileRepository.save(file)

    fun deleteFile(fileId: Int) = jobSeekerFileRepository.deleteById(fileId)

    fun updateFileList(jobSeeker: JobSeeker, files: List<JobSeekerFilePayload>) {
        val (oldFilesPayload, newFiles) = files.partition { it.id != null }
        val savedNewFiles = newFiles
            .mapNotNull { it.toJobSeekerFile() }
            .map { saveFile(it) }
        val oldFilesIdToStay = oldFilesPayload.map { it.id }
        val oldFiles = jobSeeker.files.map { getFile(it.id!!) }
        val correctOldFiles = oldFiles.filter { oldFilesIdToStay.contains(it.id) }
        val allFiles = correctOldFiles.toSet() + savedNewFiles
        jobSeekerService.updateJobSeeker(jobSeeker.copy(files = allFiles))
        val oldFilesId = correctOldFiles.map { it.id }
        val toDeleteFiles = oldFiles.filterNot { oldFilesId.contains(it.id) }
        toDeleteFiles.forEach { deleteFile(it.id!!) }
    }

}