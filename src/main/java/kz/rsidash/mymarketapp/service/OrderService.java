package kz.rsidash.mymarketapp.service;

import jakarta.transaction.Transactional;
import kz.rsidash.mymarketapp.exception.ValidationException;
import kz.rsidash.mymarketapp.model.order.Order;
import kz.rsidash.mymarketapp.model.order.OrderItem;
import kz.rsidash.mymarketapp.repostitory.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;

    public List<Order> getOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrder(long id) {
        return orderRepository.findById(id);
    }

    @Transactional
    public Order createOrder() {
        final var cartItems = cartService.getCartItems();

        if (cartItems.isEmpty()) {
            throw new ValidationException("Cart is empty");
        }

        final var order = new Order();

        final var orderItems = cartItems.stream()
                .map(ci -> {
                    var oi = new OrderItem();
                    oi.setOrder(order);
                    oi.setItem(ci.getItem());
                    oi.setCount(ci.getCount());
                    return oi;
                })
                .toList();

        order.setItems(orderItems);
        order.setTotalSum(cartItems.stream()
                .mapToLong(ci -> ci.getItem().getPrice() * ci.getCount())
                .sum());

        final var saved = orderRepository.save(order);
        cartService.clean();
        return saved;
    }
}
