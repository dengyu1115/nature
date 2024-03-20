package org.nature.biz.memory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 债券内存缓存
 * @author Nature
 * @version 1.0.0
 * @since 2024/3/19
 */
public class BoundMemory {
    /**
     * code-名称map
     */
    public static final Map<String, String> NAME_MAP = Map.of(
            "159649", "国开债ETF",
            "159650", "国开ETF",
            "159651", "国开债券ETF"
    );
    /**
     * 品种集合
     */
    public static final List<String> LIST = NAME_MAP.keySet().stream().sorted().collect(Collectors.toList());

}
