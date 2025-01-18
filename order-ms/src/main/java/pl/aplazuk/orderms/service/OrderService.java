package pl.aplazuk.orderms.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pl.aplazuk.orderms.dto.OrderDTO;
import pl.aplazuk.orderms.dto.ProductDTO;
import pl.aplazuk.orderms.model.Order;
import pl.aplazuk.orderms.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderService {


    private final OrderRepository orderRepository;
    private final RestClient.Builder restClient;

    public OrderService(OrderRepository orderRepository, RestClient.Builder restClient) {
        this.orderRepository = orderRepository;
        this.restClient = restClient;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<OrderDTO> collectOrderByProductListWithCategory(String category) {
        List<ProductDTO> productDTOListByCategory = getProductListByCategory(category);

        OrderDTO orderDTO = null;
        if (!productDTOListByCategory.isEmpty()) {
            orderDTO = new OrderDTO();
            orderDTO.setOrderNumber(String.valueOf(new Random().nextInt(100)));
            orderDTO.setCustomerName("Customer");
            orderDTO.setCustomerId(new Random().nextLong(1000));


            orderDTO.setQuantity(productDTOListByCategory.size());
            orderDTO.setProducts(productDTOListByCategory);

            BigDecimal totalPrice = productDTOListByCategory.stream()
                    .map(ProductDTO::getPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            orderDTO.setTotalPrice(totalPrice);
            createOrder(orderDTO);
        }

        return Optional.ofNullable(orderDTO);
    }

    private void createOrder(OrderDTO orderDTO) {
        if (orderDTO != null) {
            Order order = convertOrderDTOtoOrder(orderDTO);
            orderRepository.save(order);
        }
    }

    private Order convertOrderDTOtoOrder(OrderDTO orderDTO) {
        Order order = new Order();
        order.setOrderNumber(orderDTO.getOrderNumber());
        order.setCustomerName(orderDTO.getCustomerName());
        order.setCustomerId(orderDTO.getCustomerId());
        order.setTotalPrice(orderDTO.getTotalPrice());
        return order;
    }

    private List<ProductDTO> getProductListByCategory(String category) {
        List<ProductDTO> productDTOListByCategory = new ArrayList<>();
        try {
            return productDTOListByCategory = restClient.build()
                    .get()
                    .uri("http://localhost:8080/api/product/{category}", category)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                                throw new NoProductsFoundException(response.getStatusText());
                            }
                    )
                    .body(new ParameterizedTypeReference<List<ProductDTO>>() {
                    });
        } catch (NoProductsFoundException e) {
            return productDTOListByCategory;
        }
    }
}
