package kz.rsidash.mymarketapp.service;

import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public Flux<Item> getItems(String search, Pageable pageable) {
        if (StringUtils.isNoneBlank(search)) {
            return itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    search, search, pageable
            );
        }

        return itemRepository.findAll(pageable.getSort());
    }

    public Mono<Item> getItem(Long id) {
        return itemRepository.findById(id);
    }
}
