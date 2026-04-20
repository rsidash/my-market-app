package kz.rsidash.mymarketapp.service;

import kz.rsidash.mymarketapp.exception.NotFoundException;
import kz.rsidash.mymarketapp.exception.ValidationException;
import kz.rsidash.mymarketapp.model.cart.CartItem;
import kz.rsidash.mymarketapp.model.enums.Action;
import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.repository.CartItemRepository;
import kz.rsidash.mymarketapp.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void getCartItems_returnsAll() {
        var cartItem = CartItem.builder().id(1L).itemId(1L).count(2).build();
        when(cartItemRepository.findAll()).thenReturn(Flux.just(cartItem));

        StepVerifier.create(cartService.getCartItems())
                .assertNext(ci -> assertThat(ci.getCount()).isEqualTo(2))
                .verifyComplete();
    }

    @Test
    void getCartItem_delegatesToRepository() {
        var cartItem = CartItem.builder().id(1L).itemId(1L).count(3).build();
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.just(cartItem));

        StepVerifier.create(cartService.getCartItem(1L))
                .assertNext(ci -> assertThat(ci.getCount()).isEqualTo(3))
                .verifyComplete();
    }

    @Test
    void getCartItemsCountMap_returnsMap() {
        var ci1 = CartItem.builder().id(1L).itemId(1L).count(2).build();
        var ci2 = CartItem.builder().id(2L).itemId(2L).count(5).build();
        when(cartItemRepository.findAll()).thenReturn(Flux.just(ci1, ci2));

        StepVerifier.create(cartService.getCartItemsCountMap())
                .assertNext(map -> {
                    assertThat(map).containsEntry(1L, 2);
                    assertThat(map).containsEntry(2L, 5);
                })
                .verifyComplete();
    }

    @Test
    void clean_deletesAll() {
        when(cartItemRepository.deleteAll()).thenReturn(Mono.empty());

        StepVerifier.create(cartService.clean())
                .verifyComplete();

        verify(cartItemRepository).deleteAll();
    }

    @Test
    void changeItemQuantity_plus_newItem() {
        var item = new Item();
        item.setId(1L);
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(cartService.changeItemQuantity(1L, Action.PLUS))
                .expectNextCount(1)
                .verifyComplete();

        verify(cartItemRepository).save(argThat(ci -> ci.getCount() == 1 && ci.getItemId() == 1L));
    }

    @Test
    void changeItemQuantity_plus_existingItem() {
        var item = new Item();
        item.setId(1L);
        var cartItem = CartItem.builder().id(1L).itemId(1L).count(2).build();

        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.just(cartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(cartService.changeItemQuantity(1L, Action.PLUS))
                .expectNextCount(1)
                .verifyComplete();

        verify(cartItemRepository).save(argThat(ci -> ci.getCount() == 3));
    }

    @Test
    void changeItemQuantity_minus_decreasesCount() {
        var item = new Item();
        item.setId(1L);
        var cartItem = CartItem.builder().id(1L).itemId(1L).count(3).build();

        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.just(cartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(cartService.changeItemQuantity(1L, Action.MINUS))
                .expectNextCount(1)
                .verifyComplete();

        verify(cartItemRepository).save(argThat(ci -> ci.getCount() == 2));
    }

    @Test
    void changeItemQuantity_minus_toZero_deletes() {
        var item = new Item();
        item.setId(1L);
        var cartItem = CartItem.builder().id(1L).itemId(1L).count(1).build();

        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.just(cartItem));
        when(cartItemRepository.delete(cartItem)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.changeItemQuantity(1L, Action.MINUS))
                .expectNextCount(1)
                .verifyComplete();

        verify(cartItemRepository).delete(cartItem);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void changeItemQuantity_minus_nonExisting_throwsValidation() {
        var item = new Item();
        item.setId(1L);
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.changeItemQuantity(1L, Action.MINUS))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void changeItemQuantity_delete_removesItem() {
        var item = new Item();
        item.setId(1L);
        var cartItem = CartItem.builder().id(1L).itemId(1L).count(5).build();

        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.just(cartItem));
        when(cartItemRepository.delete(cartItem)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.changeItemQuantity(1L, Action.DELETE))
                .expectNextCount(1)
                .verifyComplete();

        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void changeItemQuantity_delete_nonExisting_throwsValidation() {
        var item = new Item();
        item.setId(1L);
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.changeItemQuantity(1L, Action.DELETE))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void changeItemQuantity_itemNotFound_throwsNotFound() {
        when(itemRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.changeItemQuantity(999L, Action.PLUS))
                .expectError(NotFoundException.class)
                .verify();
    }
}
