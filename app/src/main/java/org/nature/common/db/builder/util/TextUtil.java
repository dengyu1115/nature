package org.nature.common.db.builder.util;

import org.nature.common.db.builder.model.Mapping;

import java.util.List;
import java.util.function.Function;

/**
 * 文本处理工具
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
public class TextUtil {

    /**
     * 转换列子句
     * @param mappings 映射关系数据
     * @return String
     */
    public static String columns(List<Mapping> mappings) {
        return mappings(mappings, Mapping::getColumn);
    }

    /**
     * 转换值子句
     * @param mappings 映射关系数据
     * @return String
     */
    public static String properties(List<Mapping> mappings) {
        return mappings(mappings, i -> "?");
    }

    /**
     * 转换条件子句
     * @param mappings 映射关系数据
     * @return String
     */
    public static String conditions(List<Mapping> mappings) {
        return mappings(mappings, i -> i.getColumn() + "=?");
    }

    /**
     * mapping数据转换
     * @param mappings 映射数据集合
     * @param func     转换逻辑
     * @return String
     */
    private static String mappings(List<Mapping> mappings, Function<Mapping, String> func) {
        StringBuilder builder = new StringBuilder();
        int size = mappings.size();
        for (int i = 0; i < size; i++) {
            Mapping mapping = mappings.get(i);
            builder.append(func.apply(mapping));
            if (i < size - 1) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

}
