package kz.rsidash.mymarketapp.repository;

import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.repostitory.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ItemRepository itemRepository;

    private Item ball;
    private Item bat;

    @BeforeEach
    void setUp() {
        ball = new Item();
        ball.setTitle("Ball");
        ball.setDescription("A rubber ball");
        ball.setPrice(100);
        em.persist(ball);

        bat = new Item();
        bat.setTitle("Bat");
        bat.setDescription("Wooden bat");
        bat.setPrice(200);
        em.persist(bat);

        em.flush();
    }

    @Test
    void findAll_returnsAllItems() {
        var page = itemRepository.findAll(PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void findAll_pagination() {
        var page1 = itemRepository.findAll(PageRequest.of(0, 1));
        assertThat(page1.getContent()).hasSize(1);
        assertThat(page1.getTotalPages()).isEqualTo(2);
        assertThat(page1.hasNext()).isTrue();

        var page2 = itemRepository.findAll(PageRequest.of(1, 1));
        assertThat(page2.getContent()).hasSize(1);
        assertThat(page2.hasPrevious()).isTrue();
    }

    @Test
    void findAll_sortByTitle() {
        var page = itemRepository.findAll(PageRequest.of(0, 10, Sort.by("title")));
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("Ball");
        assertThat(page.getContent().get(1).getTitle()).isEqualTo("Bat");
    }

    @Test
    void findAll_sortByPrice() {
        var page = itemRepository.findAll(PageRequest.of(0, 10, Sort.by("price")));
        assertThat(page.getContent().get(0).getPrice()).isEqualTo(100);
        assertThat(page.getContent().get(1).getPrice()).isEqualTo(200);
    }

    @Test
    void findById_found() {
        var result = itemRepository.findById(ball.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Ball");
    }

    @Test
    void findById_notFound() {
        var result = itemRepository.findById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    void searchByTitle_caseInsensitive() {
        var page = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "ball", "ball", PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("Ball");
    }

    @Test
    void searchByDescription() {
        var page = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "rubber", "rubber", PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("Ball");
    }

    @Test
    void searchNoMatch() {
        var page = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "xyz", "xyz", PageRequest.of(0, 10));
        assertThat(page.getContent()).isEmpty();
    }
}
