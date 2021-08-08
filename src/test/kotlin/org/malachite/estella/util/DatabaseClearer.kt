package org.malachite.estella.util

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import javax.sql.DataSource

@Component
class DatabaseClearer {

    @Autowired
    private lateinit var datasource: DataSource

    @Bean
    fun clearDatabase() {
        val c: Connection = datasource.connection
        val s: Statement = c.createStatement()

        // Disable FK
        s.execute("SET REFERENTIAL_INTEGRITY FALSE")

        // Find all tables and truncate them
        val tables: MutableSet<String> = HashSet()
        var rs: ResultSet =
            s.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES  where TABLE_SCHEMA='PUBLIC'")
        while (rs.next()) {
            tables.add(rs.getString(1))
        }
        rs.close()
        for (table in tables) {
            s.executeUpdate("TRUNCATE TABLE $table")
        }

        // Idem for sequences
        val sequences: MutableSet<String> = HashSet()
        rs = s.executeQuery("SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA='PUBLIC'")
        while (rs.next()) {
            sequences.add(rs.getString(1))
        }
        rs.close()
        for (seq in sequences) {
            s.executeUpdate("ALTER SEQUENCE $seq RESTART WITH 1")
        }

        // Enable FK
        s.execute("SET REFERENTIAL_INTEGRITY TRUE")
        s.close()
        c.close()
    }
}