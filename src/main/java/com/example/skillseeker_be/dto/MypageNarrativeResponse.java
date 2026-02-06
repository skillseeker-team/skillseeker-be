package com.example.skillseeker_be.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MypageNarrativeResponse {
    private List<String> narratives;
}
