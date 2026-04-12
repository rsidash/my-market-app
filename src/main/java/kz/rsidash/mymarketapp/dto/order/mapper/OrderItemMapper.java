package kz.rsidash.mymarketapp.dto.order.mapper;

import jakarta.validation.constraints.NotNull;
import kz.rsidash.mymarketapp.dto.order.OrderItemDto;
import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.model.order.OrderItem;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderItemMapper {
    public OrderItemDto toDto(@NotNull OrderItem orderItem) {
        return OrderItemDto.builder()
                .id(orderItem.getId())
                .title(Optional.ofNullable(orderItem.getItem()).map(Item::getTitle).orElse(null))
                .price(Optional.ofNullable(orderItem.getItem()).map(Item::getPrice).orElse(0L))
                .count(orderItem.getCount())
                .build();
    }
}
