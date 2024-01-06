package org.nature.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum QuotaField {

    JG("01", "价格"),
    SY("02", "市盈"),
    GS("03", "股数"),
    SZ("04", "市值"),
    SZ_LT("05", "流通市值"),
    GB("06", "股本"),
    GB_LT("07", "流通股本");

    private static final List<String> CODES = Arrays.stream(values())
            .map(QuotaField::getCode).collect(Collectors.toList());
    private static final Map<String, String> CODE_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(QuotaField::getCode, QuotaField::getName));
    private final String code;
    private final String name;

    public static String codeToName(String code) {
        return CODE_NAME.get(code);
    }

    public static List<String> codes() {
        return CODES;
    }
}
