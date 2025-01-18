package pl.aplazuk.orderms.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.aplazuk.orderms.dto.OrderDTO;
import pl.aplazuk.orderms.model.Order;
import pl.aplazuk.orderms.service.OrderService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{category}")
    public ResponseEntity<?> getOrderWithProductsListByCategory(@PathVariable(name = "category") String productCategory) {
        Optional<OrderDTO> orderDTO = orderService.collectOrderByProductListWithCategory(productCategory);
        return orderDTO.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());

    }

}
