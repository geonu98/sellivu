package com.sellivu.backend.product.service;

import com.sellivu.backend.platform.CommercePlatform;
import com.sellivu.backend.product.dto.CrawledProductInfo;
import com.sellivu.backend.product.entity.Product;

public interface ProductCrawlerService {

    boolean supports(CommercePlatform platform);

    CrawledProductInfo crawl(Product product);
}