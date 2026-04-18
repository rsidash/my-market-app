package kz.rsidash.mymarketapp.repository;

import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.repostitory.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
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
    void findAll_returnsAllItems() {
        StepVerifier.create(itemRepository.findAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findById_found() {
        var item = itemRepository.findAll().blockFirst();

        StepVerifier.create(itemRepository.findById(item.getId()))
                .assertNext(i -> assertThat(i.getTitle()).isEqualTo(item.getTitle()))
                .verifyComplete();
    }

    @Test
    void findById_notFound() {
        StepVerifier.create(itemRepository.findById(999L))
                .verifyComplete();
    }

    @Test
    void searchByTitle_caseInsensitive() {
        StepVerifier.create(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("ball", "ball"))
                .assertNext(i -> assertThat(i.getTitle()).isEqualTo("Ball"))
                .verifyComplete();
    }

    @Test
    void searchByDescription() {
        StepVerifier.create(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("rubber", "rubber"))
                .assertNext(i -> assertThat(i.getTitle()).isEqualTo("Ball"))
                .verifyComplete();
    }

    @Test
    void searchNoMatch() {
        StepVerifier.create(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("xyz", "xyz"))
                .verifyComplete();
    }
}
