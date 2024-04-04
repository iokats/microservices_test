package com.ykatsatos.microservices.core.product

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.dao.DuplicateKeyException
import org.testcontainers.containers.MongoDBContainer
import com.ykatsatos.microservices.core.product.persistence.ProductEntity
import com.ykatsatos.microservices.core.product.persistence.ProductRepository
import kotlinx.coroutines.runBlocking

@DataMongoTest
class PersistenceTests {
    companion object {
        private val database = MongoDBContainer("mongo:6.0.4")

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
            registry.add("spring.data.mongodb.uri", database::getReplicaSetUrl)
            registry.add("spring.data.mongodb.auto-index-creation") { true }
        }
    }

    @Autowired
    private lateinit var repository: ProductRepository

    private lateinit var savedEntity: ProductEntity

    @BeforeEach
    fun setUpDB() = runBlocking {

        repository.deleteAll()

        val entity = ProductEntity(1,"name", 123)
        savedEntity = repository.save(entity)

        assertEqualsProduct(entity, savedEntity)
    }

    @Test
    fun create() = runBlocking {

        val newEntity = ProductEntity(2, "name", 125)
        repository.save(newEntity)

        val foundEntity = repository.findById(newEntity.id)!!
        assertEqualsProduct(newEntity, foundEntity)

        assertEquals(2, repository.count())
    }

    @Test
    fun update() = runBlocking {

        savedEntity.name = "name-2"
        repository.save(savedEntity)

        val foundEntity = repository.findById(savedEntity.id)!!
        assertEquals(1, foundEntity.version)
        assertEquals("name-2", foundEntity.name)
    }

    @Test
    fun delete() = runBlocking {

        repository.delete(savedEntity)
        assertFalse(repository.existsById(savedEntity.id))
    }

    @Test
    fun getByProductId() = runBlocking {

        val entity = repository.findByProductId(savedEntity.productId)

        assertNotNull(entity)
        assertEqualsProduct(savedEntity, entity!!)
    }

    @Test
    fun duplicateError() {
        assertThrows(DuplicateKeyException::class.java) {
            val entity = ProductEntity(savedEntity.productId, "n", 1)
            runBlocking {  repository.save(entity) }
        }
    }

    @Test
    fun optimisticLockError() = runBlocking {

        // Store the saved entity in two separate entity objects
        val entity1 = repository.findById(savedEntity.id)!!
        val entity2 = repository.findById(savedEntity.id)!!

        // Update the entity using the first entity object
        entity1.name = "n1"
        repository.save(entity1)

        // Update the entity using the second entity object.
        // This should fail since the second entity now holds an old version number, i.e. an Optimistic Lock Error
        assertThrows(OptimisticLockingFailureException::class.java) {
            entity2.name = "n2"
            runBlocking { repository.save(entity2) }
        }

        // Get the updated entity from the database and verify its new state
        val updatedEntity = repository.findById(savedEntity.id)!!
        assertEquals(1, updatedEntity.version as Int)
        assertEquals("n1", updatedEntity.name)
    }

    private fun assertEqualsProduct(expectedEntity: ProductEntity, actualEntity: ProductEntity) {

        assertEquals(expectedEntity.id, actualEntity.id)
        assertEquals(expectedEntity.version, actualEntity.version)
        assertEquals(expectedEntity.productId, actualEntity.productId)
        assertEquals(expectedEntity.name, actualEntity.name)
        assertEquals(expectedEntity.weight, actualEntity.weight)
    }
}