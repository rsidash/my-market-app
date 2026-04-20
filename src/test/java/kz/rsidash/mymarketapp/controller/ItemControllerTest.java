package kz.rsidash.mymarketapp.controller;

import kz.rsidash.mymarketapp.dto.Paging;
import kz.rsidash.mymarketapp.dto.item.ItemDto;
import kz.rsidash.mymarketapp.dto.item.ItemListView;
import kz.rsidash.mymarketapp.facade.ItemFacade;
import kz.rsidash.mymarketapp.model.enums.Action;
import kz.rsidash.mymarketapp.model.enums.SortType;
import kz.rsidash.mymarketapp.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebFluxTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ItemFacade itemFacade;

    @MockitoBean
    private CartService cartService;

    @Test
    void getItems_returnsItemsTemplate() {
        var itemListView = ItemListView.builder()
                .items(List.of(
                        ItemDto.builder().id(1).title("Item1").price(100).build(),
                        ItemDto.builder().id(2).title("Item2").price(200).build()
                ))
                .search(null)
                .sort(SortType.NO)
                .paging(Paging.builder().pageNumber(1).pageSize(5).hasPrevious(false).hasNext(false).build())
                .build();

        when(itemFacade.getItems(isNull(), eq(SortType.NO), eq(0), eq(5))).thenReturn(Mono.just(itemListView));

        webTestClient.get().uri("/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getItems_rootPath_returnsItemsTemplate() {
        var itemListView = ItemListView.builder()
                .items(List.of(ItemDto.builder().id(1).title("Item1").price(100).build()))
                .sort(SortType.NO)
                .paging(Paging.builder().pageNumber(1).pageSize(5).hasPrevious(false).hasNext(false).build())
                .build();

        when(itemFacade.getItems(isNull(), eq(SortType.NO), eq(0), eq(5))).thenReturn(Mono.just(itemListView));

        webTestClient.get().uri("/")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getItems_withSearchAndSort() {
        var itemListView = ItemListView.builder()
                .items(List.of(ItemDto.builder().id(1).title("Ball").price(50).build()))
                .search("ball")
                .sort(SortType.ALPHA)
                .paging(Paging.builder().pageNumber(1).pageSize(5).hasPrevious(false).hasNext(false).build())
                .build();

        when(itemFacade.getItems(eq("ball"), eq(SortType.ALPHA), eq(0), eq(5))).thenReturn(Mono.just(itemListView));

        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/items")
                        .queryParam("search", "ball")
                        .queryParam("sort", "ALPHA")
                        .build())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getItems_groupsByThreeWithStubs() {
        var itemListView = ItemListView.builder()
                .items(List.of(
                        ItemDto.builder().id(1).title("A").price(10).build(),
                        ItemDto.builder().id(2).title("B").price(20).build()
                ))
                .sort(SortType.NO)
                .paging(Paging.builder().pageNumber(1).pageSize(5).hasPrevious(false).hasNext(false).build())
                .build();

        when(itemFacade.getItems(any(), any(), anyInt(), anyInt())).thenReturn(Mono.just(itemListView));

        webTestClient.get().uri("/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getItem_returnsItemTemplate() {
        var item = ItemDto.builder().id(1).title("Ball").description("A ball").price(100).count(0).build();
        when(itemFacade.getItem(1L)).thenReturn(Mono.just(item));

        webTestClient.get().uri("/items/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getItem_notFound() {
        when(itemFacade.getItem(999L)).thenReturn(Mono.empty());

        webTestClient.get().uri("/items/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void changeItemQuantity_returnsItemTemplate() {
        var item = ItemDto.builder().id(1).title("Ball").price(100).count(1).build();
        when(cartService.changeItemQuantity(1L, Action.PLUS)).thenReturn(Mono.empty());
        when(itemFacade.getItem(1L)).thenReturn(Mono.just(item));

        webTestClient.post().uri(uriBuilder -> uriBuilder.path("/items/1")
                        .queryParam("action", "PLUS")
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(cartService).changeItemQuantity(1L, Action.PLUS);
    }

    @Test
    void changeItemQuantity_minus() {
        var item = ItemDto.builder().id(1).title("Ball").price(100).count(0).build();
        when(cartService.changeItemQuantity(1L, Action.MINUS)).thenReturn(Mono.empty());
        when(itemFacade.getItem(1L)).thenReturn(Mono.just(item));

        webTestClient.post().uri(uriBuilder -> uriBuilder.path("/items/1")
                        .queryParam("action", "MINUS")
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(cartService).changeItemQuantity(1L, Action.MINUS);
    }
}
