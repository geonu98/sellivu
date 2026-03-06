package com.sellivu.backend.product.repository;

import com.sellivu.backend.platform.CommercePlatform;
import com.sellivu.backend.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByPlatformAndStoreNameAndExternalProductId(
            CommercePlatform platform,
            String storeName,
            String externalProductId
    );
}