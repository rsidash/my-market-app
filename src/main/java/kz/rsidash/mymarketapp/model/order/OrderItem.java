package kz.rsidash.mymarketapp.model.order;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kz.rsidash.mymarketapp.model.item.Item;
import lombok.Data;

@Entity
@Table(name = "order-item")
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @Positive
    @Column(name = "count")
    private int count;
}
