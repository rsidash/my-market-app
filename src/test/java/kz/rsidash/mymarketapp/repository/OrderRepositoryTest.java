package kz.rsidash.mymarketapp.repository;

import kz.rsidash.mymarketapp.model.order.Order;
import kz.rsidash.mymarketapp.model.order.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll().block();
        orderRepository.deleteAll().block();
    }

    @Test
    void save_andFindById() {
        var order = new Order();
        order.setTotalSum(200L);
        var saved = orderRepository.save(order).block();

        var orderItem = new OrderItem();
        orderItem.setOrderId(saved.getId());
        orderItem.setItemId(1L);
        orderItem.setCount(2);
        orderItemRepository.save(orderItem).block();

        StepVerifier.create(orderRepository.findById(saved.getId()))
                .assertNext(o -> assertThat(o.getTotalSum()).isEqualTo(200L))
                .verifyComplete();

        StepVerifier.create(orderItemRepository.findByOrderId(saved.getId()))
                .assertNext(oi -> assertThat(oi.getCount()).isEqualTo(2))
                .verifyComplete();
    }

    @Test
    void findById_notFound() {
        StepVerifier.create(orderRepository.findById(999L))
                .verifyComplete();
    }

    @Test
    void findAll_returnsAllOrders() {
        var order1 = new Order();
        order1.setTotalSum(100L);
        orderRepository.save(order1).block();

        var order2 = new Order();
        order2.setTotalSum(200L);
        orderRepository.save(order2).block();

        StepVerifier.create(orderRepository.findAll())
                .expectNextCount(2)
                .verifyComplete();
    }
}
