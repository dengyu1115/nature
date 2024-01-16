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
public enum Unit {

    SECOND("1", "秒"),
    MINUTE("2", "分"),
    HOUR("3", "时"),
    DAY("4", "日"),
    MONTH("5", "月");

    private final String code;
    private final String name;

    private static final Map<String, String> CODE_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(Unit::getCode, Unit::getName));

    private static final List<String> CODES = Arrays.stream(values()).map(Unit::getCode)
            .collect(Collectors.toList());

    public static String name(String code) {
        return CODE_NAME.get(code);
    }

    public static List<String> codes() {
        return new ArrayList<>(CODES);
    }

}
