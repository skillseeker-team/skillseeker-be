package com.example.skillseeker_be.repository;

import com.example.skillseeker_be.entity.FeedbackChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackChecklistItemRepository extends JpaRepository<FeedbackChecklistItem, Long> {
}
