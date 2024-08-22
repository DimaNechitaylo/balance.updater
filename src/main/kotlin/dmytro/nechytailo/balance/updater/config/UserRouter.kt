package dmytro.nechytailo.balance.updater.config

import dmytro.nechytailo.balance.updater.handler.UserHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.*

@Configuration
class UserRouter(private val userHandler: UserHandler) {

    @Bean
    fun route() = router {
        POST("/set-users-balance", userHandler::setUsersBalance)
    }
}