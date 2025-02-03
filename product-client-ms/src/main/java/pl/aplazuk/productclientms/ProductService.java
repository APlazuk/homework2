package pl.aplazuk.productclientms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class ProductService {

    private static final String PRODUCT_CLIENT_CACHE_KEY_PREFIX = "pl.aplazuk.productclientms.product-client::";
    private final RestClient restClient;
    private final RedisTemplate<String, String> redisTemplate;

    public ProductService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.restClient = RestClient.create();
    }


    @CircuitBreaker(name = "productClientCall", fallbackMethod = "fallbackGetProductsByCategory")
    @Cacheable("product-client")
    public List<Product> getProductsByCategory(String category) {
        List<Product> result = restClient.get()
                .uri("http://localhost:8080/api/product/{category}", category)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Product>>() {
                });
        return result;
    }

    public List<Product> fallbackGetProductsByCategory(String category, Throwable throwable) throws JsonProcessingException {
        String jsonProducts = redisTemplate.opsForValue().get(PRODUCT_CLIENT_CACHE_KEY_PREFIX + category);
        return new ObjectMapper().readValue(jsonProducts, new TypeReference<List<Product>>() {
        });
    }


}
