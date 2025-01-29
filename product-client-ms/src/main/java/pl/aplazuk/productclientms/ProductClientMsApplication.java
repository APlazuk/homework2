package pl.aplazuk.productclientms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ProductClientMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductClientMsApplication.class, args);
	}

}
