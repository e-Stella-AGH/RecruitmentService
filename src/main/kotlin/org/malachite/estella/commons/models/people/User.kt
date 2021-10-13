package org.malachite.estella.commons.models.people

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import javax.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
    var firstName: String, var lastName: String,
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

    /*
        TODO [ES-187] - User should be data class, final form of password should be passed to default constructor
                        Encryption logic in setter is also very confusing
     */
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
