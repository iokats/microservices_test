package com.ykatsatos.microservices.core.review

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.MySQLContainer
import com.ykatsatos.microservices.core.review.persistence.ReviewEntity
import com.ykatsatos.microservices.core.review.persistence.ReviewRepository

@DataJpaTest
@Transactional(propagation = NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PersistenceTests {
    companion object {
        private val database = MySQLContainer("mysql:latest").apply {
            withDatabaseName("testes")
            withUsername("joao")
            withPassword("12345")
        }

        @BeforeAll
        @JvmStatic
        fun startDBContainer() {
            database.start()
        }

        @AfterAll
        @JvmStatic
        fun stopDBContainer() {
            database.stop()
        }

        @DynamicPropertySource
        @JvmStatic
        fun setProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { database.jdbcUrl }
            registry.add("spring.datasource.username") { database.username }
            registry.add("spring.datasource.password") { database.password }
            registry.add("spring.jpa.hibernate.ddl-auto") { "create" }
        }
    }

    @Autowired
    private lateinit var repository: ReviewRepository

    private lateinit var savedEntity: ReviewEntity

    @BeforeEach
    fun setUpDB() {

        repository.deleteAll()

        val entity = ReviewEntity(1, 2,"author", "subject", "content")
        savedEntity = repository.save(entity)

        assertEqualsReview(entity, savedEntity)
    }

    @Test
    fun create() {

        val newEntity = ReviewEntity(1, 3, "a", "s", "c")
        repository.save(newEntity)

        val foundEntity = repository.findById(newEntity.id!!).get()
        assertEqualsReview(newEntity, foundEntity)

        assertEquals(2, repository.count())
    }

    @Test
    fun update() {
        savedEntity.author = "a2"
        repository.save(savedEntity)

        val foundEntity = repository.findById(savedEntity.id!!).get()

        assertEquals(1, foundEntity.version!!.toLong())
        assertEquals("a2", foundEntity.author)
    }

    @Test
    fun delete() {
        repository.delete(savedEntity)
        assertFalse(repository.existsById(savedEntity.id!!))
    }

    @Test
    fun getByProductId() {
        val entityList = repository.findByProductId(savedEntity.productId)

        assertThat(entityList, hasSize(1))
        assertEqualsReview(savedEntity, entityList[0])
    }

    @Test
    fun duplicateError() {
        assertThrows(DataIntegrityViolationException::class.java) {
            val entity = ReviewEntity(1, 2, "a", "s", "c")
            repository.save(entity)
        }
    }

    @Test
    fun optimisticLockError() {
        // Store the saved entity in two separate entity objects

        val entity1 = repository.findById(savedEntity.id!!).get()
        val entity2 = repository.findById(savedEntity.id!!).get()

        // Update the entity using the first entity object
        entity1.author = "a1"
        repository.save(entity1)

        // Update the entity using the second entity object.
        // This should fail since the second entity now holds an old version number, i.e. an Optimistic Lock Error
        assertThrows(OptimisticLockingFailureException::class.java) {
            entity2.author = "a2"
            repository.save<ReviewEntity>(entity2)
        }

        // Get the updated entity from the database and verify its new sate
        val updatedEntity = repository.findById(savedEntity.id!!).get()
        assertEquals(1, updatedEntity.version as Int)
        assertEquals("a1", updatedEntity.author)
    }

    private fun assertEqualsReview(expectedEntity: ReviewEntity, actualEntity: ReviewEntity) {
        assertEquals(expectedEntity.id, actualEntity.id)
        assertEquals(expectedEntity.version, actualEntity.version)
        assertEquals(expectedEntity.productId, actualEntity.productId)
        assertEquals(expectedEntity.reviewId, actualEntity.reviewId)
        assertEquals(expectedEntity.author, actualEntity.author)
        assertEquals(expectedEntity.subject, actualEntity.subject)
        assertEquals(expectedEntity.content, actualEntity.content)
    }
}