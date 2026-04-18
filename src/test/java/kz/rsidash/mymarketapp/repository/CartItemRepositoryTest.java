package kz.rsidash.mymarketapp.repository;

import kz.rsidash.mymarketapp.model.cart.CartItem;
import kz.rsidash.mymarketapp.repostitory.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll().block();
    }

    @Test
    void findByItemId_found() {
        var cartItem = CartItem.builder().itemId(1L).count(3).build();
        cartItemRepository.save(cartItem).block();

        StepVerifier.create(cartItemRepository.findByItemId(1L))
                .assertNext(ci -> {
                    assertThat(ci.getCount()).isEqualTo(3);
                    assertThat(ci.getItemId()).isEqualTo(1L);
                })
                .verifyComplete();
    }

    @Test
    void findByItemId_notFound() {
        StepVerifier.create(cartItemRepository.findByItemId(999L))
                .verifyComplete();
    }

    @Test
    void findAll_returnsAllCartItems() {
        cartItemRepository.save(CartItem.builder().itemId(1L).count(2).build()).block();
        cartItemRepository.save(CartItem.builder().itemId(2L).count(1).build()).block();

        StepVerifier.create(cartItemRepository.findAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void deleteAll_clearsCart() {
        cartItemRepository.save(CartItem.builder().itemId(1L).count(1).build()).block();

        cartItemRepository.deleteAll().block();

        StepVerifier.create(cartItemRepository.findAll())
                .verifyComplete();
    }
}
