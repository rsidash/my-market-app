package kz.rsidash.mymarketapp.service;

import kz.rsidash.mymarketapp.exception.ValidationException;
import kz.rsidash.mymarketapp.model.cart.CartItem;
import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.model.order.Order;
import kz.rsidash.mymarketapp.model.order.OrderItem;
import kz.rsidash.mymarketapp.repository.ItemRepository;
import kz.rsidash.mymarketapp.repository.OrderItemRepository;
import kz.rsidash.mymarketapp.repository.OrderRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void getOrders_returnsAll() {
        var order = new Order();
        order.setId(1L);
        when(orderRepository.findAll()).thenReturn(Flux.just(order));

        StepVerifier.create(orderService.getOrders())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getOrder_found() {
        var order = new Order();
        order.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Mono.just(order));

        StepVerifier.create(orderService.getOrder(1L))
                .assertNext(o -> assertThat(o.getId()).isEqualTo(1L))
                .verifyComplete();
    }

    @Test
    void getOrder_notFound() {
        when(orderRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.getOrder(999L))
                .verifyComplete();
    }

    @Test
    void createOrder_emptyCart_throwsValidation() {
        when(cartService.getCartItems()).thenReturn(Flux.empty());

        StepVerifier.create(orderService.createOrder())
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void createOrder_createsOrderFromCart() {
        var item1 = new Item();
        item1.setId(1L);
        item1.setTitle("Ball");
        item1.setPrice(100);

        var item2 = new Item();
        item2.setId(2L);
        item2.setTitle("Bat");
        item2.setPrice(200);

        var ci1 = CartItem.builder().id(1L).itemId(1L).count(2).build();
        var ci2 = CartItem.builder().id(2L).itemId(2L).count(1).build();

        when(cartService.getCartItems()).thenReturn(Flux.just(ci1, ci2));
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item1));
        when(itemRepository.findById(2L)).thenReturn(Mono.just(item2));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(10L);
            return Mono.just(o);
        });
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(cartService.clean()).thenReturn(Mono.empty());

        StepVerifier.create(orderService.createOrder())
                .assertNext(order -> {
                    assertThat(order.getId()).isEqualTo(10L);
                    assertThat(order.getTotalSum()).isEqualTo(400L);
                })
                .verifyComplete();

        verify(cartService).clean();
    }
}
