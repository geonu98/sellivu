export interface CrawlProductRequest {
  platform: string;
  normalizedUrl: string;
  storeName: string;
  externalProductId: string;
}