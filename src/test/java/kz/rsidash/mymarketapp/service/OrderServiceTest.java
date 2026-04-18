package kz.rsidash.mymarketapp.service;

import kz.rsidash.mymarketapp.exception.ValidationException;
import kz.rsidash.mymarketapp.model.cart.CartItem;
import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.model.order.Order;
import kz.rsidash.mymarketapp.repostitory.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void getOrders_returnsAll() {
        var order = new Order();
        order.setId(1L);
        when(orderRepository.findAll()).thenReturn(List.of(order));

        var result = orderService.getOrders();

        assertThat(result).hasSize(1);
    }

    @Test
    void getOrder_found() {
        var order = new Order();
        order.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        var result = orderService.getOrder(1L);

        assertThat(result).isPresent();
    }

    @Test
    void getOrder_notFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        var result = orderService.getOrder(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void createOrder_emptyCart_throwsValidation() {
        when(cartService.getCartItems()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> orderService.createOrder())
                .isInstanceOf(ValidationException.class);
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

        var ci1 = CartItem.builder().id(1L).item(item1).count(2).build();
        var ci2 = CartItem.builder().id(2L).item(item2).count(1).build();

        when(cartService.getCartItems()).thenReturn(List.of(ci1, ci2));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(10L);
            return o;
        });

        var result = orderService.createOrder();

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getTotalSum()).isEqualTo(400L); // 100*2 + 200*1
        assertThat(result.getItems()).hasSize(2);
        verify(cartService).clean();
    }
}
