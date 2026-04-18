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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

        when(itemService.getItems(any(), any())).thenReturn(Flux.just(item));
        when(cartService.getCartItemsCountMap()).thenReturn(Mono.just(Map.of(1L, 3)));

        StepVerifier.create(itemFacade.getItems(null, SortType.NO, 0, 5))
                .assertNext(result -> {
                    assertThat(result.getItems()).hasSize(1);
                    assertThat(result.getItems().get(0).getCount()).isEqualTo(3);
                    assertThat(result.getPaging().getPageNumber()).isEqualTo(1);
                    assertThat(result.getPaging().getPageSize()).isEqualTo(5);
                    assertThat(result.getPaging().isHasPrevious()).isFalse();
                    assertThat(result.getPaging().isHasNext()).isFalse();
                })
                .verifyComplete();
    }

    @Test
    void getItems_itemNotInCart_countZero() {
        var item = new Item();
        item.setId(1L);
        item.setTitle("Ball");
        item.setPrice(100);

        when(itemService.getItems(any(), any())).thenReturn(Flux.just(item));
        when(cartService.getCartItemsCountMap()).thenReturn(Mono.just(Map.of()));

        StepVerifier.create(itemFacade.getItems(null, SortType.NO, 0, 5))
                .assertNext(result -> assertThat(result.getItems().get(0).getCount()).isZero())
                .verifyComplete();
    }

    @Test
    void getItem_found_withCartCount() {
        var item = new Item();
        item.setId(1L);
        item.setTitle("Ball");
        item.setPrice(100);

        var cartItem = CartItem.builder().id(1L).itemId(1L).count(2).build();

        when(itemService.getItem(1L)).thenReturn(Mono.just(item));
        when(cartService.getCartItem(1L)).thenReturn(Mono.just(cartItem));

        StepVerifier.create(itemFacade.getItem(1L))
                .assertNext(dto -> assertThat(dto.getCount()).isEqualTo(2))
                .verifyComplete();
    }

    @Test
    void getItem_found_notInCart() {
        var item = new Item();
        item.setId(1L);
        item.setTitle("Ball");
        item.setPrice(100);

        when(itemService.getItem(1L)).thenReturn(Mono.just(item));
        when(cartService.getCartItem(1L)).thenReturn(Mono.empty());

        StepVerifier.create(itemFacade.getItem(1L))
                .assertNext(dto -> assertThat(dto.getCount()).isZero())
                .verifyComplete();
    }

    @Test
    void getItem_notFound() {
        when(itemService.getItem(999L)).thenReturn(Mono.empty());
        when(cartService.getCartItem(999L)).thenReturn(Mono.empty());

        StepVerifier.create(itemFacade.getItem(999L))
                .verifyComplete();
    }
}
