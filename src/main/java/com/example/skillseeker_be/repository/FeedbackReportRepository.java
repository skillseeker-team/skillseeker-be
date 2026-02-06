package com.example.skillseeker_be.repository;

import com.example.skillseeker_be.entity.FeedbackReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackReportRepository extends JpaRepository<FeedbackReport, Long> {
}
