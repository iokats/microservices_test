package com.ykatsatos.microservices.core.recommendation

import org.springframework.dao.DuplicateKeyException
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
import org.testcontainers.containers.MongoDBContainer
import com.ykatsatos.microservices.core.recommendation.persistence.RecommendationEntity
import com.ykatsatos.microservices.core.recommendation.persistence.RecommendationRepository
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
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
    private lateinit var repository: RecommendationRepository

    private lateinit var savedEntity: RecommendationEntity

    @BeforeEach
    fun setUpDB() = runBlocking {

        repository.deleteAll()

        val entity = RecommendationEntity(1, 2,"author", 3, "content")
        savedEntity = repository.save(entity)

        assertEqualsRecommendation(entity, savedEntity)
    }

    @Test
    fun create()  = runBlocking {
        val newEntity = RecommendationEntity(1, 3, "a", 3, "c")
        repository.save(newEntity)

        val foundEntity = repository.findById(newEntity.id)!!
        assertEqualsRecommendation(newEntity, foundEntity)

        assertEquals(2, repository.count())
    }

    @Test
    fun update()  = runBlocking {
        savedEntity.author = "a2"
        repository.save(savedEntity)

        val foundEntity = repository.findById(savedEntity.id)!!
        assertEquals(1, foundEntity.version!!.toLong())
        assertEquals("a2", foundEntity.author)
    }

    @Test
    fun delete()  = runBlocking {
        repository.delete(savedEntity)
        assertFalse(repository.existsById(savedEntity.id))
    }

    @Test
    fun getByProductId()  = runBlocking {
        val entityList = repository.findByProductId(savedEntity.productId)

        assertEquals(1, entityList.count())
        assertEqualsRecommendation(savedEntity, entityList.first())
    }

    @Test
    fun duplicateError() {
        assertThrows(DuplicateKeyException::class.java) {
            val entity = RecommendationEntity(1, 2, "a", 3, "c")
            runBlocking { repository.save(entity) }
        }
    }

    @Test
    fun optimisticLockError() = runBlocking {
        // Store the saved entity in two separate entity objects

        val entity1 = repository.findById(savedEntity.id)!!
        val entity2 = repository.findById(savedEntity.id)!!

        // Update the entity using the first entity object
        entity1.author = "a1"
        repository.save(entity1)

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds an old version number, i.e. an Optimistic Lock Error
        assertThrows(OptimisticLockingFailureException::class.java) {
            entity2.author = "a2"
            runBlocking { repository.save(entity2) }
        }

        // Get the updated entity from the database and verify its new state
        val updatedEntity = repository.findById(savedEntity.id)!!
        assertEquals(1, updatedEntity.version as Int)
        assertEquals("a1", updatedEntity.author)
    }

    private fun assertEqualsRecommendation(expectedEntity: RecommendationEntity, actualEntity: RecommendationEntity) {
        assertEquals(expectedEntity.id, actualEntity.id)
        assertEquals(expectedEntity.version, actualEntity.version)
        assertEquals(expectedEntity.productId, actualEntity.productId)
        assertEquals(expectedEntity.recommendationId, actualEntity.recommendationId)
        assertEquals(expectedEntity.author, actualEntity.author)
        assertEquals(expectedEntity.rating, actualEntity.rating)
        assertEquals(expectedEntity.content, actualEntity.content)
    }
}