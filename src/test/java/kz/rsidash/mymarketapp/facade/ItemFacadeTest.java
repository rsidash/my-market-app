package kz.rsidash.mymarketapp.facade;

import kz.rsidash.mymarketapp.dto.item.mapper.ItemMapper;
import kz.rsidash.mymarketapp.model.cart.CartItem;
import kz.rsidash.mymarketapp.model.enums.SortType;
import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.service.CartService;
import kz.rsidash.mymarketapp.service.ItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemFacadeTest {

    @Mock
    private ItemService itemService;

    @Mock
    private CartService cartService;

    @Spy
    private ItemMapper itemMapper = new ItemMapper();

    @InjectMocks
    private ItemFacade itemFacade;

    @Test
    void getItems_returnsItemListView() {
        var item = new Item();
        item.setId(1L);
        item.setTitle("Ball");
        item.setPrice(100);

        var page = new PageImpl<>(List.of(item), PageRequest.of(0, 5), 1);
        when(itemService.getItems(any(), any(), any())).thenReturn(page);
        when(cartService.getCartItemsCountMap()).thenReturn(Map.of(1L, 3));

        var result = itemFacade.getItems(null, SortType.NO, 0, 5);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getCount()).isEqualTo(3);
        assertThat(result.getPaging().getPageNumber()).isEqualTo(1);
        assertThat(result.getPaging().getPageSize()).isEqualTo(5);
        assertThat(result.getPaging().isHasPrevious()).isFalse();
        assertThat(result.getPaging().isHasNext()).isFalse();
    }

    @Test
    void getItems_itemNotInCart_countZero() {
        var item = new Item();
        item.setId(1L);
        item.setTitle("Ball");
        item.setPrice(100);

        var page = new PageImpl<>(List.of(item), PageRequest.of(0, 5), 1);
        when(itemService.getItems(any(), any(), any())).thenReturn(page);
        when(cartService.getCartItemsCountMap()).thenReturn(Map.of());

        var result = itemFacade.getItems(null, SortType.NO, 0, 5);

        assertThat(result.getItems().get(0).getCount()).isZero();
    }

    @Test
    void getItem_found_withCartCount() {
        var item = new Item();
        item.setId(1L);
        item.setTitle("Ball");
        item.setPrice(100);

        var cartItem = CartItem.builder().id(1L).item(item).count(2).build();

        when(itemService.getItem(1L)).thenReturn(Optional.of(item));
        when(cartService.getCartItem(1L)).thenReturn(Optional.of(cartItem));

        var result = itemFacade.getItem(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getCount()).isEqualTo(2);
    }

    @Test
    void getItem_found_notInCart() {
        var item = new Item();
        item.setId(1L);
        item.setTitle("Ball");
        item.setPrice(100);

        when(itemService.getItem(1L)).thenReturn(Optional.of(item));
        when(cartService.getCartItem(1L)).thenReturn(Optional.empty());

        var result = itemFacade.getItem(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getCount()).isZero();
    }

    @Test
    void getItem_notFound() {
        when(itemService.getItem(999L)).thenReturn(Optional.empty());

        var result = itemFacade.getItem(999L);

        assertThat(result).isEmpty();
    }
}
