import { chromium } from "playwright";
import { CrawlProductRequest } from "../dto/CrawlProductRequest";
import { CrawledProductResponse } from "../dto/CrawledProductResponse";

export class SmartStoreProductCrawler {
  async crawl(request: CrawlProductRequest): Promise<CrawledProductResponse> {
    console.log("smartstore crawl start:", request);

    const browser = await chromium.launch({
      headless: true,
    });

    const page = await browser.newPage({
      userAgent:
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
    });

    try {
      await page.goto(request.normalizedUrl, {
        waitUntil: "domcontentloaded",
        timeout: 30000,
      });

      await page.waitForTimeout(3000);

      const title = await page.title();
      const content = await page.content();

      console.log("page title:", title);
      console.log("html preview:", content.slice(0, 1500));

      await browser.close();

      return {
        productName: title || "[스마트스토어] 제목 없음",
        price: 19900,
        rating: 4.78,
        reviewCount: 1523,
        thumbnailUrl: "https://dummy-image.example.com/smartstore-product.jpg",
      };
    } catch (error) {
      await browser.close();
      console.error("playwright crawl error:", error);
      throw error;
    }
  }
}