package kz.rsidash.mymarketapp.mapper;

import kz.rsidash.mymarketapp.dto.item.mapper.ItemMapper;
import kz.rsidash.mymarketapp.dto.order.mapper.OrderItemMapper;
import kz.rsidash.mymarketapp.model.item.Item;
import kz.rsidash.mymarketapp.model.order.OrderItem;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

    private final ItemMapper itemMapper = new ItemMapper();
    private final OrderItemMapper orderItemMapper = new OrderItemMapper();

    @Test
    void itemMapper_mapsAllFields() {
        var item = new Item();
        item.setId(1L);
        item.setTitle("Ball");
        item.setDescription("A ball");
        item.setImgPth("/images/ball.jpg");
        item.setPrice(100);

        var dto = itemMapper.toDto(item, 3);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("Ball");
        assertThat(dto.getDescription()).isEqualTo("A ball");
        assertThat(dto.getImgPath()).isEqualTo("/images/ball.jpg");
        assertThat(dto.getPrice()).isEqualTo(100);
        assertThat(dto.getCount()).isEqualTo(3);
    }

    @Test
    void itemMapper_zeroCount() {
        var item = new Item();
        item.setId(2L);
        item.setTitle("Bat");
        item.setPrice(50);

        var dto = itemMapper.toDto(item, 0);

        assertThat(dto.getCount()).isZero();
    }

    @Test
    void orderItemMapper_mapsWithItem() {
        var item = new Item();
        item.setId(1L);
        item.setTitle("Ball");
        item.setPrice(100);

        var orderItem = new OrderItem();
        orderItem.setId(10L);
        orderItem.setItem(item);
        orderItem.setCount(2);

        var dto = orderItemMapper.toDto(orderItem);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getTitle()).isEqualTo("Ball");
        assertThat(dto.getPrice()).isEqualTo(100);
        assertThat(dto.getCount()).isEqualTo(2);
    }

    @Test
    void orderItemMapper_mapsWithNullItem() {
        var orderItem = new OrderItem();
        orderItem.setId(10L);
        orderItem.setItem(null);
        orderItem.setCount(1);

        var dto = orderItemMapper.toDto(orderItem);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getTitle()).isNull();
        assertThat(dto.getPrice()).isZero();
        assertThat(dto.getCount()).isEqualTo(1);
    }
}
