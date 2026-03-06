package com.sellivu.backend.analysis.repository;

import com.sellivu.backend.analysis.entity.AnalysisJob;
import com.sellivu.backend.analysis.entity.AnalysisJobStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisJobRepository extends JpaRepository<AnalysisJob, Long> {

    @EntityGraph(attributePaths = "product")
    List<AnalysisJob> findByStatus(AnalysisJobStatus status);
}