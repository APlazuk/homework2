package pl.aplazuk.orderms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import pl.aplazuk.orderms.dto.OrderDTO;
import pl.aplazuk.orderms.dto.ProductDTO;
import pl.aplazuk.orderms.model.Order;
import pl.aplazuk.orderms.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private static final String CATEGORY = "zabawki";

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private RestClient.Builder restClientBuilder;

    private OrderService orderService;

    @Captor
    ArgumentCaptor<Order> orderCaptor;

    private List<ProductDTO> mockProductsByCategory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderService(orderRepository, restClientBuilder);

        when(restClientBuilder.build()).thenReturn(restClient);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), Optional.ofNullable(any()))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        List<ProductDTO> products = List.of(
                new ProductDTO(1L, "samochód zdalnie sterowany", "zabawki", new BigDecimal("12.55")),
                new ProductDTO(2L, "klocki lego", "zabawki", new BigDecimal("48.89")),
                new ProductDTO(3L, "poradnik jak pisać testy jednostkowe", "książki", new BigDecimal("25.00"))
        );

        mockProductsByCategory = products.stream().filter(product -> product.getCategory().equals(CATEGORY)).toList();
    }

    @Test
    public void shouldReturnSelectedProductsByCategory() {
        //given
        BigDecimal totalPriceForProductByCategory = mockProductsByCategory.stream().map(ProductDTO::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        when(responseSpec.body(new ParameterizedTypeReference<List<ProductDTO>>() {
        })).thenReturn(mockProductsByCategory);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        //when
        Optional<OrderDTO> actual = orderService.collectOrderByProductListWithCategory(CATEGORY);

        //then
        verify(orderRepository, times(1)).save(orderCaptor.capture());
        assertTrue(actual.isPresent());
        assertEquals(2, actual.get().getProducts().size());
        assertEquals(totalPriceForProductByCategory, actual.get().getTotalPrice());
    }

    @Test
    public void shouldNotReturnOrderWithSelectedProductsByCategory(){
        //given
        ArgumentCaptor<Predicate<HttpStatusCode>> statusCodeCaptor = ArgumentCaptor.forClass(Predicate.class);
        when(responseSpec.onStatus(any(Predicate.class), any(RestClient.ResponseSpec.ErrorHandler.class))).thenReturn(responseSpec);
        when(responseSpec.body(new ParameterizedTypeReference<List<ProductDTO>>() {
        })).thenThrow(new NoProductsFoundException("No product found for given category"));

        //when
        Optional<OrderDTO> actual = orderService.collectOrderByProductListWithCategory(CATEGORY);

        //then
        verify(orderRepository, never()).save(orderCaptor.capture());
        verify(responseSpec, times(1)).onStatus(statusCodeCaptor.capture(), any());
        assertTrue(statusCodeCaptor.getValue().test(HttpStatus.NOT_FOUND));
        assertFalse(actual.isPresent());
    }

}