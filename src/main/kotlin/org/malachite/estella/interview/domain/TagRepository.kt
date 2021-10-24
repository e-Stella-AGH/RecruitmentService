package org.malachite.estella.interview.domain

import org.malachite.estella.commons.models.interviews.Tag
import java.util.*

interface TagRepository {
    fun findById(id: Int): Optional<Tag>
    fun findByText(text:String): Optional<Tag>
    fun deleteById(id: Int)
    fun save(interviewNote: Tag): Tag
}