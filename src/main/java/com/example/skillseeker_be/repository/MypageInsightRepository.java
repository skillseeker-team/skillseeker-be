package com.example.skillseeker_be.repository;

import com.example.skillseeker_be.entity.MypageInsight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MypageInsightRepository extends JpaRepository<MypageInsight, Long> {

    Optional<MypageInsight> findByUserIdAndSummaryHash(Long userId, String summaryHash);
}
