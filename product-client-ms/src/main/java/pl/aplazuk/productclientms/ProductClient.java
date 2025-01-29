package pl.aplazuk.productclientms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/product-client")
public class ProductClient {

    private static final String PRODUCT_CLIENT_CACHE_KEY_PREFIX = "pl.aplazuk.productclientms.product-client::";
    private final RestClient restClient;
    private final RedisTemplate<String, String> redisTemplate;

    public ProductClient(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.restClient = RestClient.create();
    }

    @GetMapping
    @Cacheable("product-client")
    @CircuitBreaker(name = "productClientCall", fallbackMethod = "fallbackGetProductsByCategory")
    public ResponseEntity<List<Product>> getProductsByCategory(@RequestParam String category) {
        List<Product> result = restClient.get()
                .uri("http://localhost:8080/api/product/{category}", category)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Product>>() {
                });
        return ResponseEntity.ok(result);
    }

    public ResponseEntity<List<Product>> fallbackGetProductsByCategory(String category, Throwable throwable) throws JsonProcessingException {
        String jsonResponseEntity = redisTemplate.opsForValue().get(PRODUCT_CLIENT_CACHE_KEY_PREFIX + category);
        ObjectMapper mapper = new ObjectMapper();

        JsonNode jsonNode = mapper.readTree(jsonResponseEntity);
        JsonNode responseBody = jsonNode.get("body");
        List<Product> productList = mapper.convertValue(responseBody, new TypeReference<>() {
        });
        return jsonResponseEntity != null ? ResponseEntity.ok(productList) : ResponseEntity.ok(Collections.emptyList());
    }
}
