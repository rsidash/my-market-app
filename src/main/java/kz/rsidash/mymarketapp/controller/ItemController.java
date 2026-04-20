package kz.rsidash.mymarketapp.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import kz.rsidash.mymarketapp.dto.item.ItemDto;
import kz.rsidash.mymarketapp.facade.ItemFacade;
import kz.rsidash.mymarketapp.model.enums.Action;
import kz.rsidash.mymarketapp.model.enums.SortType;
import kz.rsidash.mymarketapp.service.CartService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Controller
@Validated
@RequiredArgsConstructor
public class ItemController {

    private final ItemFacade itemFacade;
    private final CartService cartService;

    @GetMapping({"/", "/items"})
    public Mono<String> getItems(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "NO") SortType sort,
            @RequestParam(defaultValue = "1") @Min(1) int pageNumber,
            @RequestParam(defaultValue = "5") @Positive int pageSize,
            Model model
    ) {
        return itemFacade.getItems(search, sort, pageNumber - 1, pageSize)
                .map(result -> {

                    List<List<ItemDto>> groupedItems =
                            groupByThree(result.getItems());

                    model.addAttribute("items", groupedItems);
                    model.addAttribute("search", search);
                    model.addAttribute("sort", sort);
                    model.addAttribute("paging", result.getPaging());

                    return "items";
                });
    }

    @GetMapping("/items/{id}")
    public Mono<String> getItem(
            @PathVariable @Positive Long id,
            Model model
    ) {
        return itemFacade.getItem(id)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.NOT_FOUND)
                ))
                .map(item -> {
                    model.addAttribute("item", item);
                    return "item";
                });
    }

    @PostMapping("/items/{id}")
    public Mono<String> changeItemQuantity(
            @PathVariable @Positive Long id,
            @RequestParam Action action,
            Model model
    ) {
        return cartService.changeItemQuantity(id, action)
                .then(itemFacade.getItem(id))
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.NOT_FOUND)
                ))
                .map(item -> {
                    model.addAttribute("item", item);
                    return "item";
                });
    }

    private List<List<ItemDto>> groupByThree(List<ItemDto> items) {
        if (CollectionUtils.isEmpty(items)) {
            return List.of();
        }

        List<List<ItemDto>> grouped = new ArrayList<>();

        for (int i = 0; i < items.size(); i += 3) {
            final List<ItemDto> row = new ArrayList<>(items.subList(i, Math.min(i + 3, items.size())));

            while (row.size() < 3) {
                row.add(ItemDto.builder().id(-1L).build());
            }

            grouped.add(row);
        }

        return grouped;
    }
}
