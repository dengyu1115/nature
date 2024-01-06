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

    SECOND("0", "秒", "yyyy-MM-dd HH:mm:", List.of()),
    MINUTE("1", "分钟", "yyyy-MM-dd HH:", List.of("0")),
    HOUR("2", "小时", "yyyy-MM-dd ", List.of("0", "1")),
    DAY("3", "天", "yyyy-MM-", List.of("0", "1", "2")),
    WORKDAY("4", "工作日", "yyyy-MM-", List.of("0", "1", "2")),
    WEEK("5", "周", "yyyy-MM-", List.of("0", "1", "2", "3")),
    MONTH("6", "月", "yyyy-", List.of("0", "1", "2", "5")),
    YEAR("7", "年", "", List.of("0", "1", "2", "3", "4", "6"));

    private final String code;
    private final String name;
    private final String prefix;
    private final List<String> units;

    private static final Map<String, String> CODE_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(Type::getCode, Type::getName));

    private static final Map<String, List<String>> CODE_UNITS = Arrays.stream(values())
            .collect(Collectors.toMap(Type::getCode, Type::getUnits));

    private static final List<String> CODES = Arrays.stream(values()).map(Type::getCode)
            .collect(Collectors.toList());

    public static String name(String code) {
        return CODE_NAME.get(code);
    }

    public static List<String> units(String code) {
        return new ArrayList<>(CODE_UNITS.getOrDefault(code, new ArrayList<>()));
    }

    public static List<String> codes() {
        return CODES;
    }

}
