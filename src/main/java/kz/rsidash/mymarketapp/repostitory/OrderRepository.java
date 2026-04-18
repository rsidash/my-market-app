package kz.rsidash.mymarketapp.repostitory;

import kz.rsidash.mymarketapp.model.order.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
}
