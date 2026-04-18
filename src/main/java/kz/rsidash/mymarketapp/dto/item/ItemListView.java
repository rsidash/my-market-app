package kz.rsidash.mymarketapp.dto.item;

import kz.rsidash.mymarketapp.dto.Paging;
import kz.rsidash.mymarketapp.model.enums.SortType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemListView {
    private List<ItemDto> items;
    private String search;
    private SortType sort;
    private Paging paging;
}
