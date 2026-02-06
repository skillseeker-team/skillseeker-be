package com.example.skillseeker_be.registry;

import com.example.skillseeker_be.enums.WeaknessTag;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class WeaknessTagRegistry {

    private static final Map<WeaknessTag, TagMeta> REGISTRY = new EnumMap<>(WeaknessTag.class);

    static {
        REGISTRY.put(WeaknessTag.structure, new TagMeta("답변 구조", "답변이 체계적이지 않고 두서없이 전달됨"));
        REGISTRY.put(WeaknessTag.evidence, new TagMeta("근거 부족", "주장에 대한 구체적 근거나 사례가 없음"));
        REGISTRY.put(WeaknessTag.depth, new TagMeta("깊이 부족", "기술적 깊이가 얕아 표면적 수준에 머묾"));
        REGISTRY.put(WeaknessTag.clarity, new TagMeta("명확성", "핵심 메시지가 불명확하거나 모호함"));
        REGISTRY.put(WeaknessTag.conciseness, new TagMeta("간결성", "불필요한 내용이 많아 핵심 전달이 약함"));
        REGISTRY.put(WeaknessTag.followup, new TagMeta("꼬리질문 대응", "예상 꼬리질문에 대한 준비가 부족함"));
        REGISTRY.put(WeaknessTag.pressure_handling, new TagMeta("압박 대응", "압박 상황에서 논리적 대응이 부족함"));
        REGISTRY.put(WeaknessTag.ownership, new TagMeta("주도성", "본인의 역할과 기여가 명확하지 않음"));
        REGISTRY.put(WeaknessTag.tradeoff, new TagMeta("트레이드오프", "기술 선택의 장단점 분석이 부족함"));
        REGISTRY.put(WeaknessTag.fundamentals, new TagMeta("기본기", "CS 기본 개념에 대한 이해가 부족함"));
    }

    public TagMeta get(WeaknessTag tag) {
        return REGISTRY.get(tag);
    }

    public TagMeta get(String tagName) {
        try {
            return REGISTRY.get(WeaknessTag.valueOf(tagName));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public record TagMeta(String label, String description) {}
}
