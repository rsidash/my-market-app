package kz.rsidash.mymarketapp.controller;

import kz.rsidash.mymarketapp.dto.order.OrderDto;
import kz.rsidash.mymarketapp.dto.order.mapper.OrderMapper;
import kz.rsidash.mymarketapp.model.order.Order;
import kz.rsidash.mymarketapp.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderMapper orderMapper;

    @Test
    void getOrders_returnsOrdersTemplate() throws Exception {
        when(orderService.getOrders()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    void getOrders_withOrders() throws Exception {
        var order = new Order();
        order.setId(1L);
        order.setTotalSum(500L);
        order.setItems(Collections.emptyList());

        var dto = OrderDto.builder().id(1).items(Collections.emptyList()).totalSum(500).build();

        when(orderService.getOrders()).thenReturn(List.of(order));
        when(orderMapper.toDto(order)).thenReturn(dto);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("orders", List.of(dto)));
    }

    @Test
    void getOrderById_returnsOrderTemplate() throws Exception {
        var order = new Order();
        order.setId(1L);
        order.setTotalSum(300L);
        order.setItems(Collections.emptyList());

        var dto = OrderDto.builder().id(1).items(Collections.emptyList()).totalSum(300).build();

        when(orderService.getOrder(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(dto);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("order", dto))
                .andExpect(model().attribute("newOrder", false));
    }

    @Test
    void getOrderById_withNewOrderTrue() throws Exception {
        var order = new Order();
        order.setId(1L);
        order.setTotalSum(300L);
        order.setItems(Collections.emptyList());

        var dto = OrderDto.builder().id(1).items(Collections.emptyList()).totalSum(300).build();

        when(orderService.getOrder(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(dto);

        mockMvc.perform(get("/orders/1").param("newOrder", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("newOrder", true));
    }

    @Test
    void getOrderById_notFound() throws Exception {
        when(orderService.getOrder(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/orders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void buy_redirectsToOrder() throws Exception {
        var order = new Order();
        order.setId(42L);

        when(orderService.createOrder()).thenReturn(order);

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/42?newOrder=true"));
    }
}
