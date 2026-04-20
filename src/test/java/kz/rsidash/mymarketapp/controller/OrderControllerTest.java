package kz.rsidash.mymarketapp.controller;

import kz.rsidash.mymarketapp.dto.order.OrderDto;
import kz.rsidash.mymarketapp.facade.OrderFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.mockito.Mockito.when;

@WebFluxTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private OrderFacade orderFacade;

    @Test
    void getOrders_returnsOrdersTemplate() {
        when(orderFacade.getOrders()).thenReturn(Flux.empty());

        webTestClient.get().uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getOrders_withOrders() {
        var dto = OrderDto.builder().id(1).items(Collections.emptyList()).totalSum(300).build();

        when(orderFacade.getOrders()).thenReturn(Flux.just(dto));

        webTestClient.get().uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getOrderById_returnsOrderTemplate() {
        var dto = OrderDto.builder().id(1).items(Collections.emptyList()).totalSum(300).build();

        when(orderFacade.getOrder(1L)).thenReturn(Mono.just(dto));

        webTestClient.get().uri("/orders/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getOrderById_withNewOrderTrue() {
        var dto = OrderDto.builder().id(1).items(Collections.emptyList()).totalSum(300).build();

        when(orderFacade.getOrder(1L)).thenReturn(Mono.just(dto));

        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/orders/1")
                        .queryParam("newOrder", "true")
                        .build())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getOrderById_notFound() {
        when(orderFacade.getOrder(999L)).thenReturn(Mono.empty());

        webTestClient.get().uri("/orders/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void buy_redirectsToOrder() {
        var dto = OrderDto.builder().id(42).items(Collections.emptyList()).totalSum(300).build();

        when(orderFacade.createOrder()).thenReturn(Mono.just(dto));

        webTestClient.post().uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/orders/42?newOrder=true");
    }
}
