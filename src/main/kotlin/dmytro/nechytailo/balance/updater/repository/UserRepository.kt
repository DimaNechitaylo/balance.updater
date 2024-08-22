package dmytro.nechytailo.balance.updater.repository

import dmytro.nechytailo.balance.updater.model.User
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface UserRepository : ReactiveCrudRepository<User, Long> { }