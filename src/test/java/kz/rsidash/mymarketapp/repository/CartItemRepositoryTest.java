package kz.rsidash.mymarketapp.repository;

import kz.rsidash.mymarketapp.model.cart.CartItem;
import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.repostitory.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CartItemRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CartItemRepository cartItemRepository;

    private Item item;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setTitle("Ball");
        item.setPrice(100);
        em.persist(item);
        em.flush();
    }

    @Test
    void findByItemId_found() {
        var cartItem = CartItem.builder().item(item).count(3).build();
        em.persist(cartItem);
        em.flush();

        var result = cartItemRepository.findByItemId(item.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getCount()).isEqualTo(3);
        assertThat(result.get().getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    void findByItemId_notFound() {
        var result = cartItemRepository.findByItemId(999L);
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_returnsAllCartItems() {
        var ci1 = CartItem.builder().item(item).count(2).build();
        em.persist(ci1);

        var item2 = new Item();
        item2.setTitle("Bat");
        item2.setPrice(200);
        em.persist(item2);

        var ci2 = CartItem.builder().item(item2).count(1).build();
        em.persist(ci2);
        em.flush();

        var result = cartItemRepository.findAll();
        assertThat(result).hasSize(2);
    }

    @Test
    void deleteAll_clearsCart() {
        var cartItem = CartItem.builder().item(item).count(1).build();
        em.persist(cartItem);
        em.flush();

        cartItemRepository.deleteAll();

        assertThat(cartItemRepository.findAll()).isEmpty();
    }
}
