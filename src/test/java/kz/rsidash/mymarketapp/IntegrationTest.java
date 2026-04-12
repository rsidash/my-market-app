package kz.rsidash.mymarketapp;

import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.repostitory.CartItemRepository;
import kz.rsidash.mymarketapp.repostitory.ItemRepository;
import kz.rsidash.mymarketapp.repostitory.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        orderRepository.deleteAll();
        itemRepository.deleteAll();

        var ball = new Item();
        ball.setTitle("Ball");
        ball.setDescription("A rubber ball");
        ball.setPrice(100);
        itemRepository.save(ball);

        var bat = new Item();
        bat.setTitle("Bat");
        bat.setDescription("Wooden bat");
        bat.setPrice(200);
        itemRepository.save(bat);
    }

    @Test
    void getItems_returnsItemsPage() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attributeExists("items", "sort", "paging"));
    }

    @Test
    void getRootPath_returnsItemsPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"));
    }

    @Test
    void getItemById_returnsItemPage() throws Exception {
        var item = itemRepository.findAll().get(0);

        mockMvc.perform(get("/items/" + item.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attributeExists("item"));
    }

    @Test
    void addToCart_andViewCart() throws Exception {
        var item = itemRepository.findAll().get(0);

        // Add to cart from item page
        mockMvc.perform(post("/items/" + item.getId()).param("action", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"));

        // View cart
        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("items", "total"));

        assertThat(cartItemRepository.findAll()).hasSize(1);
    }

    @Test
    void addToCart_fromItemsPage_redirects() throws Exception {
        var item = itemRepository.findAll().get(0);

        mockMvc.perform(post("/items")
                        .param("id", String.valueOf(item.getId()))
                        .param("action", "PLUS"))
                .andExpect(status().is3xxRedirection());

        assertThat(cartItemRepository.findByItemId(item.getId())).isPresent();
    }

    @Test
    void fullPurchaseFlow() throws Exception {
        var items = itemRepository.findAll();
        var item1 = items.get(0);
        var item2 = items.get(1);

        // Add items to cart
        mockMvc.perform(post("/items/" + item1.getId()).param("action", "PLUS"));
        mockMvc.perform(post("/items/" + item1.getId()).param("action", "PLUS"));
        mockMvc.perform(post("/items/" + item2.getId()).param("action", "PLUS"));

        // Verify cart
        assertThat(cartItemRepository.findAll()).hasSize(2);

        // Buy
        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection());

        // Cart should be empty
        assertThat(cartItemRepository.findAll()).isEmpty();

        // Order should exist
        var orders = orderRepository.findAll();
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getTotalSum()).isEqualTo(400L); // 100*2 + 200*1
    }

    @Test
    void viewOrders_afterPurchase() throws Exception {
        var item = itemRepository.findAll().get(0);

        // Add and buy
        mockMvc.perform(post("/items/" + item.getId()).param("action", "PLUS"));
        mockMvc.perform(post("/buy"));

        // View orders list
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"));

        // View specific order
        var order = orderRepository.findAll().get(0);
        mockMvc.perform(get("/orders/" + order.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attribute("newOrder", false));
    }

    @Test
    void deleteFromCart() throws Exception {
        var item = itemRepository.findAll().get(0);

        // Add to cart
        mockMvc.perform(post("/items/" + item.getId()).param("action", "PLUS"));
        assertThat(cartItemRepository.findAll()).hasSize(1);

        // Delete from cart
        mockMvc.perform(post("/cart/items")
                        .param("id", String.valueOf(item.getId()))
                        .param("action", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        assertThat(cartItemRepository.findAll()).isEmpty();
    }

    @Test
    void searchItems() throws Exception {
        mockMvc.perform(get("/items").param("search", "ball"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"));
    }

    @Test
    void getItem_notFound_returns404() throws Exception {
        mockMvc.perform(get("/items/99999"))
                .andExpect(status().isNotFound());
    }
}
