package kz.rsidash.mymarketapp.model.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@RequiredArgsConstructor
@Table("orders")
public class Order {
    @Id
    private Long id;

    @NotNull
    @PositiveOrZero
    private Long totalSum;
}
