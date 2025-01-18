package pl.aplazuk.orderms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.aplazuk.orderms.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
