package kz.rsidash.mymarketapp.repository;

import kz.rsidash.mymarketapp.model.cart.CartItem;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CartItemRepository extends ReactiveCrudRepository<CartItem, Long> {
    Mono<CartItem> findByItemId(Long itemId);
    Flux<CartItem> findAllByCartId(Long cartId);
}
