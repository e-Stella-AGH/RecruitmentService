package demo

import com.beust.klaxon.Klaxon
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File

@SpringBootApplication
class DemoApplication

val application_path = "src/main/resources/application.properties"
val config_path = "config.json"

fun get_configuration_data(): MutableMap<String, String> {
    val result = Klaxon().parse<MutableMap<String, String>>(File(config_path))
    if (result == null)
        return HashMap<String, String>()
    else
        return result
}

fun create_application_properties(env: MutableMap<String, String> = get_configuration_data()) {
    val strategy: String = if (env.isEmpty()) "none" else "create-drop"
    File(application_path).printWriter().use { out ->
        out.println("""
			host=${env["host"]}
			port=5432
			dbname=${env["dbname"]}
			username=${env["username"]}
			password=${env["password"]}
			spring.datasource.url=${env["url"]}
			spring.jpa.hibernate.ddl-auto=${strategy}
			spring.jpa.show-sql=true
			spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
			spring.datasource.driver-class-name=org.postgresql.Driver
		""".trimIndent())
    }
}

fun main(args: Array<String>) {
    val env: MutableMap<String, String> = System.getenv()
    if (env.containsKey("dbname"))
        create_application_properties(env)
    else
        create_application_properties()
    runApplication<DemoApplication>(*args)
}
