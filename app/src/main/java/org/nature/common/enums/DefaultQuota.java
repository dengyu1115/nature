package org.nature.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum DefaultQuota {

    HS("000300", "沪深300"),
    SZ("000001", "上证综指"),
    SC("399001", "深证成指"),
    ZXB("399005", "中小板指"),
    CYB("399006", "创业板指");

    public static final List<String> CODES = Arrays.stream(values())
            .map(DefaultQuota::getCode).collect(Collectors.toList());
    public static final Map<String, String> CODE_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(DefaultQuota::getCode, DefaultQuota::getName));

    private final String code;
    private final String name;

    public static String codeToName(String code) {
        return CODE_NAME.get(code);
    }

    public static List<String> codes() {
        return CODES;
    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }
}
