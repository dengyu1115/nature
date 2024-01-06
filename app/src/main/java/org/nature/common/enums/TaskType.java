package org.nature.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum TaskType {

    IN_WORKDAY("0", "工作日执行"),
    AFTER_WORKDAY("1", "工作日后执行");

    private static final List<String> CODES = Arrays.stream(values())
            .map(TaskType::getCode).collect(Collectors.toList());
    private static final Map<String, String> CODE_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(TaskType::getCode, TaskType::getName));
    private String code, name;

    public static List<String> codes() {
        return CODES;
    }

    public static String codeToName(String code) {
        return CODE_NAME.get(code);
    }

}
