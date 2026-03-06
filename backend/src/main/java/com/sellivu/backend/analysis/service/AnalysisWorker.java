package com.sellivu.backend.analysis.service;

import com.sellivu.backend.analysis.entity.AnalysisJob;
import com.sellivu.backend.analysis.entity.AnalysisJobStatus;
import com.sellivu.backend.analysis.repository.AnalysisJobRepository;
import com.sellivu.backend.product.dto.CrawledProductInfo;
import com.sellivu.backend.product.service.CrawlerServiceResolver;
import com.sellivu.backend.product.service.ProductCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisWorker {

    private final AnalysisJobRepository analysisJobRepository;
    private final CrawlerServiceResolver crawlerServiceResolver;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processJobs() {

        List<AnalysisJob> jobs = analysisJobRepository.findByStatus(AnalysisJobStatus.QUEUED);

        for (AnalysisJob job : jobs) {

            log.info("Processing job {}", job.getId());

            job.startCrawling();

            try {
                ProductCrawlerService crawlerService =
                        crawlerServiceResolver.resolve(job.getProduct().getPlatform());

                CrawledProductInfo crawled = crawlerService.crawl(job.getProduct());

                job.getProduct().updateProductInfo(
                        crawled.getProductName(),
                        crawled.getPrice(),
                        crawled.getRating(),
                        crawled.getReviewCount(),
                        crawled.getThumbnailUrl()
                );

                job.complete();

                log.info("Job {} completed", job.getId());

            } catch (Exception e) {
                job.fail(e.getMessage());
                log.error("Job {} failed", job.getId(), e);
            }
        }
    }
}