package kz.rsidash.mymarketapp.service;

import kz.rsidash.mymarketapp.model.enums.SortType;
import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.repostitory.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public Flux<Item> getItems(String search, SortType sortType) {
        Sort sort = toSort(sortType);

        if (StringUtils.isNoneBlank(search)) {
            return itemRepository
                    .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                            search, search
                    )
                    .sort(sortComparator(sort));
        }

        return itemRepository.findAll()
                .sort(sortComparator(sort));
    }

    public Mono<Item> getItem(Long id) {
        return itemRepository.findById(id);
    }

    private Sort toSort(SortType sortType) {
        return switch (sortType) {
            case ALPHA -> Sort.by("title");
            case PRICE -> Sort.by("price");
            default -> Sort.unsorted();
        };
    }

    private Comparator<Item> sortComparator(Sort sort) {
        return (a, b) -> {
            if (sort.isSorted()) {
                String property = sort.iterator().next().getProperty();

                return switch (property) {
                    case "title" -> a.getTitle().compareToIgnoreCase(b.getTitle());
                    case "price" -> Long.compare(a.getPrice(), b.getPrice());
                    default -> 0;
                };
            }
            return 0;
        };
    }
}
