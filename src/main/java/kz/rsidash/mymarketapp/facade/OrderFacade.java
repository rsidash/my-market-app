package kz.rsidash.mymarketapp.facade;

import kz.rsidash.mymarketapp.dto.order.OrderDto;
import kz.rsidash.mymarketapp.dto.order.mapper.OrderItemMapper;
import kz.rsidash.mymarketapp.dto.order.mapper.OrderMapper;
import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.model.order.Order;
import kz.rsidash.mymarketapp.repository.ItemRepository;
import kz.rsidash.mymarketapp.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public Mono<OrderDto> getOrder(Long orderId) {
        return orderService.getOrder(orderId)
                .flatMap(this::enrichOrder);
    }

    public Flux<OrderDto> getOrders() {
        return orderService.getOrders()
                .flatMap(this::enrichOrder);
    }

    public Mono<OrderDto> createOrder() {
        return orderService.createOrder()
                .flatMap(this::enrichOrder);
    }

    private Mono<OrderDto> enrichOrder(Order order) {
        return orderService.getOrderItems(order.getId())
                .flatMap(oi -> itemRepository.findById(oi.getItemId())
                        .defaultIfEmpty(new Item())
                        .map(item -> orderItemMapper.toDto(oi, item)))
                .collectList()
                .map(items -> orderMapper.toDto(order, items));
    }
}
