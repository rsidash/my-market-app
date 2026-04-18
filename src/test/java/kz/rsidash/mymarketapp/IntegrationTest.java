package kz.rsidash.mymarketapp;

import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.repostitory.CartItemRepository;
import kz.rsidash.mymarketapp.repostitory.ItemRepository;
import kz.rsidash.mymarketapp.repostitory.OrderItemRepository;
import kz.rsidash.mymarketapp.repostitory.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
class IntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll().block();
        cartItemRepository.deleteAll().block();
        orderRepository.deleteAll().block();
        itemRepository.deleteAll().block();

        var ball = new Item();
        ball.setTitle("Ball");
        ball.setDescription("A rubber ball");
        ball.setPrice(100);
        itemRepository.save(ball).block();

        var bat = new Item();
        bat.setTitle("Bat");
        bat.setDescription("Wooden bat");
        bat.setPrice(200);
        itemRepository.save(bat).block();
    }

    @Test
    void getItems_returnsItemsPage() {
        webTestClient.get().uri("/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getRootPath_returnsItemsPage() {
        webTestClient.get().uri("/")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getItemById_returnsItemPage() {
        var item = itemRepository.findAll().blockFirst();

        webTestClient.get().uri("/items/" + item.getId())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void addToCart_andViewCart() {
        var item = itemRepository.findAll().blockFirst();

        webTestClient.post().uri(uriBuilder -> uriBuilder.path("/items/" + item.getId())
                        .queryParam("action", "PLUS")
                        .build())
                .exchange()
                .expectStatus().isOk();

        webTestClient.get().uri("/cart/items")
                .exchange()
                .expectStatus().isOk();

        assertThat(cartItemRepository.findAll().collectList().block()).hasSize(1);
    }

    @Test
    void addToCart_fromItemsPage_redirects() {
        var item = itemRepository.findAll().blockFirst();

        webTestClient.post().uri(uriBuilder -> uriBuilder.path("/items")
                        .queryParam("id", String.valueOf(item.getId()))
                        .queryParam("action", "PLUS")
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection();

        assertThat(cartItemRepository.findByItemId(item.getId()).block()).isNotNull();
    }

    @Test
    void fullPurchaseFlow() {
        var items = itemRepository.findAll().collectList().block();
        var item1 = items.get(0);
        var item2 = items.get(1);

        webTestClient.post().uri(uriBuilder -> uriBuilder.path("/items/" + item1.getId())
                .queryParam("action", "PLUS").build()).exchange();
        webTestClient.post().uri(uriBuilder -> uriBuilder.path("/items/" + item1.getId())
                .queryParam("action", "PLUS").build()).exchange();
        webTestClient.post().uri(uriBuilder -> uriBuilder.path("/items/" + item2.getId())
                .queryParam("action", "PLUS").build()).exchange();

        assertThat(cartItemRepository.findAll().collectList().block()).hasSize(2);

        webTestClient.post().uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection();

        assertThat(cartItemRepository.findAll().collectList().block()).isEmpty();

        var orders = orderRepository.findAll().collectList().block();
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getTotalSum()).isEqualTo(400L);
    }

    @Test
    void viewOrders_afterPurchase() {
        var item = itemRepository.findAll().blockFirst();

        webTestClient.post().uri(uriBuilder -> uriBuilder.path("/items/" + item.getId())
                .queryParam("action", "PLUS").build()).exchange();
        webTestClient.post().uri("/buy").exchange();

        webTestClient.get().uri("/orders")
                .exchange()
                .expectStatus().isOk();

        var order = orderRepository.findAll().blockFirst();
        webTestClient.get().uri("/orders/" + order.getId())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void deleteFromCart() {
        var item = itemRepository.findAll().blockFirst();

        webTestClient.post().uri(uriBuilder -> uriBuilder.path("/items/" + item.getId())
                .queryParam("action", "PLUS").build()).exchange();
        assertThat(cartItemRepository.findAll().collectList().block()).hasSize(1);

        webTestClient.post().uri(uriBuilder -> uriBuilder.path("/cart/items")
                        .queryParam("id", String.valueOf(item.getId()))
                        .queryParam("action", "DELETE")
                        .build())
                .exchange()
                .expectStatus().isOk();

        assertThat(cartItemRepository.findAll().collectList().block()).isEmpty();
    }

    @Test
    void searchItems() {
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/items")
                        .queryParam("search", "ball")
                        .build())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getItem_notFound_returns404() {
        webTestClient.get().uri("/items/99999")
                .exchange()
                .expectStatus().isNotFound();
    }
}
