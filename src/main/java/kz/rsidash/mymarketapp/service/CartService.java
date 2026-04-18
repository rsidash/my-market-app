package kz.rsidash.mymarketapp.service;

import jakarta.transaction.Transactional;
import kz.rsidash.mymarketapp.exception.NotFoundException;
import kz.rsidash.mymarketapp.exception.ValidationException;
import kz.rsidash.mymarketapp.model.cart.CartItem;
import kz.rsidash.mymarketapp.model.enums.Action;
import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.repostitory.CartItemRepository;
import kz.rsidash.mymarketapp.repostitory.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;

    public Map<Long, Integer> getCartItemsCountMap() {
        return getCartItems().stream()
                .collect(
                        Collectors.toMap(
                                CartItem::getId,
                                CartItem::getCount
                        )
                );
    }

    public Optional<CartItem> getCartItem(long id) {
        return cartItemRepository.findByItemId(id);
    }

    public List<CartItem> getCartItems() {
        return cartItemRepository.findAll();
    }

    public void clean() {
        cartItemRepository.deleteAll();
    }

    @Transactional
    public CartItem changeItemQuantity(long itemId, Action action) {
        final Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        CartItem cartItem = cartItemRepository.findByItemId(itemId).orElse(null);

        if (Objects.isNull(cartItem) && (Action.MINUS == action || Action.DELETE == action)) {
            throw new ValidationException("Cannot modify non-existing cart item");
        }

        if (action == Action.DELETE) {
            cartItemRepository.delete(cartItem);
            return cartItem;
        }

        cartItem = Objects.nonNull(cartItem) ?
                cartItem :
                CartItem.builder()
                        .item(item)
                        .count(0)
                        .build();

        if (action == Action.PLUS) {
            cartItem.setCount(cartItem.getCount() + 1);
        } else {
            cartItem.setCount(cartItem.getCount() - 1);
        }

        if (cartItem.getCount() <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItemRepository.save(cartItem);
        }

        return cartItem;
    }
}
