package org.nature.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum ItemType {

    STOCK("STOCK", "股票"),
    INDEX("INDEX", "指数"),
    FUND("FUND", "基金");

    private static final Map<String, String> CODE_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(ItemType::getCode, ItemType::getName));
    private String code;
    private String name;

    public static String codeToName(String code) {
        return CODE_NAME.get(code);
    }

    public static List<String> codes() {
        return Arrays.stream(values()).map(ItemType::getCode).collect(Collectors.toList());
    }

}
