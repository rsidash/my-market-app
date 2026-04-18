package kz.rsidash.mymarketapp.facade;

import kz.rsidash.mymarketapp.dto.Paging;
import kz.rsidash.mymarketapp.dto.item.ItemDto;
import kz.rsidash.mymarketapp.dto.item.ItemListView;
import kz.rsidash.mymarketapp.dto.item.mapper.ItemMapper;
import kz.rsidash.mymarketapp.model.cart.CartItem;
import kz.rsidash.mymarketapp.model.enums.SortType;
import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.service.CartService;
import kz.rsidash.mymarketapp.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ItemFacade {
    private final ItemService itemService;
    private final CartService cartService;
    private final ItemMapper itemMapper;

    public ItemListView getItems(
            String search,
            SortType sortType,
            int pageNumber,
            int pageSize
    ) {
        final Page<Item> itemPage = itemService.getItems(
                search,
                sortType,
                PageRequest.of(pageNumber, pageSize)
        );

        final Map<Long, Integer> cartMap = cartService.getCartItemsCountMap();

        final List<ItemDto> items = itemPage.map(i ->
                itemMapper.toDto(i, cartMap.getOrDefault(i.getId(), 0))
        ).getContent();

        return ItemListView.builder()
                .items(items)
                .sort(sortType)
                .search(search)
                .paging(Paging.builder()
                        .pageSize(itemPage.getSize())
                        .pageNumber(itemPage.getNumber() + 1)
                        .hasPrevious(itemPage.hasPrevious())
                        .hasNext(itemPage.hasNext())
                        .build())
                .build();
    }

    public Optional<ItemDto> getItem(Long id) {
        return itemService.getItem(id)
                .map(i -> {
                    final Integer count = cartService.getCartItem(id)
                            .map(CartItem::getCount)
                            .orElse(0);

                    return itemMapper.toDto(i, count);
                });
    }
}
