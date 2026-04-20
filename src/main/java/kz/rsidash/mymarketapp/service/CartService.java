package kz.rsidash.mymarketapp.service;

import kz.rsidash.mymarketapp.exception.NotFoundException;
import kz.rsidash.mymarketapp.exception.ValidationException;
import kz.rsidash.mymarketapp.model.cart.CartItem;
import kz.rsidash.mymarketapp.model.enums.Action;
import kz.rsidash.mymarketapp.repository.CartItemRepository;
import kz.rsidash.mymarketapp.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;

    public Mono<Map<Long, Integer>> getCartItemsCountMap() {
        return getCartItems()
                .collectMap(
                        CartItem::getItemId,
                        CartItem::getCount
                );
    }

    public Mono<CartItem> getCartItem(long id) {
        return cartItemRepository.findByItemId(id);
    }

    public Flux<CartItem> getCartItems() {
        return cartItemRepository.findAll();
    }

    public Mono<Void> clean() {
        return cartItemRepository.deleteAll();
    }

    public Mono<CartItem> changeItemQuantity(long itemId, Action action) {
        return itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new NotFoundException("Item not found")))
                .flatMap(item ->
                        cartItemRepository.findByItemId(itemId)
                                .defaultIfEmpty(CartItem.builder()
                                        .itemId(item.getId())
                                        .count(0)
                                        .build())
                                .flatMap(cartItem -> {

                                    if (cartItem.getId() == null &&
                                            (action == Action.MINUS || action == Action.DELETE)) {
                                        return Mono.error(
                                                new ValidationException("Cannot modify non-existing cart item")
                                        );
                                    }

                                    if (action == Action.DELETE) {
                                        return cartItemRepository.delete(cartItem)
                                                .thenReturn(cartItem);
                                    }

                                    if (action == Action.PLUS) {
                                        cartItem.setCount(cartItem.getCount() + 1);
                                    } else {
                                        cartItem.setCount(cartItem.getCount() - 1);
                                    }

                                    if (cartItem.getCount() <= 0) {
                                        return cartItemRepository.delete(cartItem)
                                                .thenReturn(cartItem);
                                    }

                                    return cartItemRepository.save(cartItem);
                                })
                );
    }
}
