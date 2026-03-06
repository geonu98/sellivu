import express from "express";

const router = express.Router();

router.post("/product", (req, res) => {
  const { platform, normalizedUrl, storeName, externalProductId } = req.body;

  console.log("crawl request:", {
    platform,
    normalizedUrl,
    storeName,
    externalProductId,
  });

  return res.json({
    productName: "[스마트스토어] 테스트 상품",
    price: 19900,
    rating: 4.78,
    reviewCount: 1523,
    thumbnailUrl: "https://dummy-image.example.com/smartstore-product.jpg",
  });
});

export default router;