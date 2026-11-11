package com.choreemon.backend.db

import com.choreemon.backend.config.AppConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.sql.DriverManager

object DatabaseFactory {
    fun init(config: AppConfig) {
        val db = config.db
        val encrypt = if (config.isAzure) "true" else "false"
        val trustCert = if (config.isAzure) "false" else "true"
        val jdbcUrl = "jdbc:sqlserver://${db.host}:${db.port};" +
            "databaseName=${db.name};encrypt=$encrypt;trustServerCertificate=$trustCert"

        // Create DB if it doesn't exist (local dev only — Azure SQL DB is pre-created)
        if (!config.isAzure) createDatabaseIfNeeded(db.host, db.port, db.name, db.user, db.password)

        // Run Flyway migrations before opening the connection pool
        Flyway.configure()
            .dataSource(jdbcUrl, db.user, db.password)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load()
            .migrate()

        val hikari = HikariConfig().apply {
            this.jdbcUrl         = jdbcUrl
            driverClassName      = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
            username             = db.user
            password             = db.password
            maximumPoolSize      = 10
            isAutoCommit         = false
            transactionIsolation = "TRANSACTION_READ_COMMITTED"
            validate()
        }
        Database.connect(HikariDataSource(hikari))
    }

    private fun createDatabaseIfNeeded(host: String, port: Int, name: String, user: String, password: String) {
        val masterUrl = "jdbc:sqlserver://$host:$port;databaseName=master;encrypt=false;trustServerCertificate=true"
        try {
            DriverManager.getConnection(masterUrl, user, password).use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeUpdate(
                        "IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'$name') CREATE DATABASE [$name]"
                    )
                }
            }
        } catch (e: Exception) {
            // DB may already exist or not be accessible — Flyway will surface the real error.
        }
    }
}

/** Convenience wrapper: run a DB block on the IO dispatcher. */
suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
