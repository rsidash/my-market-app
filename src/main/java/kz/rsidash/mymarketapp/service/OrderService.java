package kz.rsidash.mymarketapp.service;

import kz.rsidash.mymarketapp.exception.ValidationException;
import kz.rsidash.mymarketapp.model.order.Order;
import kz.rsidash.mymarketapp.model.order.OrderItem;
import kz.rsidash.mymarketapp.repository.ItemRepository;
import kz.rsidash.mymarketapp.repository.OrderItemRepository;
import kz.rsidash.mymarketapp.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;

    public Flux<Order> getOrders() {
        return orderRepository.findAll();
    }

    public Mono<Order> getOrder(long id) {
        return orderRepository.findById(id);
    }

    public Mono<Order> createOrder() {
        return cartService.getCartItems()
                .collectList()
                .flatMap(cartItems -> {
                    if (cartItems.isEmpty()) {
                        return Mono.error(new ValidationException("Cart is empty"));
                    }

                    return Flux.fromIterable(cartItems)
                            .flatMap(ci ->
                                    itemRepository.findById(ci.getItemId())
                                            .map(item -> item.getPrice() * ci.getCount())
                            )
                            .reduce(0L, Long::sum)
                            .map(total -> {
                                Order order = new Order();
                                order.setTotalSum(total);
                                return order;
                            })
                            .flatMap(orderRepository::save)
                            .flatMap(savedOrder ->
                                    Flux.fromIterable(cartItems)
                                            .map(ci -> {
                                                OrderItem oi = new OrderItem();
                                                oi.setOrderId(savedOrder.getId());
                                                oi.setItemId(ci.getItemId());
                                                oi.setCount(ci.getCount());
                                                return oi;
                                            })
                                            .flatMap(orderItemRepository::save)
                                            .then(cartService.clean())
                                            .thenReturn(savedOrder)
                            );
                });
    }

    public Flux<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
}
