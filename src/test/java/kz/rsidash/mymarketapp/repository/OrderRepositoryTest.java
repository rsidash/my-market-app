package kz.rsidash.mymarketapp.repository;

import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.model.order.Order;
import kz.rsidash.mymarketapp.model.order.OrderItem;
import kz.rsidash.mymarketapp.repostitory.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void save_andFindById() {
        var item = new Item();
        item.setTitle("Ball");
        item.setPrice(100);
        em.persist(item);

        var order = new Order();
        order.setTotalSum(200L);

        var orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setItem(item);
        orderItem.setCount(2);

        order.setItems(List.of(orderItem));

        var saved = orderRepository.save(order);
        em.flush();
        em.clear();

        var found = orderRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTotalSum()).isEqualTo(200L);
        assertThat(found.get().getItems()).hasSize(1);
        assertThat(found.get().getItems().get(0).getCount()).isEqualTo(2);
    }

    @Test
    void findById_notFound() {
        var result = orderRepository.findById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_returnsAllOrders() {
        var item = new Item();
        item.setTitle("Ball");
        item.setPrice(100);
        em.persist(item);

        var order1 = new Order();
        order1.setTotalSum(100L);
        var oi1 = new OrderItem();
        oi1.setOrder(order1);
        oi1.setItem(item);
        oi1.setCount(1);
        order1.setItems(List.of(oi1));
        em.persist(order1);

        var order2 = new Order();
        order2.setTotalSum(200L);
        var oi2 = new OrderItem();
        oi2.setOrder(order2);
        oi2.setItem(item);
        oi2.setCount(2);
        order2.setItems(List.of(oi2));
        em.persist(order2);
        em.flush();

        var result = orderRepository.findAll();
        assertThat(result).hasSize(2);
    }
}
