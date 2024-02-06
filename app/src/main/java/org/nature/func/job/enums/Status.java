package org.nature.func.job.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 状态枚举
 * @author Nature
 * @version 1.0.0
 * @since 2024/2/3
 */
@AllArgsConstructor
@Getter
public enum Status {

    PAUSE("0", "暂停"),
    RUNNING("1", "启用");

    private final String code;
    private final String name;

    private static final Map<String, String> CODE_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(Status::getCode, Status::getName));

    private static final List<String> CODES = Arrays.stream(values()).map(Status::getCode)
            .collect(Collectors.toList());

    public static String name(String code) {
        return CODE_NAME.get(code);
    }

    public static List<String> codes() {
        return CODES;
    }

    public static boolean isRunning(String code) {
        return RUNNING.getCode().equals(code);
    }

}
