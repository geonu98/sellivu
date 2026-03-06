import express from "express";
import crawlRoutes from "./crawlRoutes";

const app = express();
const PORT = 3001;

app.use(express.json());
app.use("/internal/crawl", crawlRoutes);

app.get("/health", (_req, res) => {
  res.json({ status: "ok" });
});

app.listen(PORT, () => {
  console.log(`Crawler server running on port ${PORT}`);
});