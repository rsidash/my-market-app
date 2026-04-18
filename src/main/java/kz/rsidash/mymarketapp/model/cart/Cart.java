package kz.rsidash.mymarketapp.model.cart;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@RequiredArgsConstructor
@Table("carts")
public class Cart {
    @Id
    private Long id;
}
