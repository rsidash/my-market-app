package kz.rsidash.mymarketapp.dto.order.mapper;

import kz.rsidash.mymarketapp.dto.order.OrderDto;
import kz.rsidash.mymarketapp.dto.order.OrderItemDto;
import kz.rsidash.mymarketapp.model.order.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderMapper {
    public OrderDto toDto(Order order, List<OrderItemDto> items) {
        return OrderDto.builder()
                .id(order.getId())
                .items(items)
                .totalSum(order.getTotalSum())
                .build();
    }
}
