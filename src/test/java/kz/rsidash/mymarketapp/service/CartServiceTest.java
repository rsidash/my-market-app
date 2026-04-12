package kz.rsidash.mymarketapp.service;

import kz.rsidash.mymarketapp.exception.NotFoundException;
import kz.rsidash.mymarketapp.exception.ValidationException;
import kz.rsidash.mymarketapp.model.cart.CartItem;
import kz.rsidash.mymarketapp.model.enums.Action;
import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.repostitory.CartItemRepository;
import kz.rsidash.mymarketapp.repostitory.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
        var cartItem = CartItem.builder().id(1L).count(2).build();
        when(cartItemRepository.findAll()).thenReturn(List.of(cartItem));

        var result = cartService.getCartItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCount()).isEqualTo(2);
    }

    @Test
    void getCartItem_delegatesToRepository() {
        var item = new Item();
        item.setId(1L);
        var cartItem = CartItem.builder().id(1L).item(item).count(3).build();
        when(cartItemRepository.findByItemId(1L)).thenReturn(Optional.of(cartItem));

        var result = cartService.getCartItem(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getCount()).isEqualTo(3);
    }

    @Test
    void getCartItemsCountMap_returnsMap() {
        var ci1 = CartItem.builder().id(1L).count(2).build();
        var ci2 = CartItem.builder().id(2L).count(5).build();
        when(cartItemRepository.findAll()).thenReturn(List.of(ci1, ci2));

        var map = cartService.getCartItemsCountMap();

        assertThat(map).containsEntry(1L, 2).containsEntry(2L, 5);
    }

    @Test
    void clean_deletesAll() {
        cartService.clean();
        verify(cartItemRepository).deleteAll();
    }

    @Test
    void changeItemQuantity_plus_newItem() {
        var item = new Item();
        item.setId(1L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Optional.empty());

        cartService.changeItemQuantity(1L, Action.PLUS);

        verify(cartItemRepository).save(argThat(ci -> ci.getCount() == 1 && ci.getItem() == item));
    }

    @Test
    void changeItemQuantity_plus_existingItem() {
        var item = new Item();
        item.setId(1L);
        var cartItem = CartItem.builder().id(1L).item(item).count(2).build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Optional.of(cartItem));

        cartService.changeItemQuantity(1L, Action.PLUS);

        verify(cartItemRepository).save(argThat(ci -> ci.getCount() == 3));
    }

    @Test
    void changeItemQuantity_minus_decreasesCount() {
        var item = new Item();
        item.setId(1L);
        var cartItem = CartItem.builder().id(1L).item(item).count(3).build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Optional.of(cartItem));

        cartService.changeItemQuantity(1L, Action.MINUS);

        verify(cartItemRepository).save(argThat(ci -> ci.getCount() == 2));
    }

    @Test
    void changeItemQuantity_minus_toZero_deletes() {
        var item = new Item();
        item.setId(1L);
        var cartItem = CartItem.builder().id(1L).item(item).count(1).build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Optional.of(cartItem));

        cartService.changeItemQuantity(1L, Action.MINUS);

        verify(cartItemRepository).delete(cartItem);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void changeItemQuantity_minus_nonExisting_throwsValidation() {
        var item = new Item();
        item.setId(1L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.changeItemQuantity(1L, Action.MINUS))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void changeItemQuantity_delete_removesItem() {
        var item = new Item();
        item.setId(1L);
        var cartItem = CartItem.builder().id(1L).item(item).count(5).build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Optional.of(cartItem));

        cartService.changeItemQuantity(1L, Action.DELETE);

        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void changeItemQuantity_delete_nonExisting_throwsValidation() {
        var item = new Item();
        item.setId(1L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.changeItemQuantity(1L, Action.DELETE))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void changeItemQuantity_itemNotFound_throwsNotFound() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.changeItemQuantity(999L, Action.PLUS))
                .isInstanceOf(NotFoundException.class);
    }
}
