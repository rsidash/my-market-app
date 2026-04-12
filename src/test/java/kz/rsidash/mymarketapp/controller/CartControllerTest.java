package kz.rsidash.mymarketapp.controller;

import kz.rsidash.mymarketapp.dto.item.mapper.ItemMapper;
import kz.rsidash.mymarketapp.model.cart.CartItem;
import kz.rsidash.mymarketapp.model.enums.Action;
import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private ItemMapper itemMapper;

    @Test
    void postItems_redirectsWithParams() throws Exception {
        mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("action", "PLUS")
                        .param("search", "ball")
                        .param("sort", "ALPHA")
                        .param("pageNumber", "2")
                        .param("pageSize", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?search=ball&sort=ALPHA&pageNumber=2&pageSize=10"));

        verify(cartService).changeItemQuantity(1L, Action.PLUS);
    }

    @Test
    void postItems_redirectsWithDefaults() throws Exception {
        mockMvc.perform(post("/items")
                        .param("id", "5")
                        .param("action", "MINUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?search=&sort=NO&pageNumber=1&pageSize=5"));

        verify(cartService).changeItemQuantity(5L, Action.MINUS);
    }

    @Test
    void getCartItems_returnsCartTemplate() throws Exception {
        when(cartService.getCartItems()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("items", "total"))
                .andExpect(model().attribute("total", 0L));
    }

    @Test
    void getCartItems_withItems() throws Exception {
        var item = new Item();
        item.setId(1L);
        item.setTitle("Ball");
        item.setPrice(100);

        var cartItem = CartItem.builder().id(1L).item(item).count(2).build();
        when(cartService.getCartItems()).thenReturn(List.of(cartItem));

        var dto = kz.rsidash.mymarketapp.dto.item.ItemDto.builder()
                .id(1).title("Ball").price(100).count(2).build();
        when(itemMapper.toDto(item, 2)).thenReturn(dto);

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("total", 200L));
    }

    @Test
    void postCartItems_changesQuantityAndReturnsCart() throws Exception {
        when(cartService.getCartItems()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("items", "total"));

        verify(cartService).changeItemQuantity(1L, Action.PLUS);
    }

    @Test
    void postCartItems_deleteAction() throws Exception {
        when(cartService.getCartItems()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/cart/items")
                        .param("id", "3")
                        .param("action", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        verify(cartService).changeItemQuantity(3L, Action.DELETE);
    }
}
