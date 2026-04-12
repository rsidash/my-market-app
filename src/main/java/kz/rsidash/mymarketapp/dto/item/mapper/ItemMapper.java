package kz.rsidash.mymarketapp.dto.item.mapper;

import kz.rsidash.mymarketapp.dto.item.ItemDto;
import kz.rsidash.mymarketapp.model.item.Item;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {

    public ItemDto toDto(Item source, int count) {
        return ItemDto.builder()
                .id(source.getId())
                .title(source.getTitle())
                .description(source.getDescription())
                .imgPath(source.getImgPth())
                .price(source.getPrice())
                .count(count)
                .build();
    }

}
