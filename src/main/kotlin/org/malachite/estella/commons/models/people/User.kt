package org.malachite.estella.commons.models.people

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import javax.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
    val firstName: String, val lastName: String,
    @Column(unique = true) val mail: String
) {
    constructor(id: Int?, firstName: String, lastName: String, mail: String, password: String?) : this(
        id,
        firstName,
        lastName,
        mail
    ) {
        this.password = password ?: ""
    }

    var password: String = ""
        @JsonIgnore
        get() = field
        set(value) {
            val passwordEncoder = BCryptPasswordEncoder()
            field = passwordEncoder.encode(value)
        }


    fun comparePassword(password: String): Boolean =
        BCryptPasswordEncoder().matches(password, this.password)

}
