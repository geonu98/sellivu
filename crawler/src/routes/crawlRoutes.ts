import express from "express";
import { CrawlProductRequest } from "../dto/CrawlProductRequest";
import { SmartStoreProductCrawler } from "../service/SmartStoreProductCrawler";

const router = express.Router();
const smartStoreProductCrawler = new SmartStoreProductCrawler();

router.post("/product", async (req, res) => {
  const body = req.body as Partial<CrawlProductRequest> | undefined;

  try {
    if (!body || !body.platform || !body.normalizedUrl) {
      return res.status(400).json({
        errorCode: "INVALID_REQUEST",
        errorMessage: "platform과 normalizedUrl은 필수입니다.",
      });
    }

    const result = await smartStoreProductCrawler.crawl(body as CrawlProductRequest);
    return res.json(result);
  } catch (error) {
    console.error("crawl error:", error);

    const message =
      error instanceof Error ? error.message : "크롤링 중 알 수 없는 오류가 발생했습니다.";

    return res.status(500).json({
      errorCode: "CRAWL_FAILED",
      errorMessage: message,
    });
  }
});

export default router;