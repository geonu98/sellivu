package com.sellivu.backend.analysis.service;

import com.sellivu.backend.analysis.entity.AnalysisJob;
import com.sellivu.backend.analysis.entity.AnalysisJobStatus;
import com.sellivu.backend.analysis.repository.AnalysisJobRepository;
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
@Transactional
public class AnalysisWorker {

    private final AnalysisJobRepository analysisJobRepository;
    private final ProductCrawlerService productCrawlerService;

    @Scheduled(fixedDelay = 5000)
    public void processJobs() {

        List<AnalysisJob> jobs = analysisJobRepository.findByStatus(AnalysisJobStatus.QUEUED);

        for (AnalysisJob job : jobs) {

            log.info("Processing job {}", job.getId());

            job.startCrawling();
            analysisJobRepository.save(job);

            try {
                productCrawlerService.crawl(job.getProduct());
                job.complete();
                analysisJobRepository.save(job);

                log.info("Job {} completed", job.getId());

            } catch (Exception e) {
                job.fail(e.getMessage());
                analysisJobRepository.save(job);

                log.error("Job {} failed", job.getId(), e);
            }
        }
    }
}