package org.malachite.estella

import com.beust.klaxon.Klaxon
import java.io.File

const val applicationPath = "src/main/resources/application.properties"
const val configPath = "config2.json"

fun getConfigurationData(): MutableMap<String, String> {
    val file = File(configPath)
    if (!file.exists())
        return HashMap()
    return Klaxon().parse<MutableMap<String, String>>(File(configPath)) ?: HashMap<String, String>()
}

fun getApplicationPropertiesForSql(env: MutableMap<String, String>): String {
    return """
			spring.datasource.url=${env["DATABASE_URL"]}
            spring.jpa.generate-ddl=true
            spring.jpa.hibernate.ddl-auto=create
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
        spring.jpa.generate-ddl=true
        spring.jpa.hibernate.ddl-auto=create
        spring.jpa.show-sql=true
        spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
        spring.datasource.driver-class-name=org.h2.Driver
        spring.datasource.driverClassName=org.h2.Driver
        spring.h2.console.enabled=true
        springdoc.swagger-ui.path=/docs
    """.trimIndent()
}

fun prepareSpringProperties(env: MutableMap<String, String> = getConfigurationData()) {
    var properties = if (env.isNotEmpty()) getApplicationPropertiesForSql(env) else getApplicationPropertiesForH2()
    properties += "\n" + getOtherApplicationProperties()
    File(applicationPath).printWriter().use { out ->
        out.println(properties)
    }
}

fun getApiKey():String=
    System.getenv().getOrDefault("API_KEY","API_KEY")

fun getOtherApplicationProperties(): String = """
    mail_service_url=https://email-service-estella.herokuapp.com/
    admin_api_key=${getApiKey()}
    should_fake_load=true
""".trimIndent()