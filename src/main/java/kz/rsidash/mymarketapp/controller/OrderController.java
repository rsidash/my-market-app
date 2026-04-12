package kz.rsidash.mymarketapp.controller;

import jakarta.validation.constraints.Positive;
import kz.rsidash.mymarketapp.dto.order.mapper.OrderMapper;
import kz.rsidash.mymarketapp.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
@Validated
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @GetMapping("/orders")
    public String getOrders(Model model) {
        final var orders = orderService.getOrders().stream()
                .map(orderMapper::toDto)
                .toList();

        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String getOrderById(
            @PathVariable @Positive long id,
            @RequestParam(defaultValue = "false") boolean newOrder,
            Model model
    ) {
        final var order = orderService.getOrder(id)
                .map(orderMapper::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("order", order);
        model.addAttribute("newOrder", newOrder);
        return "order";
    }

    @PostMapping("/buy")
    public String buy() {
        final var order = orderService.createOrder();
        return "redirect:/orders/" + order.getId() + "?newOrder=true";
    }
}
