package com.sellivu.backend.product.service;

import com.sellivu.backend.platform.CommercePlatform;
import com.sellivu.backend.product.dto.CrawlProductRequest;
import com.sellivu.backend.product.dto.CrawledProductInfo;
import com.sellivu.backend.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartStoreProductCrawlerService implements ProductCrawlerService {

    private final RestTemplate restTemplate;

    @Override
    public boolean supports(CommercePlatform platform) {
        return platform == CommercePlatform.SMARTSTORE;
    }

    @Override
    public CrawledProductInfo crawl(Product product) {
        log.info("스마트스토어 crawler API 호출 - storeName={}, productId={}",
                product.getStoreName(),
                product.getExternalProductId());

        CrawlProductRequest request = CrawlProductRequest.builder()
                .platform(product.getPlatform())
                .normalizedUrl(product.getNormalizedUrl())
                .storeName(product.getStoreName())
                .externalProductId(product.getExternalProductId())
                .build();

        String url = "http://localhost:3001/internal/crawl/product";

        try {
            ResponseEntity<CrawledProductInfo> response =
                    restTemplate.postForEntity(url, request, CrawledProductInfo.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("크롤러 호출 실패: status=" + response.getStatusCode());
            }

            CrawledProductInfo body = response.getBody();
            if (body == null) {
                throw new IllegalStateException("크롤러 응답이 비어 있습니다.");
            }

            return body;

        } catch (HttpStatusCodeException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("크롤러 HTTP 오류 - status={}, body={}", e.getStatusCode(), responseBody, e);

            if (responseBody != null &&
                    responseBody.contains("SmartStore blocked crawl with captcha page")) {
                throw new IllegalStateException("스마트스토어 보안 확인 페이지로 차단되었습니다.");
            }

            throw new IllegalStateException("크롤러 HTTP 오류: " + e.getStatusCode());
        } catch (ResourceAccessException e) {
            log.error("크롤러 서버 연결 실패", e);
            throw new IllegalStateException("크롤러 서버에 연결할 수 없습니다.");
        }
    }
}