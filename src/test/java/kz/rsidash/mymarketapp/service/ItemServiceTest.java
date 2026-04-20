package kz.rsidash.mymarketapp.service;

import kz.rsidash.mymarketapp.model.enums.SortType;
import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    void getItems_noSearch_callsFindAll() {
        when(itemRepository.findAll(SortType.NO.toSort())).thenReturn(Flux.empty());

        StepVerifier.create(itemService.getItems(null, PageRequest.of(0, 10, SortType.NO.toSort())))
                .verifyComplete();

        verify(itemRepository).findAll(SortType.NO.toSort());
        verify(itemRepository, never()).findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(any(), any(), any());
    }

    @Test
    void getItems_withSearch_callsSearchMethod() {
        var item = new Item();
        item.setId(1L);
        item.setTitle("Ball");
        item.setPrice(100);

        when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("ball", "ball", PageRequest.of(0, 10, SortType.NO.toSort())))
                .thenReturn(Flux.just(item));

        StepVerifier.create(itemService.getItems("ball", PageRequest.of(0, 10, SortType.NO.toSort())))
                .expectNextCount(1)
                .verifyComplete();

        verify(itemRepository).findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("ball", "ball", PageRequest.of(0, 10, SortType.NO.toSort()));
    }

    @Test
    void getItems_blankSearch_callsFindAll() {
        when(itemRepository.findAll(SortType.NO.toSort())).thenReturn(Flux.empty());

        StepVerifier.create(itemService.getItems("  ", PageRequest.of(0, 10, SortType.NO.toSort())))
                .verifyComplete();

        verify(itemRepository).findAll(SortType.NO.toSort());
    }

    @Test
    void getItems_sortAlpha_sortsByTitle() {
        var itemB = new Item();
        itemB.setId(1L);
        itemB.setTitle("Bat");
        itemB.setPrice(200);

        var itemA = new Item();
        itemA.setId(2L);
        itemA.setTitle("Apple");
        itemA.setPrice(50);

        when(itemRepository.findAll(SortType.ALPHA.toSort())).thenReturn(Flux.just(itemA, itemB));

        StepVerifier.create(itemService.getItems(null, PageRequest.of(0, 10, SortType.ALPHA.toSort())))
                .assertNext(i -> assertThat(i.getTitle()).isEqualTo("Apple"))
                .assertNext(i -> assertThat(i.getTitle()).isEqualTo("Bat"))
                .verifyComplete();
    }

    @Test
    void getItems_sortPrice_sortsByPrice() {
        var expensive = new Item();
        expensive.setId(1L);
        expensive.setTitle("Bat");
        expensive.setPrice(200);

        var cheap = new Item();
        cheap.setId(2L);
        cheap.setTitle("Ball");
        cheap.setPrice(50);

        when(itemRepository.findAll(SortType.PRICE.toSort())).thenReturn(Flux.just(cheap, expensive));

        StepVerifier.create(itemService.getItems(null, PageRequest.of(0, 10, SortType.PRICE.toSort())))
                .assertNext(i -> assertThat(i.getPrice()).isEqualTo(50))
                .assertNext(i -> assertThat(i.getPrice()).isEqualTo(200))
                .verifyComplete();
    }

    @Test
    void getItem_found() {
        var item = new Item();
        item.setId(1L);
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));

        StepVerifier.create(itemService.getItem(1L))
                .assertNext(i -> assertThat(i.getId()).isEqualTo(1L))
                .verifyComplete();
    }

    @Test
    void getItem_notFound() {
        when(itemRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(itemService.getItem(999L))
                .verifyComplete();
    }
}
