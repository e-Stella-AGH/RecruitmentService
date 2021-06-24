package org.malachite.estella.people.infrastrucutre

import org.malachite.estella.commons.models.people.User
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: CrudRepository<User, Int> {
}