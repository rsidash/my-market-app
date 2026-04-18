package kz.rsidash.mymarketapp.service;

import kz.rsidash.mymarketapp.model.enums.SortType;
import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.repostitory.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    void getItems_noSearch_callsFindAll() {
        var pageable = PageRequest.of(0, 5);
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        itemService.getItems(null, SortType.NO, pageable);

        verify(itemRepository).findAll(any(Pageable.class));
        verify(itemRepository, never()).findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(any(), any(), any());
    }

    @Test
    void getItems_withSearch_callsSearchMethod() {
        var pageable = PageRequest.of(0, 5);
        when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                eq("ball"), eq("ball"), any(Pageable.class))).thenReturn(Page.empty());

        itemService.getItems("ball", SortType.NO, pageable);

        verify(itemRepository).findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                eq("ball"), eq("ball"), any(Pageable.class));
    }

    @Test
    void getItems_blankSearch_callsFindAll() {
        var pageable = PageRequest.of(0, 5);
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        itemService.getItems("  ", SortType.NO, pageable);

        verify(itemRepository).findAll(any(Pageable.class));
    }

    @Test
    void getItems_sortAlpha_appliesTitleSort() {
        var pageable = PageRequest.of(0, 5);
        var item = new Item();
        item.setId(1L);
        item.setTitle("A");
        when(itemRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item)));

        var result = itemService.getItems(null, SortType.ALPHA, pageable);

        verify(itemRepository).findAll(argThat((Pageable p) ->
                p.getSort().getOrderFor("title") != null));
    }

    @Test
    void getItems_sortPrice_appliesPriceSort() {
        var pageable = PageRequest.of(0, 5);
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        itemService.getItems(null, SortType.PRICE, pageable);

        verify(itemRepository).findAll(argThat((Pageable p) ->
                p.getSort().getOrderFor("price") != null));
    }

    @Test
    void getItem_found() {
        var item = new Item();
        item.setId(1L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        var result = itemService.getItem(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void getItem_notFound() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        var result = itemService.getItem(999L);

        assertThat(result).isEmpty();
    }
}
