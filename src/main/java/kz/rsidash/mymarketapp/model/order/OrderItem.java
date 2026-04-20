package kz.rsidash.mymarketapp.model.order;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@RequiredArgsConstructor
@Table("order-item")
public class OrderItem {
    @Id
    private Long id;

    private Long orderId;

    private Long itemId;

    private int count;
}
