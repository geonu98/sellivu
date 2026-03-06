package com.sellivu.backend.product.service;

import com.sellivu.backend.product.entity.Product;

public interface ProductCrawlerService {
    void crawl(Product product);
}