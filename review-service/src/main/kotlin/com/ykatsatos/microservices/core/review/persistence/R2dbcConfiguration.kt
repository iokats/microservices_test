package com.ykatsatos.microservices.core.review.persistence

import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import org.springframework.r2dbc.core.DatabaseClient

/**
 * We want our app to create a table in the database on startup. However, R2DBC doesn't support this functionality
 * because it lacks the rich set of annotations for describing entities, as seen in Spring Data JPA. Therefore, the
 * responsibility falls on us to define the database schema for each entity.
 *
 * With R2DBC we need to add a schema.sql file within the 'resources'. If we want to override the default location of the
 * schema.sql file, ike in our case (data/schema.sql), we have to use the following fragment of code for getting the
 * schema from the new location of the schema.sql file.
 */
@Configuration
class R2dbcConfiguration (@Autowired private val connectionFactory: ConnectionFactory) : AbstractR2dbcConfiguration() {

    @Bean
    fun connectionFactoryInitializer(databaseClient: DatabaseClient): ConnectionFactoryInitializer {

        return ConnectionFactoryInitializer().apply {

            setConnectionFactory(connectionFactory())

            setDatabasePopulator(ResourceDatabasePopulator(ClassPathResource("data/schema.sql")))
        }
    }

    /**
     *  We return the default {@link ConnectionFactory}. We could create our custom {@link ConnectionFactory} by using
     *  the information provided by the 'r2dbc-mysql' library.
     *
     *  @see <a href="https://github.com/asyncer-io/r2dbc-mysql/wiki/getting-started">r2dbc-mysql Getting Started</a>
     */
    override fun connectionFactory(): ConnectionFactory = connectionFactory
}