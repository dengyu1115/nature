package org.nature.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum Market {

    SZ("sz", "深圳", "m:0+t:6,m:0+t:80"),
    SH("sh", "上海", "m:1+t:2,m:1+t:23"),
    BJ("bj", "北京", "m:0+t:81+s:2048");

    private static final Map<String, String> CODE_NAME = Arrays.stream(values()).collect(Collectors.toMap(Market::getCode, Market::getName));
    private static final Map<String, String> CODE_FS = Arrays.stream(values()).collect(Collectors.toMap(Market::getCode, Market::getFs));
    private final String code;
    private final String name;
    private final String fs;

    public static String codeToName(String code) {
        return CODE_NAME.get(code);
    }

    public static String codeToFs(String code) {
        return CODE_FS.get(code);
    }
}
