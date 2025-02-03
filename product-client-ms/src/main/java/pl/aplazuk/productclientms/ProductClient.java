package pl.aplazuk.productclientms;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/product-client")
public class ProductClient {

    private final ProductService productService;

    public ProductClient(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getProductsByCategory(@RequestParam String category) {
        List<Product> result = productService.getProductsByCategory(category);
        return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
    }

}
