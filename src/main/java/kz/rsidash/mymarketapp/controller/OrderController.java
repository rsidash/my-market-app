package kz.rsidash.mymarketapp.controller;

import jakarta.validation.constraints.Positive;
import kz.rsidash.mymarketapp.dto.order.mapper.OrderItemMapper;
import kz.rsidash.mymarketapp.dto.order.mapper.OrderMapper;
import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.repository.ItemRepository;
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
import reactor.core.publisher.Mono;

@Controller
@Validated
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ItemRepository itemRepository;

    @GetMapping("/orders")
    public Mono<String> getOrders(Model model) {
        return orderService.getOrders()
                .flatMap(order ->
                        orderService.getOrderItems(order.getId())
                                .flatMap(oi -> itemRepository.findById(oi.getItemId())
                                        .defaultIfEmpty(new Item())
                                        .map(item -> orderItemMapper.toDto(oi, item)))
                                .collectList()
                                .map(items -> orderMapper.toDto(order, items))
                )
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
        return orderService.getOrder(id)
                .flatMap(order ->
                        orderService.getOrderItems(order.getId())
                                .flatMap(oi -> itemRepository.findById(oi.getItemId())
                                        .defaultIfEmpty(new Item())
                                        .map(item -> orderItemMapper.toDto(oi, item)))
                                .collectList()
                                .map(items -> orderMapper.toDto(order, items))
                )
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
        return orderService.createOrder()
                .map(order ->
                        "redirect:/orders/" + order.getId() + "?newOrder=true"
                );
    }
}
