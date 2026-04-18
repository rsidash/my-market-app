package kz.rsidash.mymarketapp.model.cart;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("cart-item")
public class CartItem {
    @Id
    private Long id;

    private Long cartId;

    private Long itemId;

    private int count;
}
