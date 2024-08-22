package dmytro.nechytailo.balance.updater.handler

import dmytro.nechytailo.balance.updater.service.UserService
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class UserHandler(private val userService: UserService) {

    fun setUsersBalance(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono(object : ParameterizedTypeReference<Map<Long, Int>>() {})
            .flatMap { userBalances: Map<Long, Int> ->
                userService.updateUserBalances(userBalances)
                    .then(ServerResponse.ok().build())
            }
    }

}