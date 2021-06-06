package demo.models.people

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Table
import javax.persistence.Id;

@Entity
@Table(name="users")
data class User(@Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
                val firstName: String, val lastName: String,
                val mail: String, val password: String?) {
}
