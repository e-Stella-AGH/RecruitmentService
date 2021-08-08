package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.JobSeekerFile
import org.malachite.estella.people.domain.JobSeekerFileNotFoundException
import org.malachite.estella.people.domain.JobSeekerFilePayload
import org.malachite.estella.people.domain.JobSeekerFileRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class JobSeekerFileService(
    @Autowired private val jobSeekerFileRepository: JobSeekerFileRepository,
) : EStellaService<JobSeekerFile>() {

    override val throwable: Exception = JobSeekerFileNotFoundException()

    fun getFile(fileId: Int) = withExceptionThrower { jobSeekerFileRepository.findById(fileId) }

    fun saveFile(file: JobSeekerFile): JobSeekerFile = jobSeekerFileRepository.save(file)

    fun deleteFile(fileId: Int) = jobSeekerFileRepository.deleteById(fileId)

    fun saveFiles(files: List<JobSeekerFilePayload>): List<JobSeekerFile> = files
        .mapNotNull { it.toJobSeekerFile() }
        .map { saveFile(it) }

    fun getOrAddFile(jobSeeker: JobSeeker, files: Set<JobSeekerFilePayload>): List<JobSeekerFile> =
        files.mapNotNull { if (it.id === null) it.toJobSeekerFile()?.let { saveFile(it) } else getFile(it.id) }

    fun deleteFiles(files: List<JobSeekerFile>) =
        files.forEach { deleteFile(it.id!!) }

    fun updateFiles(previousFiles: MutableSet<JobSeekerFile>, updatedFiles: List<JobSeekerFilePayload>): MutableSet<JobSeekerFile> {
        val (oldFilesPayload, newFilesPayloads) = updatedFiles.partition { it.id != null }
        val savedNewFiles = saveFiles(newFilesPayloads)
        val oldFilesIdToStay = oldFilesPayload.map { it.id }

        val oldFiles = previousFiles.map { getFile(it.id!!) }
        val correctOldFiles = oldFiles.filter { oldFilesIdToStay.contains(it.id) }
        val allFiles = correctOldFiles.toSet() + savedNewFiles

        val oldFilesId = correctOldFiles.map { it.id }
        val toDeleteFiles = oldFiles.filterNot { oldFilesId.contains(it.id) }
        deleteFiles(toDeleteFiles)
        return allFiles.toMutableSet()
    }

}