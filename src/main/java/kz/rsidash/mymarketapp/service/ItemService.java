package kz.rsidash.mymarketapp.service;

import kz.rsidash.mymarketapp.model.enums.SortType;
import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.repostitory.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public Page<Item> getItems(String search, SortType sortType, Pageable pageable) {
        final PageRequest sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                toSort(sortType)
        );

        if (StringUtils.isNoneBlank(search)) {
            return itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    search, search, sortedPageable
            );
        }

        return itemRepository.findAll(sortedPageable);
    }

    public Optional<Item> getItem(Long id) {
        return itemRepository.findById(id);
    }

    private Sort toSort(SortType sortType) {
        return switch (sortType) {
            case ALPHA -> Sort.by("title");
            case PRICE -> Sort.by("price");
            default -> Sort.unsorted();
        };
    }
}
