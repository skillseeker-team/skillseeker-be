package com.example.skillseeker_be.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum QuestionCategory {
    fundamentals,
    network,
    database,
    architecture,
    framework,
    performance,
    devops,
    testing,
    project,
    collaboration,
    culture;

    private static final Set<String> VALID_NAMES = Arrays.stream(values())
            .map(Enum::name)
            .collect(Collectors.toSet());

    public static boolean isValid(String name) {
        return name != null && VALID_NAMES.contains(name);
    }
}
