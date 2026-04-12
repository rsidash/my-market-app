package kz.rsidash.mymarketapp.dto.order.mapper;

import kz.rsidash.mymarketapp.dto.order.OrderDto;
import kz.rsidash.mymarketapp.model.order.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMapper {
    private final OrderItemMapper orderItemMapper;

    public OrderDto toDto(Order order) {
        final var items = order.getItems().stream()
                .map(orderItemMapper::toDto)
                .toList();

        return OrderDto.builder()
                .id(order.getId())
                .items(items)
                .totalSum(order.getTotalSum())
                .build();
    }
}
