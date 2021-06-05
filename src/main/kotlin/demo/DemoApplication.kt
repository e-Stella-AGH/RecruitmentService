package demo

import com.beust.klaxon.Klaxon
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File

@SpringBootApplication
class DemoApplication

const val applicationPath = "src/main/resources/application.properties"
const val configPath = "config.json"

fun getConfigurationData(): MutableMap<String, String> {
    val file = File(configPath)
    if (!file.exists())
        return HashMap()
    return Klaxon().parse<MutableMap<String, String>>(File(configPath)) ?: HashMap<String, String>()
}

fun getApplicationPropertiesForSql(env: MutableMap<String, String>): String {
    return """
			spring.datasource.url=${env["DATABASE_URL"]}
			spring.jpa.hibernate.ddl-auto=create-drop
			spring.jpa.show-sql=true
			spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
			spring.datasource.driver-class-name=org.postgresql.Driver
		""".trimIndent()
}

fun getApplicationPropertiesForH2(): String {
    return """
        spring.datasource.url=jdbc:h2:file:./myDB
        spring.datasource.username=admin
        spring.datasource.password=admin
        spring.jpa.hibernate.ddl-auto=create-drop
        spring.jpa.show-sql=true
        spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
        spring.datasource.driver-class-name=org.h2.Driver
        spring.datasource.driverClassName=org.h2.Driver
        spring.h2.console.enabled=true
        springdoc.swagger-ui.path=/docs
    """.trimIndent()
}

fun prepareSpringProperties(env: MutableMap<String, String> = getConfigurationData()) {
    val properties = if (env.isNotEmpty()) getApplicationPropertiesForSql(env) else getApplicationPropertiesForH2()
    File(applicationPath).printWriter().use { out ->
        out.println(properties)
    }
}

fun main(args: Array<String>) {
    val env: MutableMap<String, String> = System.getenv()
    if (env.containsKey("DATABASE_URL"))
        prepareSpringProperties(env)
    else
        prepareSpringProperties()
    runApplication<DemoApplication>(*args)
}
