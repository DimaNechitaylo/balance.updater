package dmytro.nechytailo.balance.updater.service

import dmytro.nechytailo.balance.updater.repository.UserRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class UserService(private val userRepository: UserRepository) {

    fun updateUserBalances(userBalances: Map<Long, Int>): Mono<Void> {
        return Flux.fromIterable(userBalances.entries)
            .flatMap { (id, balance) ->
                userRepository.findById(id)
                    .flatMap { user ->
                        user.balance = balance
                        userRepository.save(user)
                    }
            }
            .then()
    }

}