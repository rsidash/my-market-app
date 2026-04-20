package kz.rsidash.mymarketapp.controller;

import jakarta.validation.constraints.Positive;
import kz.rsidash.mymarketapp.facade.OrderFacade;
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
import reactor.core.publisher.Mono;

@Controller
@Validated
@RequiredArgsConstructor
public class OrderController {
    private final OrderFacade orderFacade;

    @GetMapping("/orders")
    public Mono<String> getOrders(Model model) {
        return orderFacade.getOrders()
                .collectList()
                .map(orders -> {
                    model.addAttribute("orders", orders);
                    return "orders";
                });
    }

    @GetMapping("/orders/{id}")
    public Mono<String> getOrderById(
            @PathVariable @Positive long id,
            @RequestParam(defaultValue = "false") boolean newOrder,
            Model model
    ) {
        return orderFacade.getOrder(id)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.NOT_FOUND)
                ))
                .map(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("newOrder", newOrder);
                    return "order";
                });
    }

    @PostMapping("/buy")
    public Mono<String> buy() {
        return orderFacade.createOrder()
                .map(order ->
                        "redirect:/orders/" + order.getId() + "?newOrder=true"
                );
    }
}
