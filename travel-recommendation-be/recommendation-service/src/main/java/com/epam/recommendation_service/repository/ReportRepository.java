package com.epam.recommendation_service.repository;

import com.epam.recommendation_service.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report,Long> {
    List<Report> findByReportedByAndCommentID(String reportedBy, int commentID);
}
