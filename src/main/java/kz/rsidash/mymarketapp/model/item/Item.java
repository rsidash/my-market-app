package kz.rsidash.mymarketapp.model.item;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "items")
@Data
@RequiredArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "imgPth")
    private String imgPth;

    @PositiveOrZero
    @Column(name = "price")
    private long price;
}
