package org.malachite.estella.services

import org.malachite.estella.commons.models.interviews.Note
import org.malachite.estella.commons.models.interviews.Tag
import org.malachite.estella.interview.api.NotesFilePayload
import org.malachite.estella.interview.domain.NoteRepository
import org.malachite.estella.interview.domain.TagRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Clob
import java.util.*
import javax.sql.rowset.serial.SerialClob

@Service
class NoteService(
    @Autowired private val noteRepository: NoteRepository,
    @Autowired private val tagRepository: TagRepository,
) {

    private fun String.toClob(): Clob =
        SerialClob(String(Base64.getDecoder().decode(this)).toCharArray())

    private fun String.toTag(): Tag =
        tagRepository.findByText(this).orElse(createTag(this))

    fun createTag(text:String):Tag =
        tagRepository.save(Tag(null,text))

    private fun NotesFilePayload.toNote(): Note =
        Note(this.id,
            this.author,
            this.tags.map{it.toTag()}.toSet(),
            this.fileBase64.toClob())

    fun updateNotes(notes: Set<NotesFilePayload>): MutableSet<Note> =
        notes.map { noteRepository.save(it.toNote()) }.toMutableSet()



}