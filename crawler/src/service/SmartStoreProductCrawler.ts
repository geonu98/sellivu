import { CrawlProductRequest } from "../dto/CrawlProductRequest";
import { CrawledProductResponse } from "../dto/CrawledProductResponse";

export class SmartStoreProductCrawler {
  async crawl(request: CrawlProductRequest): Promise<CrawledProductResponse> {
    console.log("smartstore crawl start:", request);

    return {
      productName: "[스마트스토어] 테스트 상품",
      price: 19900,
      rating: 4.78,
      reviewCount: 1523,
      thumbnailUrl: "https://dummy-image.example.com/smartstore-product.jpg",
    };
  }
}