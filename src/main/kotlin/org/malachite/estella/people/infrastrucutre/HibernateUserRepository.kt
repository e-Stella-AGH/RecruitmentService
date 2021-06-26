package org.malachite.estella.people.infrastrucutre

import org.malachite.estella.commons.models.people.User
import org.malachite.estella.people.domain.UserRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HibernateUserRepository: CrudRepository<User, Int>, UserRepository