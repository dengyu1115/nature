package org.nature.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum DateType {

    WORKDAY("0", "W"),
    HOLIDAY("1", "H");

    private static final Map<String, String> CODE_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(DateType::getCode, DateType::getName));
    private final String code;
    private final String name;

    public static String codeToName(String code) {
        return CODE_NAME.get(code);
    }
}
