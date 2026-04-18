package kz.rsidash.mymarketapp.controller;

import kz.rsidash.mymarketapp.dto.Paging;
import kz.rsidash.mymarketapp.dto.item.ItemDto;
import kz.rsidash.mymarketapp.dto.item.ItemListView;
import kz.rsidash.mymarketapp.facade.ItemFacade;
import kz.rsidash.mymarketapp.model.enums.SortType;
import kz.rsidash.mymarketapp.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemFacade itemFacade;

    @MockitoBean
    private CartService cartService;

    @Test
    void getItems_returnsItemsTemplate() throws Exception {
        var itemListView = ItemListView.builder()
                .items(List.of(
                        ItemDto.builder().id(1).title("Item1").price(100).build(),
                        ItemDto.builder().id(2).title("Item2").price(200).build()
                ))
                .search(null)
                .sort(SortType.NO)
                .paging(Paging.builder().pageNumber(1).pageSize(5).hasPrevious(false).hasNext(false).build())
                .build();

        when(itemFacade.getItems(isNull(), eq(SortType.NO), eq(0), eq(5))).thenReturn(itemListView);

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attributeExists("items", "sort", "paging"));
    }

    @Test
    void getItems_rootPath_returnsItemsTemplate() throws Exception {
        var itemListView = ItemListView.builder()
                .items(List.of(ItemDto.builder().id(1).title("Item1").price(100).build()))
                .sort(SortType.NO)
                .paging(Paging.builder().pageNumber(1).pageSize(5).hasPrevious(false).hasNext(false).build())
                .build();

        when(itemFacade.getItems(isNull(), eq(SortType.NO), eq(0), eq(5))).thenReturn(itemListView);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"));
    }

    @Test
    void getItems_withSearchAndSort() throws Exception {
        var itemListView = ItemListView.builder()
                .items(List.of(ItemDto.builder().id(1).title("Ball").price(50).build()))
                .search("ball")
                .sort(SortType.ALPHA)
                .paging(Paging.builder().pageNumber(1).pageSize(5).hasPrevious(false).hasNext(false).build())
                .build();

        when(itemFacade.getItems(eq("ball"), eq(SortType.ALPHA), eq(0), eq(5))).thenReturn(itemListView);

        mockMvc.perform(get("/items")
                        .param("search", "ball")
                        .param("sort", "ALPHA"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attribute("search", "ball"))
                .andExpect(model().attribute("sort", SortType.ALPHA));
    }

    @Test
    void getItems_groupsByThreeWithStubs() throws Exception {
        var itemListView = ItemListView.builder()
                .items(List.of(
                        ItemDto.builder().id(1).title("A").price(10).build(),
                        ItemDto.builder().id(2).title("B").price(20).build()
                ))
                .sort(SortType.NO)
                .paging(Paging.builder().pageNumber(1).pageSize(5).hasPrevious(false).hasNext(false).build())
                .build();

        when(itemFacade.getItems(any(), any(), anyInt(), anyInt())).thenReturn(itemListView);

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("items"));
    }

    @Test
    void getItem_returnsItemTemplate() throws Exception {
        var item = ItemDto.builder().id(1).title("Ball").description("A ball").price(100).count(0).build();
        when(itemFacade.getItem(1L)).thenReturn(Optional.of(item));

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", item));
    }

    @Test
    void getItem_notFound() throws Exception {
        when(itemFacade.getItem(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/items/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void changeItemQuantity_returnsItemTemplate() throws Exception {
        var item = ItemDto.builder().id(1).title("Ball").price(100).count(1).build();
        when(itemFacade.getItem(1L)).thenReturn(Optional.of(item));

        mockMvc.perform(post("/items/1").param("action", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", item));

        verify(cartService).changeItemQuantity(1L, kz.rsidash.mymarketapp.model.enums.Action.PLUS);
    }

    @Test
    void changeItemQuantity_minus() throws Exception {
        var item = ItemDto.builder().id(1).title("Ball").price(100).count(0).build();
        when(itemFacade.getItem(1L)).thenReturn(Optional.of(item));

        mockMvc.perform(post("/items/1").param("action", "MINUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"));

        verify(cartService).changeItemQuantity(1L, kz.rsidash.mymarketapp.model.enums.Action.MINUS);
    }
}
