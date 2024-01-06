package org.nature.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum ExeStatus {

    START("0", "执行中"),
    END("1", "执行结束"),
    EXCEPTION("2", "执行异常");

    private static final List<String> CODES = Arrays.stream(values())
            .map(ExeStatus::getCode).collect(Collectors.toList());
    private static final Map<String, String> CODE_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(ExeStatus::getCode, ExeStatus::getName));
    private final String code, name;

    public static List<String> codes() {
        return CODES;
    }

    public static String codeToName(String code) {
        return CODE_NAME.get(code);
    }

}
