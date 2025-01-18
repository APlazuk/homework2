package pl.aplazuk.orderms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;
import pl.aplazuk.orderms.dto.OrderDTO;
import pl.aplazuk.orderms.dto.ProductDTO;
import pl.aplazuk.orderms.model.Order;
import pl.aplazuk.orderms.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(OrderService.class)
class OrderServiceTest {

    private static final String CATEGORY = "zabawki";

    @Autowired
    MockRestServiceServer server;

    @MockitoBean
    OrderRepository orderRepository;

    @Autowired
    OrderService orderService;

    @Autowired
    ObjectMapper objectMapper;

    @Captor
    ArgumentCaptor<Order> orderCaptor;

    private List<ProductDTO> productsByCategory;

    @BeforeEach
    void setUp() {
        List<ProductDTO> products = List.of(
                new ProductDTO(1L, "samochód zdalnie sterowany", "zabawki", new BigDecimal(12.55)),
                new ProductDTO(2L, "klocki lego", "zabawki", new BigDecimal(48.89)),
                new ProductDTO(3L, "poradnik jak pisać testy jednostkowe", "książki", new BigDecimal(25.00))
        );

        productsByCategory = products.stream().filter(product -> product.getCategory().equals(CATEGORY)).toList();
    }


    @Test
    public void shouldReturnSelectedProductsByCategory() throws JsonProcessingException {
        //given
        server.expect(requestTo("http://localhost:8080/api/product/" + CATEGORY))
                .andRespond(withSuccess(objectMapper.writeValueAsString(productsByCategory), MediaType.APPLICATION_JSON));
        BigDecimal totalPriceForProductByCategory = productsByCategory.stream().map(ProductDTO::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

        //when
        Optional<OrderDTO> actual = orderService.collectOrderByProductListWithCategory(CATEGORY);

        //then
        verify(orderRepository, times(1)).save(orderCaptor.capture());
        assertTrue(actual.isPresent());
        assertEquals(2, actual.get().getProducts().size());
        assertEquals(totalPriceForProductByCategory, actual.get().getTotalPrice());
    }

    @Test
    public void shouldNotReturnOrderWithSelectedProductsByCategory() throws JsonProcessingException {
        //given
        server.expect(requestTo("http://localhost:8080/api/product/" + CATEGORY))
                .andRespond(withResourceNotFound());
        //when
        Optional<OrderDTO> orderDTO = orderService.collectOrderByProductListWithCategory(CATEGORY);
        //then
        verify(orderRepository, never()).save(orderCaptor.capture());
        assertFalse(orderDTO.isPresent());
    }

}