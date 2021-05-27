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
    val file = File(config_path)
    if (!file.exists())
        return HashMap()
    val result = Klaxon().parse<MutableMap<String, String>>(File(config_path))
    if (result == null)
        return HashMap<String, String>()
    else
        return result
}

fun get_application_properties_for_sql(env: MutableMap<String, String>): String {
    return """
			host=${env["host"]}
			port=5432
			dbname=${env["dbname"]}
			username=${env["username"]}
			password=${env["password"]}
			spring.datasource.url=${env["url"]}
			spring.jpa.hibernate.ddl-auto=create-drop
			spring.jpa.show-sql=true
			spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
			spring.datasource.driver-class-name=org.postgresql.Driver
		""".trimIndent()
}

fun get_application_properties_for_h2(): String {
    return """
        spring.datasource.url=jdbc:h2:file:./myDB
        spring.jpa.hibernate.ddl-auto=create-drop
        spring.jpa.show-sql=true
        spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
        spring.datasource.driver-class-name=org.h2.Driver
        spring.datasource.driverClassName=org.h2.Driver
    """.trimIndent()
}

fun prepare_spring_properties(env: MutableMap<String, String> = get_configuration_data()) {
    val properties = if (env.isNotEmpty()) get_application_properties_for_sql(env) else get_application_properties_for_h2()
    File(application_path).printWriter().use { out ->
        out.println(properties)
    }
}

fun main(args: Array<String>) {
    val env: MutableMap<String, String> = System.getenv()
    if (env.containsKey("dbname"))
        prepare_spring_properties(env)
    else
        prepare_spring_properties()
    runApplication<DemoApplication>(*args)
}
