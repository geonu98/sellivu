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
        message: "platform과 normalizedUrl은 필수입니다.",
      });
    }

    const result = await smartStoreProductCrawler.crawl(body as CrawlProductRequest);
    return res.json(result);
  } catch (error) {
    console.error("crawl error:", error);
    return res.status(500).json({
      message: "크롤링 중 오류가 발생했습니다.",
    });
  }
});

export default router;