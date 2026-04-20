package kz.rsidash.mymarketapp.controller;

import kz.rsidash.mymarketapp.dto.order.OrderDto;
import kz.rsidash.mymarketapp.dto.order.OrderItemDto;
import kz.rsidash.mymarketapp.dto.order.mapper.OrderItemMapper;
import kz.rsidash.mymarketapp.dto.order.mapper.OrderMapper;
import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.model.order.Order;
import kz.rsidash.mymarketapp.model.order.OrderItem;
import kz.rsidash.mymarketapp.repository.ItemRepository;
import kz.rsidash.mymarketapp.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderMapper orderMapper;

    @MockitoBean
    private OrderItemMapper orderItemMapper;

    @MockitoBean
    private ItemRepository itemRepository;

    @Test
    void getOrders_returnsOrdersTemplate() {
        when(orderService.getOrders()).thenReturn(Flux.empty());

        webTestClient.get().uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getOrders_withOrders() {
        var order = new Order();
        order.setId(1L);
        order.setTotalSum(500L);

        var orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setOrderId(1L);
        orderItem.setItemId(10L);
        orderItem.setCount(2);

        var item = new Item();
        item.setId(10L);
        item.setTitle("Ball");
        item.setPrice(250);

        var orderItemDto = OrderItemDto.builder().id(1).title("Ball").price(250).count(2).build();
        var dto = OrderDto.builder().id(1).items(List.of(orderItemDto)).totalSum(500).build();

        when(orderService.getOrders()).thenReturn(Flux.just(order));
        when(orderService.getOrderItems(1L)).thenReturn(Flux.just(orderItem));
        when(itemRepository.findById(10L)).thenReturn(Mono.just(item));
        when(orderItemMapper.toDto(orderItem, item)).thenReturn(orderItemDto);
        when(orderMapper.toDto(eq(order), any())).thenReturn(dto);

        webTestClient.get().uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getOrderById_returnsOrderTemplate() {
        var order = new Order();
        order.setId(1L);
        order.setTotalSum(300L);

        var dto = OrderDto.builder().id(1).items(Collections.emptyList()).totalSum(300).build();

        when(orderService.getOrder(1L)).thenReturn(Mono.just(order));
        when(orderService.getOrderItems(1L)).thenReturn(Flux.empty());
        when(orderMapper.toDto(eq(order), any())).thenReturn(dto);

        webTestClient.get().uri("/orders/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getOrderById_withNewOrderTrue() {
        var order = new Order();
        order.setId(1L);
        order.setTotalSum(300L);

        var dto = OrderDto.builder().id(1).items(Collections.emptyList()).totalSum(300).build();

        when(orderService.getOrder(1L)).thenReturn(Mono.just(order));
        when(orderService.getOrderItems(1L)).thenReturn(Flux.empty());
        when(orderMapper.toDto(eq(order), any())).thenReturn(dto);

        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/orders/1")
                        .queryParam("newOrder", "true")
                        .build())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getOrderById_notFound() {
        when(orderService.getOrder(999L)).thenReturn(Mono.empty());

        webTestClient.get().uri("/orders/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void buy_redirectsToOrder() {
        var order = new Order();
        order.setId(42L);

        when(orderService.createOrder()).thenReturn(Mono.just(order));

        webTestClient.post().uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/orders/42?newOrder=true");
    }
}
