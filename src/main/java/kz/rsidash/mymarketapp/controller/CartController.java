package kz.rsidash.mymarketapp.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import kz.rsidash.mymarketapp.dto.item.ItemDto;
import kz.rsidash.mymarketapp.dto.item.mapper.ItemMapper;
import kz.rsidash.mymarketapp.model.enums.Action;
import kz.rsidash.mymarketapp.model.enums.SortType;
import kz.rsidash.mymarketapp.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Objects;

@Controller
@Validated
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final ItemMapper itemMapper;

    @PostMapping("/items")
    public String changeItemQuantityFromItems(
            @RequestParam @Positive long id,
            @RequestParam Action action,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "NO") SortType sort,
            @RequestParam(defaultValue = "1") @Min(1) int pageNumber,
            @RequestParam(defaultValue = "5") @Positive int pageSize
    ) {
        cartService.changeItemQuantity(id, action);
        return "redirect:/items?search=" + (Objects.nonNull(search) ? search : "")
                + "&sort=" + sort
                + "&pageNumber=" + pageNumber
                + "&pageSize=" + pageSize;
    }

    @GetMapping("/cart/items")
    public String getCart(Model model) {
        populateCartModel(model);
        return "cart";
    }

    @PostMapping("/cart/items")
    public String changeItemQuantityFromCart(
            @RequestParam @Positive long id,
            @RequestParam Action action,
            Model model
    ) {
        cartService.changeItemQuantity(id, action);
        populateCartModel(model);
        return "cart";
    }

    private void populateCartModel(Model model) {
        final var items = cartService.getCartItems().stream()
                .map(ci -> itemMapper.toDto(ci.getItem(), ci.getCount()))
                .toList();

        final var total = items.stream()
                .mapToLong(i -> i.getPrice() * i.getCount())
                .sum();

        model.addAttribute("items", items);
        model.addAttribute("total", total);
    }
}
