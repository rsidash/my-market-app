package kz.rsidash.mymarketapp.controller;

import kz.rsidash.mymarketapp.dto.item.ItemDto;
import kz.rsidash.mymarketapp.dto.item.mapper.ItemMapper;
import kz.rsidash.mymarketapp.model.cart.CartItem;
import kz.rsidash.mymarketapp.model.enums.Action;
import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.service.CartService;
import kz.rsidash.mymarketapp.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

@WebFluxTest(CartController.class)
class CartControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private ItemMapper itemMapper;

    @MockitoBean
    private ItemService itemService;

    @Test
    void postItems_redirectsWithParams() {
        when(cartService.changeItemQuantity(1L, Action.PLUS)).thenReturn(Mono.empty());

        webTestClient.post().uri(uriBuilder -> uriBuilder.path("/items")
                        .queryParam("id", "1")
                        .queryParam("action", "PLUS")
                        .queryParam("search", "ball")
                        .queryParam("sort", "ALPHA")
                        .queryParam("pageNumber", "2")
                        .queryParam("pageSize", "10")
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/items?search=ball&sort=ALPHA&pageNumber=2&pageSize=10");

        verify(cartService).changeItemQuantity(1L, Action.PLUS);
    }

    @Test
    void postItems_redirectsWithDefaults() {
        when(cartService.changeItemQuantity(5L, Action.MINUS)).thenReturn(Mono.empty());

        webTestClient.post().uri(uriBuilder -> uriBuilder.path("/items")
                        .queryParam("id", "5")
                        .queryParam("action", "MINUS")
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/items?search=&sort=NO&pageNumber=1&pageSize=5");

        verify(cartService).changeItemQuantity(5L, Action.MINUS);
    }

    @Test
    void getCartItems_returnsCartTemplate() {
        when(cartService.getCartItems()).thenReturn(Flux.empty());

        webTestClient.get().uri("/cart/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getCartItems_withItems() {
        var item = new Item();
        item.setId(1L);
        item.setTitle("Ball");
        item.setPrice(100);

        var cartItem = CartItem.builder().id(1L).itemId(1L).count(2).build();
        var dto = ItemDto.builder().id(1).title("Ball").price(100).count(2).build();

        when(cartService.getCartItems()).thenReturn(Flux.just(cartItem));
        when(itemService.getItem(1L)).thenReturn(Mono.just(item));
        when(itemMapper.toDto(item, 2)).thenReturn(dto);

        webTestClient.get().uri("/cart/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void postCartItems_changesQuantityAndReturnsCart() {
        when(cartService.changeItemQuantity(1L, Action.PLUS)).thenReturn(Mono.empty());
        when(cartService.getCartItems()).thenReturn(Flux.empty());

        webTestClient.post().uri(uriBuilder -> uriBuilder.path("/cart/items")
                        .queryParam("id", "1")
                        .queryParam("action", "PLUS")
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(cartService).changeItemQuantity(1L, Action.PLUS);
    }

    @Test
    void postCartItems_deleteAction() {
        when(cartService.changeItemQuantity(3L, Action.DELETE)).thenReturn(Mono.empty());
        when(cartService.getCartItems()).thenReturn(Flux.empty());

        webTestClient.post().uri(uriBuilder -> uriBuilder.path("/cart/items")
                        .queryParam("id", "3")
                        .queryParam("action", "DELETE")
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(cartService).changeItemQuantity(3L, Action.DELETE);
    }
}
