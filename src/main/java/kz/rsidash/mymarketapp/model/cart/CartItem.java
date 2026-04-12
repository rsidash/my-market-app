package kz.rsidash.mymarketapp.model.cart;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kz.rsidash.mymarketapp.model.item.Item;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cart-item")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @Positive
    @Column(name = "count")
    private int count;
}
