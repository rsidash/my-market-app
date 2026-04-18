package kz.rsidash.mymarketapp.model.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@RequiredArgsConstructor
@Table("items")
public class Item {
    @Id
    private Long id;

    @NotBlank
    private String title;

    private String description;

    private String imgPth;

    @PositiveOrZero
    private long price;
}
