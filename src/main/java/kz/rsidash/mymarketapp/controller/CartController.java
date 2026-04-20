package kz.rsidash.mymarketapp.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import kz.rsidash.mymarketapp.dto.item.mapper.ItemMapper;
import kz.rsidash.mymarketapp.model.enums.Action;
import kz.rsidash.mymarketapp.model.enums.SortType;
import kz.rsidash.mymarketapp.service.CartService;
import kz.rsidash.mymarketapp.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

@Controller
@Validated
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final ItemMapper itemMapper;
    private final ItemService itemService;

    @PostMapping("/items")
    public Mono<String> changeItemQuantityFromItems(
            @RequestParam @Positive long id,
            @RequestParam Action action,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "NO") SortType sort,
            @RequestParam(defaultValue = "1") @Min(1) int pageNumber,
            @RequestParam(defaultValue = "5") @Positive int pageSize
    ) {
        return cartService.changeItemQuantity(id, action)
                .thenReturn(buildRedirect(search, sort, pageNumber, pageSize));
    }

    @GetMapping("/cart/items")
    public Mono<String> getCart(Model model) {

        return populateCartModel(model)
                .thenReturn("cart");
    }

    @PostMapping("/cart/items")
    public Mono<String> changeItemQuantityFromCart(
            @RequestParam @Positive long id,
            @RequestParam Action action,
            Model model
    ) {
        return cartService.changeItemQuantity(id, action)
                .then(populateCartModel(model))
                .thenReturn("cart");
    }

    private Mono<Void> populateCartModel(Model model) {

        return cartService.getCartItems()
                .flatMap(ci ->
                        itemService.getItem(ci.getItemId())
                                .map(item -> itemMapper.toDto(item, ci.getCount()))
                )
                .collectList()
                .map(items -> {

                    long total = items.stream()
                            .mapToLong(i -> i.getPrice() * i.getCount())
                            .sum();

                    model.addAttribute("items", items);
                    model.addAttribute("total", total);

                    return items;
                })
                .then();
    }

    private String buildRedirect(String search, SortType sort, int pageNumber, int pageSize) {
        return "redirect:/items?search=" + (search != null ? search : "")
                + "&sort=" + sort
                + "&pageNumber=" + pageNumber
                + "&pageSize=" + pageSize;
    }
}
