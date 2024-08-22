package dmytro.nechytailo.balance.updater

import dmytro.nechytailo.balance.updater.model.User
import dmytro.nechytailo.balance.updater.repository.UserRepository
import org.slf4j.LoggerFactory



import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.StopWatch
import reactor.core.publisher.Mono
import java.util.concurrent.ThreadLocalRandom

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserHandlerIntegrationTest(
    @LocalServerPort val port: Int,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val databaseClient: DatabaseClient
) {

    private val logger = LoggerFactory.getLogger("TestLogger")

    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setUp() {
        cleanDatabase()
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
    }

    @Test
    fun `should update one million user balances`() {
        val initialUsers = createUsers(1_000_000)
        val savedUsers = saveUsers(initialUsers)

        savedUsers?.let {
            val updatedBalances = updateBalances(it)
            verifyUpdatedBalances(it, updatedBalances)
        } ?: error("No users were saved.")
    }

    private fun cleanDatabase() {
        databaseClient.sql("DELETE FROM users").then().block()
    }

    private fun createUsers(count: Int): List<User> {
        return (1..count).map { User(id = null, name = "User$it", balance = 0) }
    }

    private fun saveUsers(users: List<User>): List<User>? {
        return userRepository.saveAll(users).collectList().block()
    }

    private fun updateBalances(users: List<User>): Map<Long?, Int> {
        val updatedBalances = users.associate { it.id to generateRandomBalance() }

        val stopWatch = StopWatch()
        stopWatch.start()

        webTestClient.post()
            .uri("/set-users-balance")
            .body(Mono.just(updatedBalances), Map::class.java)
            .exchange()
            .expectStatus().isOk

        stopWatch.stop()
        logger.info("Request to /set-users-balance took ${stopWatch.totalTimeMillis} ms")

        return updatedBalances
    }

    private fun verifyUpdatedBalances(savedUsers: List<User>, updatedBalances: Map<Long?, Int>) {
        savedUsers.forEach { user ->
            userRepository.findById(user.id!!).block()?.let { updatedUser ->
                val expectedBalance = updatedBalances[user.id]
                if (expectedBalance == updatedUser.balance) {
                    //logger.info("User ID: ${user.id} balance is correctly updated to $expectedBalance")
                } else {
                    logger.error("User ID: ${user.id} has balance $expectedBalance but found ${updatedUser.balance}")
                }
                assert(expectedBalance == updatedUser.balance) {
                    "User ID: ${user.id} has balance $expectedBalance but found ${updatedUser.balance}"
                }
            }
        }
    }

    private fun generateRandomBalance(): Int {
        return ThreadLocalRandom.current().nextInt(0, 10000)
    }
}
