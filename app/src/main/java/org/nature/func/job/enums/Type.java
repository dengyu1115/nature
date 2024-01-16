package org.nature.func.job.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum Type {

    ONCE("0", "一次"),
    REPEAT("1", "重复");

    private final String code;
    private final String name;

    private static final Map<String, String> CODE_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(Type::getCode, Type::getName));

    private static final List<String> CODES = Arrays.stream(values()).map(Type::getCode)
            .collect(Collectors.toList());

    public static String name(String code) {
        return CODE_NAME.get(code);
    }

    public static List<String> codes() {
        return CODES;
    }

}
