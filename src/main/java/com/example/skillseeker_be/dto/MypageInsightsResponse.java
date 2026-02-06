package com.example.skillseeker_be.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MypageInsightsResponse {
    private List<String> highlights;
    private List<String> nextActions;
    private boolean cached;
}
