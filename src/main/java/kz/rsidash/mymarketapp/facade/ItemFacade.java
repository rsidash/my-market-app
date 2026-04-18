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
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ItemFacade {

    private final ItemService itemService;
    private final CartService cartService;
    private final ItemMapper itemMapper;

    public Mono<ItemListView> getItems(
            String search,
            SortType sortType,
            int pageNumber,
            int pageSize
    ) {

        Mono<Map<Long, Integer>> cartMapMono = cartService.getCartItemsCountMap();

        return itemService.getItems(search, sortType)
                .skip((long) pageNumber * pageSize)
                .take(pageSize)
                .collectList()
                .zipWith(cartMapMono)
                .map(tuple -> {

                    List<Item> items = tuple.getT1();
                    Map<Long, Integer> cartMap = tuple.getT2();

                    List<ItemDto> itemDtos = items.stream()
                            .map(i -> itemMapper.toDto(
                                    i,
                                    cartMap.getOrDefault(i.getId(), 0)
                            ))
                            .toList();

                    return ItemListView.builder()
                            .items(itemDtos)
                            .sort(sortType)
                            .search(search)
                            .paging(Paging.builder()
                                    .pageSize(pageSize)
                                    .pageNumber(pageNumber + 1)
                                    .hasPrevious(pageNumber > 0)
                                    .hasNext(items.size() == pageSize)
                                    .build())
                            .build();
                });
    }

    public Mono<ItemDto> getItem(Long id) {
        return itemService.getItem(id)
                .zipWith(
                        cartService.getCartItem(id)
                                .map(CartItem::getCount)
                                .defaultIfEmpty(0)
                )
                .map(tuple -> {
                    Item item = tuple.getT1();
                    int count = tuple.getT2();

                    return itemMapper.toDto(item, count);
                });
    }
}
