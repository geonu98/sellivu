import { chromium } from "playwright";
import { CrawlProductRequest } from "../dto/CrawlProductRequest";
import { CrawledProductResponse } from "../dto/CrawledProductResponse";

export class SmartStoreProductCrawler {
  async crawl(request: CrawlProductRequest): Promise<CrawledProductResponse> {
    console.log("smartstore crawl start:", request);

    const browser = await chromium.launch({
      headless: false,
      slowMo: 300,
    });

    const context = await browser.newContext({
      viewport: { width: 1365, height: 900 },
      locale: "ko-KR",
      timezoneId: "Asia/Seoul",
      userAgent:
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36",
    });

    const page = await context.newPage();

    try {
      await page.goto("https://smartstore.naver.com/", {
        waitUntil: "domcontentloaded",
        timeout: 30000,
      });

      await page.waitForTimeout(2000);

      await page.goto(request.normalizedUrl, {
        waitUntil: "domcontentloaded",
        timeout: 30000,
      });

      await page.waitForTimeout(3000);

      const finalUrl = page.url();
      const title = await page.title();
      const content = await page.content();
      const bodyText = await page.locator("body").innerText().catch(() => "");

      const isCaptchaPage =
        content.includes("captcha") ||
        content.includes("captcha_wrap") ||
        content.includes("captcha_form");

      const isLoginPage =
        content.includes("nid.login") ||
        bodyText.includes("로그인");

      console.log("final url:", finalUrl);
      console.log("page title:", title);
      console.log("body preview:", bodyText.slice(0, 500));
      console.log("html preview:", content.slice(0, 1500));
      console.log("is captcha page:", isCaptchaPage);
      console.log("is login page:", isLoginPage);

      if (isCaptchaPage) {
        throw new Error("SmartStore blocked crawl with captcha page");
      }

      if (isLoginPage) {
        throw new Error("SmartStore redirected to login page");
      }

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