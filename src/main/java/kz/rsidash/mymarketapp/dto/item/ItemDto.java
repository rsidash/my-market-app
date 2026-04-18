package kz.rsidash.mymarketapp.dto.item;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private long id;
    private String title;
    private String description;
    private String imgPath;
    private long price;
    private int count;
}
