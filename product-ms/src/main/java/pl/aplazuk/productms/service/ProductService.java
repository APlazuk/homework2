package pl.aplazuk.productms.service;

import org.springframework.stereotype.Service;
import pl.aplazuk.productms.model.Product;
import pl.aplazuk.productms.repository.ProductRepository;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductByCategory(String category) {
        return productRepository.findAllByCategory(category);
    }
}
